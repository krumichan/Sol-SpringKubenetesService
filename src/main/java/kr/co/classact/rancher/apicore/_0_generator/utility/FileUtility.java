package kr.co.classact.rancher.apicore._0_generator.utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class FileUtility {

    private static final FileUtility instance = new FileUtility();
    public static FileUtility ins() { return instance; }
    private FileUtility() { }

    public void checkFile(String file) throws FileNotFoundException, FileSystemException {
        checkFile(new File(file));
    }

    public void checkFile(File file) throws FileNotFoundException, FileSystemException {
        if (!file.exists()) {
            throw new FileNotFoundException("File does not exists." + "\n" + "path: " + file.getPath());
        }

        if (!file.isFile()) {
            throw new FileSystemException("File does not 'file' type." + "\n" + "path: " + file.getPath());
        }
    }

    public String loadContent(String path) throws Exception {
        return Files.readString(Paths.get(path), StandardCharsets.US_ASCII);
    }

    public Path writeContent(String path, String content) throws Exception {
        return Files.writeString(Paths.get(path), content);
    }

    public void setupDirectory(String path, boolean removeOption) throws Exception {
        File output = new File(path);

        if (output.exists()) {
            if (removeOption && !output.delete()) {
                throw new Exception("Failed to delete output file." + "\n" + "path: " + path);
            } else if (!output.isDirectory()) {
                throw new Exception("[" + path + "] output path is not directory.");
            }
        }

        if (!output.exists() && !output.mkdirs()) {
            throw new Exception("Failed to create output directory." + "\n" + "path: " + path);
        }
    }
}
