package com.github.fileserve;

import java.nio.ByteBuffer;

public class FileReference {

    private int id;
    private String name;
    private long crc;
    private long fileSize;

    /**
     * Creates a {@link FileReference} object which is used to describe the attributes of the file.
     * @param name The name of the file, may include a relative directory if the file exists in a sub-directory.
     */
    public FileReference(int id, String name, long crc, long fileSize) {
        this.id = id;
        this.name = name;
        this.crc = crc;
        this.fileSize = fileSize;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCrc() {
        return crc;
    }

    public void setCrc(long crc) {
        this.crc = crc;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public byte[] toBytes() {
        //Length of file name, space neded for crc and filesize
        int totalLength = Integer.BYTES + Integer.BYTES + Long.BYTES + Long.BYTES + name.getBytes().length;
        ByteBuffer reference = ByteBuffer.allocate(totalLength);
        reference.putInt(id);
        reference.putInt(totalLength);
        reference.putLong(fileSize);
        reference.putLong(crc);
        reference.put(name.getBytes());
        return (byte[]) reference.flip().array();
    }

}
