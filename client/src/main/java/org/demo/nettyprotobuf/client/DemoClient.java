package org.demo.nettyprotobuf.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.demo.nettyprotobuf.proto.DemoMessages;


public class DemoClient {
  
  static final String HOST = System.getenv("host");
  static final int PORT = Integer.parseInt(System.getenv("port"));
  public static void main(String[] args) throws InterruptedException {
    EventLoopGroup group = new NioEventLoopGroup();

    try {
      Bootstrap bootstrap = new Bootstrap();
      bootstrap.group(group)
               .channel(NioSocketChannel.class)
               .handler(new LoggingHandler(LogLevel.INFO))
               .handler(new DemoClientInitializer());
      
      // Create connection 
      Channel c = bootstrap.connect(HOST, PORT).sync().channel();

      int i = 0;
      while (i++ < 10) {
        DemoClientHandler handle = c.pipeline().get(DemoClientHandler.class);
        DemoMessages.DemoResponse resp = handle.sendRequest();
        System.out.println("Got response msg from Server: " + resp.getResponseMsg());
        Thread.sleep(2000);
      }

      c.close();


    } finally {
      group.shutdownGracefully();
    }
    
  }
}