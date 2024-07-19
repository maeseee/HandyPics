package org.ase.transfer;

import com.google.common.annotations.VisibleForTesting;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@AllArgsConstructor
public class TransferFolder {

    private final TransferFile transferFile;
    private final Retry retry;

    public void transfer(Path sourceFolder, Path destinationFolder, LocalDateTime lastBackupTime) {
        transferFile.transfer(sourceFolder, destinationFolder, lastBackupTime);
        retry.callWithRetry(() -> {
            try {
                callSubdirectories(sourceFolder, destinationFolder, lastBackupTime);
            } catch (IOException e) {
                System.err.println(sourceFolder + " has not been copied\n" + e);
                throw new RuntimeException(e);
            }
        });
    }

    private void callSubdirectories(Path sourceFolder, Path destinationFolder, LocalDateTime lastBackupTime) throws IOException {
        Collection<Path> sourceDirectories = transferFile.listDirectories(sourceFolder);
        List<Path> filteredList = sourceDirectories.stream()
                .filter(this::isNotInDirectoryIgnoreList)
                .filter(this::isNotHiddenDirectory)
                .toList();
        for (Path subSourceFolder : filteredList) {
            transfer(subSourceFolder, destinationFolder, lastBackupTime);
        }
    }

    @VisibleForTesting
    boolean isNotInDirectoryIgnoreList(Path folder) {
        List<String> ignoreList = List.of("sent", "private", "audio", "thumbnails", "whatsapp voice notes");
        String folderName = folder.getFileName().toString().toLowerCase().trim();
        return ignoreList.stream().noneMatch(folderName::contains);
    }

    @VisibleForTesting
    boolean isNotHiddenDirectory(Path path) {
        String directoryName = path.getFileName().toString();
        return !directoryName.startsWith(".");
    }
}
