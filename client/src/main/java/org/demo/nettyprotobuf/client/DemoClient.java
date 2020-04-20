package org.demo.nettyprotobuf.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.demo.nettyprotobuf.proto.DemoMessages;


public class DemoClient {
  
  static final String HOST;
  static final int PORT;

  static {
    if (System.getenv("port") == null){
      PORT = 8080;
    } else {
      PORT = Integer.parseInt(System.getenv("port"));
    }

    if (System.getenv("host") == null){
      HOST = "127.0.0.1";
    } else {
      HOST =  System.getenv("host");
    }
  }

  public static void main(String[] args) throws InterruptedException {
    EventLoopGroup group = new NioEventLoopGroup();

    try {
      Bootstrap bootstrap = new Bootstrap();
      bootstrap.group(group)
               .channel(NioSocketChannel.class)
               .handler(new DemoClientInitializer());
      
      // Create connection 
      Channel c = bootstrap.connect(HOST, PORT).sync().channel();
      DemoMsgClientHandler handle = c.pipeline().get(DemoMsgClientHandler.class);

      int i = 0;
      while (i++ < 10) {
        DemoMessages.DemoResponse resp = handle.sendRequest(DemoMessages.Type.MSG);
        System.out.println("Got response msg from Server: " + resp.getResponseMsg());
        Thread.sleep(1000);
      }

      DemoMessages.DemoResponse resp = handle.sendRequest(DemoMessages.Type.FILE);
      System.out.println("Got response msg from Server: " + resp.getResponseMsg());

      c.close();


    } finally {
      group.shutdownGracefully();
    }
    
  }
}