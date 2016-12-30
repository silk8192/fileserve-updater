package com.github.fileserve.client;

import com.github.fileserve.FileRepository;
import com.github.fileserve.UpdateTable;
import com.github.fileserve.net.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OutboundRequestHandler extends SimpleChannelInboundHandler<Response> {

    private static final Logger logger = LogManager.getLogger();
    private ChunkReceiverPool chunkReceiverPool;
    private FileRequesterService fileRequesterService;
    private UpdateTable updateTable;

    public OutboundRequestHandler(FileRepository fileRepository) {
        this.updateTable = fileRepository.getUpdateTable();
        this.chunkReceiverPool = new ChunkReceiverPool(fileRepository);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        logger.info("Sending requests...");
        this.fileRequesterService = new FileRequesterService(updateTable, ctx.channel());
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
