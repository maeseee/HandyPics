package org.ase.ftp;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

@RequiredArgsConstructor
public class FtpAccessor {

    private final FtpClient ftpClient;

    public Collection<FileProperty> listFiles(Path folder) throws IOException {
        return ftpClient.listFiles(folder);
    }

    public Collection<Path> getListDirectories(Path sourceFolder) throws IOException {
        return ftpClient.listDirectories(sourceFolder);
    }

    public void downloadFile(Path sourceFile, Path destinationFile) throws IOException {
        ftpClient.downloadFile(sourceFile, destinationFile);
    }

    public void storeFileTo(Path sourceFile, Path destinationFolder) throws IOException {
        ftpClient.putFileToPath(sourceFile, destinationFolder.resolve(sourceFile.getFileName()));
    }
}
