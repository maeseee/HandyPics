package org.ase.ftp;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class ApacheFtpClient implements FtpClient {

    private final static int PORT = 2221;
    private final static String USERNAME = "android";
    private final static String PASSWORD = "mySweetHandyAccess";

    private final FTPClient ftp = new FTPClient();
    private final String server;

    public ApacheFtpClient(String server) {
        this(server, null);
    }

    @VisibleForTesting
    ApacheFtpClient(String server, PrintCommandListener printCommandListener) {
        this.server = server;
        if (printCommandListener != null) {
            ftp.addProtocolCommandListener(printCommandListener);
        }
    }

    @Override
    public Collection<FileProperty> listFiles(Path path) throws IOException {
        open();
        FTPFile[] files = ftp.listFiles(path.toString());
        List<FileProperty> content = Arrays.stream(files)
                .filter(FTPFile::isFile)
                .map(ftpFile -> new FileProperty(
                        path.resolve(ftpFile.getName()),
                        ftpFile.getTimestampInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()))
                .collect(toList());
        close();
        return content;
    }

    @Override
    public Collection<Path> listDirectories(Path path) throws IOException {
        open();
        FTPFile[] files = ftp.listFiles(path.toString());
        List<Path> content = Arrays.stream(files)
                .filter(FTPFile::isDirectory)
                .map(ftpFile -> path.resolve(ftpFile.getName()))
                .collect(toList());
        close();
        return content;
    }

    @Override
    public void uploadFile(Path sourceFile, Path destinationFile) throws IOException {
        String destinationFileString = "/" + toLinuxPath(destinationFile);
        open();
        ftp.deleteFile(destinationFileString);
        ftp.storeFile(destinationFileString, new FileInputStream(sourceFile.toFile()));
        close();
    }

    @Override
    public void downloadFile(Path sourceFile, Path destinationFolder) throws IOException {
        open();
        String linuxSourcePath = toLinuxPath(sourceFile);
        InputStream inputStream = ftp.retrieveFileStream(linuxSourcePath);
        if (inputStream == null) {
            throw new FileNotFoundException(linuxSourcePath);
        }
        Files.copy(inputStream, destinationFolder);
        inputStream.close();
        close();
    }

    private void open() throws IOException {
        ftp.connect(server, PORT);
        int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            throw new IOException("Exception in connecting to FTP Server");
        }

        ftp.login(USERNAME, PASSWORD);
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
    }

    private void close() throws IOException {
        ftp.logout();
        ftp.disconnect();
    }

    private String toLinuxPath(Path path) {
        return path.toString().replace("\\", "/");
    }
}