package org.demo.nettyprotobuf.client;

import com.google.protobuf.ByteString;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.demo.nettyprotobuf.proto.DemoMessages;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class DemoMsgClientHandler extends SimpleChannelInboundHandler<DemoMessages.DemoResponse> {

  private Channel channel;
  private DemoMessages.DemoResponse resp;
  private final BlockingQueue<DemoMessages.DemoResponse> resps = new LinkedBlockingQueue<DemoMessages.DemoResponse>();
  public DemoMessages.DemoResponse sendRequest(DemoMessages.Type type) {

      DemoMessages.DemoRequest req = null;
      // send File request
      if (DemoMessages.Type.FILE == type) {
          InputStream inputStream = null;
          try {
              inputStream = getClass().getResourceAsStream("/components.png");

              DemoMessages.FileMsg fileMsg = DemoMessages.FileMsg.newBuilder()
                      .setFileBytes(ByteString.readFrom(inputStream))
                      .setFilename("components.png")
                      .build();
              req = DemoMessages.DemoRequest.newBuilder()
                      .setType(DemoMessages.Type.FILE)
                      .setFile(fileMsg)
                      .build();
              // Send request
              channel.writeAndFlush(req);
          } catch (Exception e) {
              e.printStackTrace();
          } finally {
              try {
                  if (inputStream != null) {
                      inputStream.close();
                  }
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
      } else {
          // send message request.
           req = DemoMessages.DemoRequest.newBuilder()
                  .setType(DemoMessages.Type.MSG)
                  .setRequestMsg("From Client").build();
      }
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