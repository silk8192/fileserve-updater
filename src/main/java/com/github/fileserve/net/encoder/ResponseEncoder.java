package com.github.fileserve.net.encoder;

import com.github.fileserve.net.Response;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ResponseEncoder extends MessageToByteEncoder<Response> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Response response, ByteBuf out) throws Exception {
        out.writeInt(response.getFileId());
        out.writeShort(response.getChunkId());
        out.writeInt(response.getFinalChunk());
        out.writeBytes(response.getChunk());
    }

}
