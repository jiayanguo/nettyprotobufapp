package org.demo.nettyprotobuf.server;

import org.demo.nettyprotobuf.proto.DemoMessages;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

public class DemoServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // Why this order, decoder, encoder, handler?
        // Note: Every IO operation on a Channel in Netty is non-blocking.
        // This means that every operation is returned immediately after the call.
        // When received message uses decoder, and then response needs encoder,
        // then handling the requests uses handler.
        ChannelPipeline p = ch.pipeline();
        p.addLast(new ProtobufVarint32FrameDecoder());
        p.addLast(new ProtobufDecoder(DemoMessages.DemoRequest.getDefaultInstance()));

        p.addLast(new ProtobufVarint32LengthFieldPrepender());
        p.addLast(new ProtobufEncoder());

        p.addLast(new DemoProtocolServerHandler());
    }

}
