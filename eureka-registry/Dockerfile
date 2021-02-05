FROM openjdk:8-jdk-alpine
VOLUME /tmp
ARG JAR_FILE
ADD ${JAR_FILE} app.jar
# -Djava.security.egd这个配置解决tomcat8启动时，因为需要收集环境噪声来生成安全随机数导致启动过慢的问题
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]