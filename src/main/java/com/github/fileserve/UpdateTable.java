package com.github.fileserve;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.zip.CRC32;

public class UpdateTable {

    private final Logger logger = LogManager.getLogger();
    private ArrayList<FileReference> fileReferences;
    private FileRepository fileRepository;

    public UpdateTable(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
        fileReferences = new ArrayList<>(fileRepository.getNumberOfFiles());
    }

    public void parse() throws IOException {
        byte[] allData = FileUtils.readFileToByteArray(fileRepository.getIndexFile());
        try (DataInputStream bais = new DataInputStream(new ByteArrayInputStream(allData))) {
            while (bais.available() > 0) {
                int id = bais.readInt();
                int totalBlockLength = bais.readInt();
                long fileSize = bais.readLong();
                long crc = bais.readLong();
                int nameLength = totalBlockLength - (Long.BYTES + Long.BYTES + Integer.BYTES + Integer.BYTES);
                byte[] name = new byte[nameLength];
                bais.readFully(name);
                String fileName = new String(name, StandardCharsets.UTF_8);
                fileReferences.add(id - 1, new FileReference(id, fileName, crc, fileSize));
            }
        }
    }

    public void generateTable() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); DataOutputStream dos = new DataOutputStream(new FileOutputStream(fileRepository.getIndexFile()))) {
            for (int i = 0; i < fileRepository.getFiles().size(); i++) {
                File file = fileRepository.getFiles().get(i);
                if (file.isDirectory())
                    return;
                String name;
                long crc = 0, fileSize;
                try (InputStream in = new FileInputStream(file)) {
                    CRC32 crcMaker = new CRC32();
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        crcMaker.update(buffer, 0, bytesRead);
                    }
                    crc = crcMaker.getValue();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                String subDirectory = file.getParent().replace(fileRepository.getRepositoryPath().toString(), "");
                if (subDirectory.isEmpty()) {
                    name = file.getName().replace("\\", "");
                } else {
                    name = subDirectory.replace("\\", "") + "\\" + file.getName();
                }
                fileSize = file.length();
                FileReference fileRef = new FileReference(i + 1, name, crc, fileSize);
                baos.write(fileRef.toBytes());
            }
            dos.write(baos.toByteArray());
        } catch (IOException e) {
            logger.catching(e);
        }
    }

}
