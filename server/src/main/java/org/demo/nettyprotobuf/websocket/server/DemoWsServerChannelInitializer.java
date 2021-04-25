package org.demo.nettyprotobuf.websocket.server;

import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLiteOrBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.demo.nettyprotobuf.proto.DemoMessages;

import java.util.List;
import java.util.logging.Logger;

import static io.netty.buffer.Unpooled.wrappedBuffer;

public class DemoWsServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    Logger log = Logger.getLogger(DemoWsServerChannelInitializer.class.getName());

    @Override
    protected void initChannel(final SocketChannel ch) {

        System.out.println("initialize ... ");
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new LoggingHandler());
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new WebSocketServerCompressionHandler());
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws", "PROTOBUFF", true));

        pipeline.addLast(new ProtobufVarint32FrameDecoder());
        pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());

        pipeline.addLast(new MessageToMessageDecoder<WebSocketFrame>() {
            @Override
            protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> objs) throws Exception {
                System.out.println("MessageToMessageDecoder msg ------------------------");
                if (frame instanceof TextWebSocketFrame) {
                    TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
                    log.info("TextWebSocketFrame");
                } else if (frame instanceof BinaryWebSocketFrame) {
                    ByteBuf buf = ((BinaryWebSocketFrame) frame).content();
                    objs.add(buf);
                    buf.retain();
                    log.info("BinaryWebSocketFrame received------------------------");
                } else if (frame instanceof PongWebSocketFrame) {
                    log.info("WebSocket Client received pong");
                } else if (frame instanceof CloseWebSocketFrame) {
                    log.info("receive close frame");
                }
            }
        });

        pipeline.addLast(new MessageToMessageEncoder<MessageLiteOrBuilder>() {
            @Override
            protected void encode(ChannelHandlerContext ctx, MessageLiteOrBuilder msg, List<Object> out) throws Exception {
                ByteBuf result = null;
                if (msg instanceof MessageLite) {
                    result = wrappedBuffer(((MessageLite) msg).toByteArray());
                }
                if (msg instanceof MessageLite.Builder) {
                    result = wrappedBuffer(((MessageLite.Builder) msg).build().toByteArray());
                }

                WebSocketFrame frame = new BinaryWebSocketFrame(result);
                out.add(frame);
            }
        });


        pipeline.addLast(new ProtobufDecoder(DemoMessages.DemoRequest.getDefaultInstance()));

        pipeline.addLast(new DemoProtocolServerHandler());

    }

}
