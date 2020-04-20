package org.demo.nettyprotobuf.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.demo.nettyprotobuf.proto.DemoMessages;

import java.io.FileOutputStream;
import java.io.IOException;

public class DemoProtocolServerHandler extends SimpleChannelInboundHandler<DemoMessages.DemoRequest> {
  private static final String FILE_DIR = "/tmp/";

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, DemoMessages.DemoRequest msg) {
    if (msg.getType() == DemoMessages.Type.MSG) {
      DemoMessages.DemoResponse.Builder builder = DemoMessages.DemoResponse.newBuilder();
      String message = "Accepted from Server, returning response";
      System.out.println(message);
      builder.setResponseMsg(message)
              .setCode(0);
      ctx.write(builder.build());
    } else if (msg.getType() == DemoMessages.Type.FILE) {

      byte[] bFile = msg.getFile().toByteArray();
      FileOutputStream fileOuputStream = null;
      try {
        fileOuputStream = new FileOutputStream(FILE_DIR + msg.getFile().getFilename());
        fileOuputStream.write(bFile);
      } catch (Exception e) {
        System.out.println(e);
      }finally {
        try {
          if (fileOuputStream != null) {
            fileOuputStream.close();
          }
        } catch (IOException e) {
          System.out.println(e);
        }
      }
      DemoMessages.DemoResponse.Builder builder = DemoMessages.DemoResponse.newBuilder();
      String message = "File saved to: " + FILE_DIR;
      System.out.println(message);
      builder.setResponseMsg(message)
              .setCode(0);
      ctx.write(builder.build());
    } else {
      DemoMessages.DemoResponse.Builder builder = DemoMessages.DemoResponse.newBuilder();
      String message = "Unsupported message type " + msg.getType();
      System.out.println(message);
      builder.setResponseMsg(message)
              .setCode(1);
      ctx.write(builder.build());
    }
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