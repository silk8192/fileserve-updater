package com.github.fileserve;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;

public class UpdateTable {

    private final Logger logger = LogManager.getLogger();

    private final ArrayList<FileReference> fileReferences;

    public UpdateTable() {
        this.fileReferences = new ArrayList<>();
    }

    public void parse(byte[] data) throws IOException {
        try (DataInputStream bais = new DataInputStream(new ByteArrayInputStream(data))) {
            do {
                int id = bais.readInt();
                int totalBlockLength = bais.readInt();
                long fileSize = bais.readLong();
                long crc = bais.readLong();
                int nameLength = totalBlockLength - (Long.BYTES + Long.BYTES + Integer.BYTES + Integer.BYTES);
                byte[] name = new byte[nameLength];
                bais.readFully(name);
                String fileName = new String(name, StandardCharsets.UTF_8);
                fileReferences.add(id, new FileReference(id, fileName, crc, fileSize));
            } while (bais.available() > 0);
        }
    }

    public void generateTable(Path cachePath, File indexFile, ArrayList<File> files) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); DataOutputStream dos = new DataOutputStream(new FileOutputStream(indexFile))) {
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                String name;
                long crc = FileUtils.checksumCRC32(file);
                long fileSize = file.length();
                String subDirectory = file.getParent().replace(cachePath.toString(), "");
                if (subDirectory.isEmpty()) {
                    name = file.getName().replace("\\", "");
                } else {
                    name = subDirectory.replace("\\", "") + "\\" + file.getName();
                }
                FileReference fileRef = new FileReference(i, name, crc, fileSize);
                baos.write(fileRef.toBytes());
            }
            dos.write(baos.toByteArray());
        } catch (IOException e) {
            logger.catching(e);
        }
    }

    public long getCRC(int fileId) {
        return this.getFileReferences().get(fileId).getCrc();
    }

    public ArrayList<FileReference> getFileReferences() {
        return fileReferences;
    }

}
