package com.github.fileserve.net.encoder;

import com.github.fileserve.net.Request;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RequestEncoder extends MessageToByteEncoder<Request> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Request request, ByteBuf out) throws Exception {
        out.writeInt(request.getFileId());
        out.writeByte(request.getPriority().toInteger());
    }

}
