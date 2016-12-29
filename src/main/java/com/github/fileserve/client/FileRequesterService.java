package com.github.fileserve.client;

import com.github.fileserve.UpdateTable;
import com.github.fileserve.net.Request;
import io.netty.channel.Channel;

public class FileRequesterService extends Thread {

    private UpdateTable updateTable;
    private Channel channel;

    FileRequesterService(UpdateTable updateTable, Channel channel) {
        this.updateTable = updateTable;
        this.channel = channel;
    }

    @Override
    public void run() {
        boolean isRunning = true;
        while(isRunning) {
            channel.eventLoop().submit(() -> {
                for(int i = 0; i < updateTable.getFileReferences().size(); i++) {
                    channel.writeAndFlush(new Request(i, (byte) 1));
                }
            });
            //finally stop the FileRequester thread
            isRunning = false;
        }
    }

    public void start() {
        super.start();
    }

}
