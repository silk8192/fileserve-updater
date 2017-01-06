package com.github.fileserve.client;

import com.github.fileserve.fs.FileRepository;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;
import java.util.List;

public class InboundUpdateTableHandler extends ReplayingDecoder {

    private static Logger logger = LogManager.getLogger();

    private final String cachePath;

    public InboundUpdateTableHandler(String cachePath) {
        this.cachePath = cachePath;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        logger.info("Connected to server!");
    }


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {
        logger.info("Receiving reference table...");
        byte[] bytes = new byte[byteBuf.readInt()];
        byteBuf.readBytes(bytes);
        FileRepository fileRepository = new FileRepository(Paths.get(cachePath), bytes);
        logger.info("Received " + fileRepository.getUpdateTable().getFileReferences().size() + " file references!");
        ctx.pipeline().remove(this);
        ctx.pipeline().addLast(new OutboundRequestHandler(fileRepository));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.catching(cause);
    }

}
