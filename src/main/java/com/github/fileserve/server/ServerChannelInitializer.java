package com.github.fileserve.server;

import com.github.fileserve.FileRepository;
import com.github.fileserve.net.decoder.RequestDecoder;
import com.github.fileserve.net.encoder.ResponseEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A {@link ChannelInitializer} for the service pipeline.
 */
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final Logger logger = LogManager.getLogger();

    private final FileRepository fileRepository;

    public ServerChannelInitializer(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new RequestDecoder());
        pipeline.addLast(new ResponseEncoder());
        pipeline.addLast("handler", new InboundRequestHandler(fileRepository));
        logger.info("Connection received from " + ch.remoteAddress().getAddress());
    }

}