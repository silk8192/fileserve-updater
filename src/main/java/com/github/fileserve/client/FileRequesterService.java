package com.github.fileserve.client;

import com.github.fileserve.FileRepository;
import com.github.fileserve.net.Request;
import com.google.common.util.concurrent.AbstractService;
import io.netty.channel.Channel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileRequesterService extends AbstractService implements Runnable {

    private final ExecutorService service = Executors.newSingleThreadExecutor();
    private FileRepository fileRepository;
    private Channel channel;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private Logger logger = LogManager.getLogger();
    private final PriorityBlockingQueue<Request> pendingRequests = new PriorityBlockingQueue<>();

    FileRequesterService(FileRepository fileRepository, Channel channel) {
        this.fileRepository = fileRepository;
        this.channel = channel;
    }

    @Override
    public void run() {
        while (running.get()) {
            while(!pendingRequests.isEmpty()) {
                channel.writeAndFlush(pendingRequests.poll());
            }
            doStop();
        }
    }

    @Override
    protected void doStart() {
        for (int fileId = 0; fileId < this.fileRepository.getUpdateTable().getFileReferences().size(); fileId++) {
            logger.info("Requesting file: " + fileId);
            pendingRequests.put(new Request(fileId, (byte) 1));
        }
        service.submit(this);
    }

    @Override
    protected void doStop() {
        running.set(false);
        service.shutdownNow();
    }
}
