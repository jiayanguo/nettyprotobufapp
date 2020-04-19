package org.demo.nettyprotobuf.server;

import org.demo.nettyprotobuf.proto.DemoMessages;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class DemoProtocolServerHandler extends SimpleChannelInboundHandler<DemoMessages.DemoRequest> {

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, DemoMessages.DemoRequest msg)
  {
    
    DemoMessages.DemoResponse.Builder builder = DemoMessages.DemoResponse.newBuilder();
    String message = "Accepted from Server, returning response";
    System.out.println(message);
    builder.setResponseMsg(message)
           .setCode(0);
    ctx.write(builder.build());
    
  }
  
  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
      ctx.flush();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
      cause.printStackTrace();
      ctx.close();
  }

}