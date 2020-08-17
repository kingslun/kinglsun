FROM java:8
COPY ./target/kings-docker.jar /root/startup/
WORKDIR /root/startup/
EXPOSE 8080
ENTRYPOINT exec java ${JVM_OPTS:- -Xdebug -Xms1024m -Xmx1024m} -jar /root/startup/kings-docker.jar