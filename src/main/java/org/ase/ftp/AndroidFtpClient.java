package org.ase.ftp;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class AndroidFtpClient implements FtpClient {

    private final static int PORT = 2221;
    private final static String USERNAME = "android";
    private final static String PASSWORD = "mySweetHandyAccess";

    private final FTPClient ftp = new FTPClient();
    private final String server;

    public AndroidFtpClient(String server) {
        this(server, null);
    }

    @VisibleForTesting
    AndroidFtpClient(String server, PrintCommandListener printCommandListener) {
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
    public void putFileToPath(File file, String path) throws IOException {
        open();
        ftp.storeFile(path, new FileInputStream(file));
        close();
    }

    @Override
    public void downloadFile(Path source, Path destination) throws IOException {
        open();
        String linuxSourcePath = source.toString().replace("\\", "/");
        InputStream inputStream = ftp.retrieveFileStream(linuxSourcePath);
        if (inputStream == null) {
            throw new FileNotFoundException(linuxSourcePath);
        }
        Files.copy(inputStream, destination);
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
}