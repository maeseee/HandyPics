package org.ase.ftp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

public class FtpAccessor {

    private final FtpClient ftpClient;
    private final Path destinationPath;

    public FtpAccessor(FtpClient ftpClient, Path destinationPath) {
        this.ftpClient = ftpClient;
        this.destinationPath = destinationPath;
    }

    public void copyFilesFrom(Path sourcePath, String destinationFolder) throws IOException {
        ftpClient.open();
        Collection<String> files = ftpClient.listFiles(sourcePath);
        files.forEach(filename -> download(sourcePath + "/" + filename, destinationPath + "/" + destinationFolder + "/" + filename));
        ftpClient.close();
    }

    public void copyFileFrom(File filePath) throws IOException {
        ftpClient.open();
        download(filePath.getPath(), destinationPath + "/" + filePath.getName());
        ftpClient.close();
    }

    private void download(String sourcePath, String destinationPath) {
        try {
            ftpClient.downloadFile(sourcePath, destinationPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
