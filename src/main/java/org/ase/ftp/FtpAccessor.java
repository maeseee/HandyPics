package org.ase.ftp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

public class FtpAccessor {

    private final FtpClient ftpClient;
    private final Path destinationDir;

    public FtpAccessor(FtpClient ftpClient, Path destinationDir) {
        this.ftpClient = ftpClient;
        this.destinationDir = destinationDir;
    }

    public void copyFilesFrom(Path sourcePath) throws IOException {
        ftpClient.open();
        Collection<String> files = ftpClient.listFiles(sourcePath);
        files.forEach(filename -> {
            try {
                ftpClient.downloadFile(sourcePath + "/" + filename, destinationDir + "/" + filename);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        ftpClient.close();
    }

    public void copyFileFrom(File filePath) throws IOException {
        ftpClient.open();
        ftpClient.downloadFile(filePath.getPath(), destinationDir + "/" + filePath.getName());
        ftpClient.close();
    }
}
