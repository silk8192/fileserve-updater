package com.github.fileserve.net;

/**
 * A single file may be split up into multiple responses. The client receives these responses and constructs a single file.
 */
public class Response {

    private final int fileId;

    private final short chunkId;

    private int finalChunk;

    private byte[] chunk;

    public Response(int fileId, short chunkId, int finalChunk, byte[] chunk) {
        this.fileId = fileId;
        this.chunkId = chunkId;
        this.finalChunk = finalChunk;
        this.chunk = chunk;
    }

    public int getFileId() {
        return fileId;
    }

    public short getChunkId() {
        return chunkId;
    }


    public byte[] getChunk() {
        return chunk;
    }

    @Override
    public String toString() {
        return "Response{" + "fileId=" + fileId + ", chunkId=" + chunkId + '}';
    }

    public int getFinalChunk() {
        return finalChunk;
    }

}
