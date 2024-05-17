package org.ase.ftp;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class FtpAccessor {

    private final FtpClient ftpClient;

    public void copyFilesFrom(Path sourcePath, Path destinationPath, LocalDateTime lastBackupTime) throws IOException {
        ftpClient.open();
        Collection<FileProperty> files = ftpClient.listFiles(sourcePath);
        List<String> filteredList = files.stream()
                .filter(fileProperty -> isModificationDateNewer(lastBackupTime, fileProperty.modificationDate()))
                // TODO filter image data
                .map(FileProperty::fileName)
                .toList();

        for (String filename : filteredList) {
            download(sourcePath.resolve(filename), destinationPath.resolve(filename));
        }
        ftpClient.close();
    }

    public void copyFileFrom(Path sourcePath, Path destinationPath) throws IOException {
        ftpClient.open();
        download(sourcePath, destinationPath);
        ftpClient.close();
    }

    private void download(Path sourcePath, Path destinationPath) throws IOException {
        // TODO retry
        ftpClient.downloadFile(sourcePath.toString(), destinationPath.toString());
    }

    private boolean isModificationDateNewer(LocalDateTime lastBackupTime, LocalDateTime modificationDate) {
        return lastBackupTime.isBefore(modificationDate);
    }
}
