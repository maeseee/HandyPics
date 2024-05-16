package org.ase.ftp;

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

    public void copyFiesFrom(Path sourceDir) throws IOException {
        ftpClient.open();
        Collection<String> files = ftpClient.listFiles(sourceDir);
        files.forEach(filename -> {
            try {
                ftpClient.downloadFile(sourceDir + "/" + filename, destinationDir + "/" + filename);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        ftpClient.close();
    }
}
