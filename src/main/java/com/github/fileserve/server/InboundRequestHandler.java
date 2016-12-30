package com.github.fileserve.server;

import com.github.fileserve.FileRepository;
import com.github.fileserve.net.Request;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;

public class InboundRequestHandler extends SimpleChannelInboundHandler<Request> {

    private final Logger logger = LogManager.getLogger();

    private final FileRepository fileRepository;

    private final ChunkDispatcherPool chunkDispatcherPool;

    public InboundRequestHandler(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
        chunkDispatcherPool = new ChunkDispatcherPool(fileRepository);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Connection activated from: " + InetAddress.getLocalHost().toString());
        byte[] buffer = fileRepository.getIndexData();
        ByteBuf buf = ctx.alloc().buffer(Integer.BYTES + buffer.length );
        buf.writeInt(buffer.length);
        buf.writeBytes(buffer);
        ctx.channel().writeAndFlush(buf);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Request request) throws Exception {
        chunkDispatcherPool.submit(request, channelHandlerContext);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.catching(cause);
        chunkDispatcherPool.halt();
        ctx.close();
    }
}