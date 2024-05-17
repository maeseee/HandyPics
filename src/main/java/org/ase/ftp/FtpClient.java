package org.ase.ftp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

public interface FtpClient {
    void open() throws IOException;

    void close() throws IOException;

    Collection<FileProperty> listFiles(Path path) throws IOException;

    Collection<Path> listDirectories(Path path) throws IOException;

    void putFileToPath(File file, String path) throws IOException;

    void downloadFile(String source, String destination) throws IOException;
}
