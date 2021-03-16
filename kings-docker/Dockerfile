FROM java:8
COPY ./target/kings-docker.jar /root/startup/app.jar
WORKDIR /root/startup/
EXPOSE 8080
ENTRYPOINT exec java ${JVM_OPTS:- -Xdebug -Xms124m -Xmx124m} -jar /root/startup/app.jar