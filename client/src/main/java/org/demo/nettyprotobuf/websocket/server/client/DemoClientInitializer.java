package org.demo.nettyprotobuf.websocket.server.client;

import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLiteOrBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.Log4J2LoggerFactory;
import org.demo.nettyprotobuf.proto.DemoMessages;

import java.util.List;

import static io.netty.buffer.Unpooled.wrappedBuffer;

public class DemoClientInitializer extends ChannelInitializer<SocketChannel> {
    InternalLogger log = Log4J2LoggerFactory.getInstance(DemoClientInitializer.class);
    private WebSocketClientHandshaker handshaker;

    public DemoClientInitializer(WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new HttpClientCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new ProtobufVarint32FrameDecoder());
        pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
        pipeline.addLast(new DemoMsgClientHandler(handshaker));
        pipeline.addLast(new ProtobufDecoder(DemoMessages.DemoResponse.getDefaultInstance()));
        pipeline.addLast(new MessageToMessageDecoder<WebSocketFrame>() {
            @Override
            protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame,
                                  List<Object> objs) throws Exception {
                if (frame instanceof TextWebSocketFrame) {
                    TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
                } else if (frame instanceof BinaryWebSocketFrame) {
                    ByteBuf buf = ((BinaryWebSocketFrame) frame).content();
                    objs.add(buf);
                    buf.retain();
                } else if (frame instanceof PongWebSocketFrame) {
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
    }

}