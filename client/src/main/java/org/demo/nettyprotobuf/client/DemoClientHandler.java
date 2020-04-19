package org.demo.nettyprotobuf.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.demo.nettyprotobuf.proto.DemoMessages;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class DemoClientHandler extends SimpleChannelInboundHandler<DemoMessages.DemoResponse> {

  private Channel channel;
  private DemoMessages.DemoResponse resp;
  private final BlockingQueue<DemoMessages.DemoResponse> resps = new LinkedBlockingQueue<DemoMessages.DemoResponse>();
  public DemoMessages.DemoResponse sendRequest() {
    DemoMessages.DemoRequest req = DemoMessages.DemoRequest.newBuilder()
                            .setRequestMsg("From Client").build();
    
    // Send request
    channel.writeAndFlush(req);
    
    // Now wait for response from server
    boolean interrupted = false;
    for (;;) {
        try {
            resp = resps.take();
            break;
        } catch (InterruptedException ignore) {
            interrupted = true;
        }
    }

    if (interrupted) {
        Thread.currentThread().interrupt();
    }
    
    return resp;
  }

  @Override
  public void channelRegistered(ChannelHandlerContext ctx) {
      channel = ctx.channel();
  }
  
  @Override
  protected void channelRead0(ChannelHandlerContext ctx, DemoMessages.DemoResponse msg)
      throws Exception {
    resps.add(msg);
  }
  
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
      cause.printStackTrace();
      ctx.close();
  }
}