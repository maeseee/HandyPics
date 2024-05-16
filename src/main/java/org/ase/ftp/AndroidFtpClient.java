package org.ase.ftp;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

import static java.util.stream.Collectors.toList;

public class AndroidFtpClient implements FtpClient {

    private final static int PORT = 2221;
    private final static String USERNAME = "android";
    private final static String PASSWORD = "mySweetHandyAccess";

    private final String server;
    private FTPClient ftp;

    public AndroidFtpClient(String server) {
        this.server = server;
    }

    @Override
    public void open() throws IOException {
        ftp = new FTPClient();
        ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));

        ftp.connect(server, PORT);
        int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            throw new IOException("Exception in connecting to FTP Server");
        }

        ftp.login(USERNAME, PASSWORD);
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
    }

    @Override
    public void close() throws IOException {
        ftp.disconnect();
    }

    @Override
    public Collection<String> listFiles(Path path) throws IOException {
        FTPFile[] files = ftp.listFiles(path.toString());
        return Arrays.stream(files)
                .map(FTPFile::getName)
                .collect(toList());
    }

    @Override
    public void putFileToPath(File file, String path) throws IOException {
        ftp.storeFile(path, new FileInputStream(file));
    }

    @Override
    public void downloadFile(String source, String destination) throws IOException {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(destination));
        ftp.retrieveFile(source, out);
        out.close();
    }
}