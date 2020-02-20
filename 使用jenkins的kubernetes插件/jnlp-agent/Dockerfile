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
