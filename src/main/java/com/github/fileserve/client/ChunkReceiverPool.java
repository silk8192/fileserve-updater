package com.github.fileserve.client;

import com.github.fileserve.fs.FileRepository;
import com.github.fileserve.net.Response;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

public class ChunkReceiverPool {

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Table<Integer, Short, Response> responses = HashBasedTable.create();
    private FileRepository fileRepository;
    private static final Logger logger = LogManager.getLogger();

    public ChunkReceiverPool(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public void submit(Response response) {
        responses.put(response.getFileId(), response.getChunkId(), response);
        //Check if this chunk is the final chunk, then remove from table for finalizing the file.
        if (response.getChunkId() == response.getFinalChunk()) {
            executorService.submit(() -> {
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
                byte[] decompressed = decompress(file);
                store(response.getFileId(), decompressed);
                responses.row(response.getFileId()).clear();
                logger.info("Received file: " + response.getFileId());
            });
        }
    }

    /**
     * This method stores files as uncompressed files into the client cache directory.
     * @param fileId The id of the file being stored, used for referencing with the {@link FileRepository}.
     * @param fileData The raw file data.
     */
    private void store(int fileId, byte[] fileData) {
        try {
            File newFile = Paths.get(fileRepository.getRepositoryPath().toString(),
                    fileRepository.getUpdateTable().getFileReferences().get(fileId).getName()).toFile();
            newFile.getParentFile().mkdirs();
            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(newFile))) {
                dos.write(fileData);
            }
        } catch (IOException e) {
            logger.catching(e);
        }
    }

    public static byte[] decompress(final byte[] input) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ByteArrayInputStream bin = new ByteArrayInputStream(input);
             GZIPInputStream gzipper = new GZIPInputStream(bin)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipper.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            gzipper.close();
            out.close();
        } catch (Exception e) {
            logger.catching(e);
        } finally {
            return out.toByteArray();
        }
    }

}
