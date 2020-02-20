# kubernetes plugin

插件首页: https://plugins.jenkins.io/kubernetes

代码托管：https://github.com/jenkinsci/kubernetes-plugin

## 前提条件

* jenkins与kubernetes在同一个集群
* kubernetes集群支持动态卷

## 配置

打开**系统管理**->**系统配置**，新增一个云，配置以下4项：

* 名称：kubernetes
* Kubernetes 地址：https://kubernetes.default.svc.cluster.local
* Direct Connection：不勾
* Jenkins 地址：http://jenkins.jenkins.svc.cluster.local:8080

## 使用示例

### echo

```
podTemplate {
  node(POD_LABEL) {
    stage('Run shell') {
      sh 'echo hello world'
    }
  }
}
```

### 使用jnlp-agent

```
podTemplate(containers: [
    containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-agent-maven:jdk11')
]) {
  node(POD_LABEL) {
    
    git url: 'http://git.tisson.imgruud.com/zhuzhiou/helloworld.git'
    
    stage('Compile') {
      sh 'mvn clean compile'
    }
  }
}
```

使用PersistentVolumeClaim
```
podTemplate(containers: [
    containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-agent-maven:jdk11')
], volumes: [
  persistentVolumeClaim(mountPath: '/root/.m2/repository', claimName: 'maven-local')
]) {
  node(POD_LABEL) {
    
    git url: 'http://git.tisson.imgruud.com/zhuzhiou/helloworld.git'
    
    stage('Compile') {
      sh 'mvn clean compile'
    }
  }
}
```
上面这个脚本需要在k8s里创建一个pvc
```
# cat << EOF | kubectl -n jenkin apply -f -
kind: PersistentVolumeClaim
apiVersion: v1
metadata:
  name: maven-local
spec:
  accessModes:
    - ReadWriteMany
  storageClassName: fast
  resources:
    requests:
      storage: 5Gi
EOF
```

## 定制jnlp-slave

以上的示例，在示例中使用是没有问题，但在实际项目中，这些镜并没有提供docker、kuberctl等工具，最简单有效的方法是定制自己的jnlp-slave镜像。

```
FROM jenkins/jnlp-slave:alpine as jnlp

FROM openjdk:11-jdk

USER root

RUN apt-get install git && rm -rf /var/lib/apt/lists/*

ADD docker-18.09.9-linux-amd64.tar.gz /usr/local/bin/
ADD kubectl-v1.17.2-linux-amd64.tar.gz /usr/local/bin/
ADD apache-maven-3.6.3-bin.tar.gz /opt/

COPY --from=jnlp /usr/local/bin/jenkins-agent /usr/local/bin/jenkins-agent
COPY --from=jnlp /usr/share/jenkins/agent.jar /usr/share/jenkins/agent.jar

ENV M3_HOME /opt/apache-maven-3.6.3
ENV PATH $PATH:$M3_HOME/bin

ENTRYPOINT ["/usr/local/bin/jenkins-agent"]
```
使用
```
podTemplate(serviceAccount: 'jenkins', containers: [
    containerTemplate(name: 'jnlp', image: 'zhuzhiou/jnlp-slave', workingDir: '/var/jenkins/')
], envVars: [
  envVar(key: 'MAVEN_OPTS', value: '-Duser.home=/var/maven -Djava.awt.headless=true')
], volumes: [
  hostPathVolume(mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock'),
  persistentVolumeClaim(mountPath: '/var/maven/.m2/repository', claimName: 'maven-local')
]) {
  node(POD_LABEL) {
    
    checkout scm
    
    stage('Compile') {
      sh 'mvn clean package'
    }
    
    stage('Test') {
      sh 'mvn test'
    }
    
    stage('Build') {
      docker.build('test')
    }
    
    stage('Deploy') {
      sh 'kubectl -n default apply -f manifest/deployment.yaml'
    }
    
  }
}
```
