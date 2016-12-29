package com.github.fileserve.net;

/**
 * A one time request for a file. Requests will be replied to based on priority.
 */
public class Request implements Comparable<Request> {

    private final int fileId;
    private final Priority priority;

    public Request(int fileId, byte priority) {
        this.fileId = fileId;
        this.priority = Priority.valueOf(priority);
    }

    public int getFileId() {
        return fileId;
    }

    public Priority getPriority() {
        return priority;
    }

    @Override
    public int compareTo(Request o) {
        return Integer.compare(o.getPriority().toInteger(), this.priority.toInteger());
    }

    @Override
    public String toString() {
        return "Request{" +
                "fileId=" + fileId +
                ", priority=" + priority +
                '}';
    }

    public enum Priority {
        LOW((byte) 0),
        MEDIUM((byte) 1),
        HIGH((byte) 2);

        private final byte priorityValue;

        Priority(byte i) {
            priorityValue = i;
        }

        public static Priority valueOf(byte v) {
            switch (v) {
                case 0:
                    return HIGH;
                case 1:
                    return MEDIUM;
                case 2:
                    return LOW;
                default:
                    throw new IllegalArgumentException("Out of range!");
            }
        }

        public int toInteger() {
            return priorityValue;
        }

    }
}
