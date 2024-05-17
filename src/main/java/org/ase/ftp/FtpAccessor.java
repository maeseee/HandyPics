package org.ase.ftp;

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
        files.forEach(filename -> download(sourcePath.resolve(filename), destinationPath.resolve(destinationFolder).resolve(filename)));
        ftpClient.close();
    }

    public void copyFileFrom(Path filePath) throws IOException {
        ftpClient.open();
        download(filePath, destinationPath.resolve(filePath.getFileName()));
        ftpClient.close();
    }

    private void download(Path sourcePath, Path destinationPath) {
        try {
            ftpClient.downloadFile(sourcePath.toString(), destinationPath.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
