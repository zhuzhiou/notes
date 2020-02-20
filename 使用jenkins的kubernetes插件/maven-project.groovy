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