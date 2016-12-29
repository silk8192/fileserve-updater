package com.github.fileserve.client;

import com.github.fileserve.net.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OutboundRequestHandler extends SimpleChannelInboundHandler<Response> {

    private static final Logger logger = LogManager.getLogger();

    ChunkReceiverPool chunkReceiverPool = new ChunkReceiverPool();

    FileRequesterService fileRequesterService = new FileRequesterService();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Connected to server! Sending requests...");
        fileRequesterService.start();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Response response) throws Exception {
        chunkReceiverPool.submit(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.catching(cause);
        ctx.close();
    }
}
