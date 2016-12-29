package com.github.fileserve.net.decoder;

import com.github.fileserve.NetworkConstant;
import com.github.fileserve.net.Response;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class ResponseDecoder extends ReplayingDecoder<Response> {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf buf, List<Object> list) throws Exception {
        int fileId = buf.readInt();
        short chunkId = buf.readShort();
        int finalChunk = buf.readInt();
        byte[] bytes = new byte[NetworkConstant.CHUNK_LENGTH];
        buf.readBytes(bytes);
        Response response = new Response(fileId, chunkId, finalChunk, bytes);
        list.add(response);
    }

}
