package com.github.fileserve.client;

import com.github.fileserve.FileRepository;
import com.github.fileserve.net.Request;
import com.google.common.util.concurrent.AbstractService;
import io.netty.channel.Channel;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

public class FileRequesterService extends AbstractService implements Runnable {

    private final ExecutorService service = Executors.newSingleThreadExecutor();
    private FileRepository fileRepository;
    private Channel channel;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private Logger logger = LogManager.getLogger();

    FileRequesterService(FileRepository fileRepository, Channel channel) {
        this.fileRepository = fileRepository;
        this.channel = channel;
    }

    @Override
    public void run() {
        while (running.get()) {
            channel.eventLoop().submit(() -> {
                for (int fileId = 0; fileId < this.fileRepository.getUpdateTable().getFileReferences().size(); fileId++) {
                    logger.info("Requesting file: " + fileId);
                    channel.writeAndFlush(new Request(fileId, (byte) 1));
                }
            });
            //finally stop the FileRequester thread
            running.set(false);
        }
    }

    @Override
    protected void doStart() {
        service.submit(this);
    }

    @Override
    protected void doStop() {
        running.set(false);
        service.shutdownNow();
    }
}
