package com.github.fileserve.client;

import com.github.fileserve.net.Response;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

public class ChunkReceiverPool {

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private Table<Integer, Short, Response> responses = HashBasedTable.create();

    private static final Logger logger = LogManager.getLogger();

    public void submit(Response response) {
        responses.put(response.getFileId(), response.getChunkId(), response);
        //Check if this chunk is the final chunk, then remove from table for finalizing the file.
        if (response.getChunkId() == response.getFinalChunk()) {
            executorService.submit(() -> {
                long startTime = System.nanoTime();
                //perform checks to see if all chunks are received.
                List<Response> fileResponses = Lists.newArrayList();
                responses.row(response.getFileId()).entrySet().forEach(i -> fileResponses.add(i.getValue()));
                Collections.sort(fileResponses, Comparator.comparing(Response::getChunkId));
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                try {
                    for (int i = 0; i < response.getFinalChunk(); i++) {
                        os.write(fileResponses.get(i).getChunk());
                    }
                    os.close();
                } catch (Exception e) {
                    logger.catching(e);
                }
                byte[] file = os.toByteArray();
                try {
                    catchFile(decompress(file));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                responses.row(response.getFileId()).clear();
                logger.info((System.nanoTime() - startTime)/1000000);
            });
        }
    }

    private void catchFile(byte[] decompressedFile) {
        logger.info(new String(decompressedFile));
    }

    public static byte[] decompress(final byte[] input) throws Exception{
        try (ByteArrayInputStream bin = new ByteArrayInputStream(input);
             GZIPInputStream gzipper = new GZIPInputStream(bin)) {
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int len;
            while ((len = gzipper.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            gzipper.close();
            out.close();
            return out.toByteArray();
        }
    }

}
