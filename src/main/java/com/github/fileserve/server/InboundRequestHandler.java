package com.github.fileserve.server;

import com.github.fileserve.fs.FileRepository;
import com.github.fileserve.net.Request;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;

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
        logger.info("Connection activated from: " + ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress());
        byte[] buffer = fileRepository.getIndexData();
        ByteBuf buf = ctx.alloc().buffer(Integer.BYTES + buffer.length );
        buf.writeInt(buffer.length);
        buf.writeBytes(buffer);
        ctx.channel().writeAndFlush(buf).addListener((ChannelFutureListener) channelFuture -> {
            if(channelFuture.isSuccess()) {
                logger.info("Sent update table data!");
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Connection closed from: " + ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress());
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