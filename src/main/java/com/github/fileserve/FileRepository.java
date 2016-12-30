package com.github.fileserve;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;

public class FileRepository {

    private Logger logger = LogManager.getLogger();

    private ArrayList<File> files = new ArrayList<>();

    /**
     * Directory of the file repository.
     */
    private final Path path;

    private UpdateTable updateTable;

    private File indexFile;
    public FileRepository(Path path) {
        this.path = path;
        try {
            Files.walk(path).filter(f -> Files.isRegularFile(f) && !f.toFile().getName().matches("index")).map(Path::toFile).forEach(files::add);
            this.updateTable = new UpdateTable();
            this.indexFile = new File(Paths.get(path.toString() + "\\" + "index").toString());
            if (!Files.exists(indexFile.toPath())) {
                logger.info("Missing update table! Generating...");
                updateTable.generateTable(path, indexFile, files);
            } else {
                updateTable.parse(getIndexData());
            }
        } catch (IOException e) {
            logger.catching(e);
        } finally {
            logger.info("Loaded " + files.size() + " files.");
        }
    }

    /**
     * Create a new {@link FileRepository} with given index file data.
     * @param path
     * @param indexFileData
     */
    public FileRepository(Path path, byte[] indexFileData) {
        this.path = path;
        this.updateTable = new UpdateTable();
        this.indexFile = new File(Paths.get(path.toString() + "\\" + "index").toString());
        try {
            updateTable.parse(indexFileData);
            Files.write(indexFile.toPath(), indexFileData);
            Files.walk(path).filter(f -> Files.isRegularFile(f) && !f.toFile().getName().matches("index")).map(Path::toFile).forEach(files::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Optional<File> locate(int fileId) {
        Optional<File> file = Optional.of(files.get(fileId));
        if(fileId > files.size() || !file.isPresent()) {
            logger.error("Could not locate file or malformed file request!", new ArrayIndexOutOfBoundsException());
        }
        return file;
    }

    public File getIndexFile() {
        return indexFile;
    }

    public UpdateTable getUpdateTable() {
        return updateTable;
    }

    public byte[] getIndexData() throws IOException {
        return Files.readAllBytes(indexFile.toPath());
    }

    public ArrayList<File> getFiles() {
        return files;
    }

    public int getNumberOfFiles() {
        return files.size();
    }

    public Path getRepositoryPath() {
        return path;
    }
}
