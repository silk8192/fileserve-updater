package com.github.fileserve.client;

import com.github.fileserve.FileRepository;
import com.github.fileserve.net.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OutboundRequestHandler extends SimpleChannelInboundHandler<Response> {

    private static final Logger logger = LogManager.getLogger();
    private ChunkReceiverPool chunkReceiverPool;
    private FileRepository fileRepository;

    public OutboundRequestHandler(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
        this.chunkReceiverPool = new ChunkReceiverPool(fileRepository);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        logger.info("Sending requests...");
        FileRequesterService fileRequesterService = new FileRequesterService(fileRepository, ctx.channel());
        fileRequesterService.doStart();
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
