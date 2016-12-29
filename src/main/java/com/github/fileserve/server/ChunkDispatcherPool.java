package com.github.fileserve.server;

import com.github.fileserve.FileRepository;
import com.github.fileserve.net.Request;
import com.github.fileserve.net.Response;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.zip.GZIPOutputStream;

public class ChunkDispatcherPool {

    private final Logger logger = LogManager.getLogger();
    private final ExecutorService executorService;
    private final FileRepository fileRepository;
    private final PriorityBlockingQueue<Request> requests = new PriorityBlockingQueue<>();
    private static final int MAX_SERVICEABLE = 2056;
    private static final int CHUNK_LENGTH = 512;
    private boolean isStopped = false;

    public ChunkDispatcherPool(FileRepository fileRepository) {
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.fileRepository = fileRepository;
    }

    public void submit(Request request, ChannelHandlerContext ctx) {
        if (isStopped) {
            return;
        }
        if (requests.size() >= MAX_SERVICEABLE) {
            logger.error("Max serviceable number of request reached!");
            return;
        }
        requests.add(request);
        executorService.execute(() -> {
            Request r = requests.poll();
            logger.info("File Requested:  " + r.getFileId() + "\t" + "\t" + r.getPriority().toInteger());
            sendFile(request, ctx);
        });
    }


    /**
     * Handles gzip compression and breakup of file into chunks.
     *
     * @param request The {@link Request} to respond to with chunks of a file.
     */
    private void sendFile(Request request, ChannelHandlerContext ctx) {
        int fileId = request.getFileId();

        /*
          TODO: Possible error when the file is too big.
         */
        try {
            File file = fileRepository.locate(fileId).orElseThrow(() -> new NullPointerException("Unable to process file: " + fileId));
            ByteSource source = Files.asByteSource(file);
            byte[] uncompressed = source.read();
            ByteArrayOutputStream byteStream =
                    new ByteArrayOutputStream(uncompressed.length);

            try (GZIPOutputStream zipStream = new GZIPOutputStream(byteStream)) {
                zipStream.write(uncompressed);
            }

            byte[] compressedData = byteStream.toByteArray();

            List<byte[]> chunks = Arrays.asList(generateChunks(compressedData));
            int chunkId = 0;
            int finalChunk = chunks.size();
            for (byte[] chunk : chunks) {
                executorService.submit(new DispatchTask(fileId, (short) ++chunkId, finalChunk, chunk, ctx));
            }
        } catch (IOException | NullPointerException e) {
            logger.catching(e);
        }
    }

    private static byte[][] generateChunks(byte[] source) {
        byte[][] ret = new byte[(int) Math.ceil(source.length / (double) CHUNK_LENGTH)][CHUNK_LENGTH];
        int start = 0;
        for (int i = 0; i < ret.length; i++) {
            ret[i] = Arrays.copyOfRange(source, start, start + CHUNK_LENGTH);
            start += CHUNK_LENGTH;
        }
        return ret;
    }

    public void halt() {
        isStopped = true;
        executorService.shutdown();
    }

    private class DispatchTask implements Runnable {

        private final int fileId;
        private final short chunkId;
        private final byte[] chunk;
        private final ChannelHandlerContext ctx;
        private int finalChunk;

        public DispatchTask(int fileId, short chunkId, int finalChunk, byte[] chunk, ChannelHandlerContext ctx) {
            this.fileId = fileId;
            this.chunkId = chunkId;
            this.finalChunk = finalChunk;
            this.chunk = chunk;
            this.ctx = ctx;
        }

        @Override
        public void run() {
            ctx.writeAndFlush(new Response(fileId, chunkId, finalChunk, chunk));
        }
    }

}