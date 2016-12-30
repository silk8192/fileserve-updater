package com.github.fileserve.client;

import com.github.fileserve.FileRepository;
import com.github.fileserve.net.Request;
import io.netty.channel.Channel;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.stream.IntStream;

public class FileRequesterService extends Thread {

    private FileRepository fileRepository;
    private Channel channel;
    private Logger logger = LogManager.getLogger();

    FileRequesterService(FileRepository fileRepository, Channel channel) {
        this.fileRepository = fileRepository;
        this.channel = channel;
    }

    @Override
    public void run() {
        boolean isRunning = true;
        while(isRunning) {
            IntStream.range(0, fileRepository.getUpdateTable().getFileReferences().size()).forEach(file -> {
                channel.eventLoop().submit(() -> {
                    try {
                        if(FileUtils.checksumCRC32(fileRepository.locate(file).get()) != fileRepository.getCRC(file)) {
                            logger.info("Requesting file: " + file);
                            channel.writeAndFlush(new Request(file, (byte) 1));
                        }
                    } catch (IOException e) {
                        logger.catching(e);
                    }
                });
            });
            //finally stop the FileRequester thread
            isRunning = false;
        }
    }

    public void start() {
        super.start();
    }

}
