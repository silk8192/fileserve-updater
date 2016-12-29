package com.github.fileserve.net.decoder;

import com.github.fileserve.net.Request;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class RequestDecoder extends ReplayingDecoder<Request> {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        int fileId = in.readInt();
        byte priority = in.readByte();
        out.add(new Request(fileId, priority, channelHandlerContext.channel()));
    }

}
