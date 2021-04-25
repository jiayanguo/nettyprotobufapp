package org.demo.nettyprotobuf.websocket.server.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.demo.nettyprotobuf.proto.DemoMessages;

import java.net.URI;


public class DemoClient {

    static final String HOST;
    static final int PORT;

    static {
        if (System.getenv("port") == null) {
            PORT = 8080;
        } else {
            PORT = Integer.parseInt(System.getenv("port"));
        }

        if (System.getenv("host") == null) {
            HOST = "127.0.0.1";
        } else {
            HOST = System.getenv("host");
        }
    }

    public static void main(String[] args) {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            URI websocketURI = new URI(String.format("ws://%s:%d/ws", HOST, PORT));
            HttpHeaders httpHeaders = new DefaultHttpHeaders();
            final WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(websocketURI, WebSocketVersion.V13, "PROTOBUFF", true, httpHeaders);
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .handler(new DemoClientInitializer(handshaker));

            // Create connection
            Channel c = bootstrap.connect(websocketURI.getHost(), websocketURI.getPort()).sync().channel();
            System.out.println("----channel handshake:");
            DemoMsgClientHandler handler = c.pipeline().get(DemoMsgClientHandler.class);
            ChannelFuture future = handler.handshakeFuture();

            while (!future.isSuccess()) {
                Thread.sleep(1000);
                System.out.println("handshake is not successful yet");
                if (handshaker.isHandshakeComplete()) {
                    System.out.println("handshake is completed!");
                } else {
                    System.out.println("handshake is not completed yet!");
                }
            }

            System.out.println("----channel handshake succeeded.");
            c = future.sync().channel();

            if (!c.isActive()) {
                System.out.println("Channel is not active");
                return;
            } else {
                System.out.println("Channel is active");
                c.flush();
            }
            int i = 0;
            while (i++ < 10) {
                DemoMessages.DemoResponse resp = DemoMessages.DemoResponse.parseFrom(handler.sendRequest(DemoMessages.Type.MSG));
                System.out.println("Got response msg from Server: " + resp.getResponseMsg());
                Thread.sleep(1000);
            }

            DemoMessages.DemoResponse resp = DemoMessages.DemoResponse.parseFrom(handler.sendRequest(DemoMessages.Type.FILE));
            System.out.println("Got response msg from Server: " + resp.getResponseMsg());

            c.close();


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }

    }
}