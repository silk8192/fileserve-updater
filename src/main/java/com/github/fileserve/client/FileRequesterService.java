package com.github.fileserve.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileRequesterService extends Thread {

    ExecutorService service = Executors.newSingleThreadExecutor();

    FileRequesterService() {

    }

    @Override
    public void run() {

    }

    public void start() {
        super.start();
    }

}
