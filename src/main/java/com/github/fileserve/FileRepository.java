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
            updateTable = new UpdateTable(this);
            indexFile = new File(Paths.get(path.toString() + "\\" + "index").toString());
            if (!Files.exists(indexFile.toPath())) {
                logger.info("Missing update table! Generating...");
                updateTable.generateTable();
            } else {
                updateTable.parse();
            }
        } catch (IOException e) {
            logger.catching(e);
        } finally {
            logger.info("Loaded " + files.size() + " files.");
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
