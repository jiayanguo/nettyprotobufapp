FROM openjdk:8
ENV port=8080
ENV host=127.0.0.1
COPY target/client-*.jar /opt/apps/client.jar
WORKDIR /opt/apps/
CMD ["java", "-jar", "./client.jar"]