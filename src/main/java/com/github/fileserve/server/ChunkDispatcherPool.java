package com.github.fileserve.server;

import com.github.fileserve.FileRepository;
import com.github.fileserve.NetworkConstant;
import com.github.fileserve.net.Request;
import com.github.fileserve.net.Response;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
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
    private final int MAX_SERVICABLE = 2056;
    private boolean isStopped = false;

    public ChunkDispatcherPool(FileRepository fileRepository) {
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.fileRepository = fileRepository;
    }

    private static byte[][] generateChunks(byte[] source, int chunksize) {
        byte[][] ret = new byte[(int) Math.ceil(source.length / (double) chunksize)][chunksize];
        int start = 0;
        for (int i = 0; i < ret.length; i++) {
            ret[i] = Arrays.copyOfRange(source, start, start + chunksize);
            start += chunksize;
        }
        return ret;
    }


    public void submit(Request request) {
        if (isStopped) {
            return;
        }
        if (requests.size() >= MAX_SERVICABLE) {
            logger.error("Max serviceable number of request reached!");
            return;
        }
        requests.add(request);
        executorService.execute(() -> {
            Request r = requests.poll();
            logger.info("File Requested:  " + r.getFileId() + "\t" + r.getChannel().remoteAddress() + "\t" + r.getPriority().toInteger());
            sendFile(request);
        });
    }

    /**
     * Handles gzip compression and breakup of file into chunks.
     *
     * @param request The {@link Request} to respond to with chunks of a file.
     */
    private void sendFile(Request request) {
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

            int chunkId = 0;
            List<byte[]> chunks = Arrays.asList(generateChunks(compressedData, NetworkConstant.CHUNK_LENGTH));
            int finalChunk = chunks.size();
            for (byte[] chunk : chunks) {
                executorService.submit(new DispatchTask(fileId, (short) ++chunkId, finalChunk, chunk, request));
            }
        } catch (IOException | NullPointerException e) {
            logger.catching(e);
        }
    }

    public void halt() {
        isStopped = true;
        executorService.shutdown();
    }

    private class DispatchTask implements Runnable {

        private final int fileId;
        private final short chunkId;
        private final byte[] chunk;
        private final Request request;
        private int finalChunk;

        public DispatchTask(int fileId, short chunkId, int finalChunk, byte[] chunk, Request request) {
            this.fileId = fileId;
            this.chunkId = chunkId;
            this.finalChunk = finalChunk;
            this.chunk = chunk;
            this.request = request;
        }

        @Override
        public void run() {
            request.getChannel().writeAndFlush(new Response(fileId, chunkId, finalChunk, chunk));
        }
    }

}