package com.github.fileserve.client;

import com.github.fileserve.net.decoder.ResponseDecoder;
import com.github.fileserve.net.encoder.RequestEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final String cachePath;

    public ClientChannelInitializer(String cachePath) {
        this.cachePath = cachePath;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new InboundUpdateTableHandler(cachePath));
        pipeline.addLast(new RequestEncoder());
        pipeline.addLast(new ResponseDecoder());
    }

}
