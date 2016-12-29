package com.github.fileserve.client;

import com.github.fileserve.UpdateTable;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class InboundUpdateTableHandler extends ByteToMessageDecoder {

    private static Logger logger = LogManager.getLogger();

    private final UpdateTable updateTable;

    public InboundUpdateTableHandler() {
        updateTable = new UpdateTable();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        logger.info("Connected to server!");
    }


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {
        logger.info("Receiving reference table...");
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        updateTable.parse(bytes);
        logger.info("Received " + updateTable.getFileReferences().size() + " file references!");
        ctx.pipeline().remove(this);
        ctx.pipeline().addLast(new OutboundRequestHandler(updateTable));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.catching(cause);
    }

}
