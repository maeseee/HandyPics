package org.ase;

import java.io.IOException;
import java.util.Collection;

public class FtpAccessor {
    public static void main(String[] args) throws IOException {
        String server = "192.168.50.162";
        int port = 2221;
        String username = "android";
        String password = "mySweetHandyAccess";
        String remoteFolder = "DCIM/MyAlbums/Best";
        String localFolder = "C:/Users/maese//Bilder/FromHandy/temp/";

        FtpClient ftpClient = new FtpClient(server, port, username, password);
        ftpClient.open();
        Collection<String> files = ftpClient.listFiles(remoteFolder);
        files.forEach(filename -> {
            try {
                ftpClient.downloadFile(remoteFolder + "/" + filename, localFolder + "/" + filename);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        ftpClient.close();
    }
}
