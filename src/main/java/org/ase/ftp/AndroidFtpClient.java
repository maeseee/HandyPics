package org.ase.ftp;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;

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
    public void open() throws IOException {
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
    public Collection<FileProperty> listFiles(Path path) throws IOException {
        FTPFile[] files = ftp.listFiles(path.toString());
        return Arrays.stream(files)
                .map(ftpFile -> new FileProperty(
                        path.resolve(ftpFile.getName()),
                        ftpFile.getTimestampInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()))
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