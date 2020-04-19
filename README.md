# This is a network application build on netty and protobuf.

## How to build
* mvn clean install

## How to run.
* First Option (use docker compose):

    docker-compose up
    
* Second Option (Manually):

    Start server:
    ```
       java -jar server/target/server-1.0-SNAPSHOT.jar
    ```
    Start client:
    ```
       java -jar client/target/client-1.0-SNAPSHOT.jar
    ```

## Reference:

https://www.baeldung.com/netty

https://dzone.com/articles/build-a-simple-netty-application-with-and-without

https://github.com/lohitvijayarenu/netty-protobuf
