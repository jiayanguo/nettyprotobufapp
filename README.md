# This is a network application build on netty, websocket and protobuf.

## How to build
* mvn clean install

## How to run.
* First Option (use docker compose):
    
    start:
    
    `docker-compose up`
    
    build docker image:
    
    `docker-compose build`
    
    stop:
    
    `docker-compose down`
    
* Second Option (Manually):

    Start server:
    ```
       java -jar server/target/server-1.0-SNAPSHOT.jar
    ```
    Start client:
    ```
       java -jar client/target/client-1.0-SNAPSHOT.jar
    ```
  
 ## What it does?
 
 * Server:
 
    The netty network application server accepts plain test messages and files. 
    The files will be saved under /tmp folder.
 
 * Client:
 
    The client sends 10 plain text messages and then one file. Then exit.
 
 
## Note:

By default, it uses port 8080. Make sure the port is available.

## Reference:

https://netty.io/

https://www.baeldung.com/netty

https://dzone.com/articles/build-a-simple-netty-application-with-and-without

https://github.com/lohitvijayarenu/netty-protobuf

https://github.com/spmallette/netty-example

https://blog.csdn.net/hxx688/article/details/103417976
