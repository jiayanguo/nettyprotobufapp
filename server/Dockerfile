FROM openjdk:8
ENV port=8080
COPY target/server-*.jar /opt/apps/server.jar
WORKDIR /opt/apps/
CMD ["java", "-jar", "./server.jar"]