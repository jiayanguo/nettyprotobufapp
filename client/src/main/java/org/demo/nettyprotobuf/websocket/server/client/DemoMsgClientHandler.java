package org.demo.nettyprotobuf.websocket.server.client;

import com.google.protobuf.ByteString;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.demo.nettyprotobuf.proto.DemoMessages;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class DemoMsgClientHandler extends SimpleChannelInboundHandler<Object> {

    private Channel channel;
    private byte[] resp;
    private final BlockingQueue<byte[]> resps = new LinkedBlockingQueue<byte[]>();
    private WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;

    public DemoMsgClientHandler(WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    public byte[] sendRequest(DemoMessages.Type type) {

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
        for (; ; ) {
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
    public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        channel = ctx.channel();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        final Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            // web socket client connected
            handshaker.finishHandshake(ch, (FullHttpResponse) msg);
            handshakeFuture.setSuccess();
            return;
        }

        WebSocketFrame frame = (WebSocketFrame) msg;
        BinaryWebSocketFrame binframe = (BinaryWebSocketFrame) frame;
        byte[] bytes = new byte[binframe.content().readableBytes()];
        binframe.content().readBytes(bytes);
        resps.add(bytes);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }
}