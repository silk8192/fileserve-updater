package com.github.fileserve.server;

import com.github.fileserve.FileRepository;
import com.github.fileserve.net.Request;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;

public class InboundRequestHandler extends SimpleChannelInboundHandler<Request> {

    private final Logger logger = LogManager.getLogger();

    private final ChunkDispatcherPool chunkDispatcherPool;

    public InboundRequestHandler(FileRepository fileRepository) {
        chunkDispatcherPool = new ChunkDispatcherPool(fileRepository);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Connection activated from: " + InetAddress.getLocalHost().toString());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Request request) throws Exception {
        chunkDispatcherPool.submit(request);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.catching(cause);
        chunkDispatcherPool.halt();
        ctx.close();
    }
}