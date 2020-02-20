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