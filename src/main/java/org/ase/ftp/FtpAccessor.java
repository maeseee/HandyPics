package org.ase.ftp;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

public class FtpAccessor {

    private final static int PORT = 2221;
    private final static String USERNAME = "android";
    private final static String PASSWORD = "mySweetHandyAccess";

    private final String server;
    private final Path destinationDir;

    public FtpAccessor(String server, Path destinationDir) {
        this.server = server;
        this.destinationDir = destinationDir;
    }

    public void copyFiesFrom(Path sourceDir) throws IOException {
        ApacheFtpClient ftpClient = new ApacheFtpClient(server, PORT, USERNAME, PASSWORD);
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
