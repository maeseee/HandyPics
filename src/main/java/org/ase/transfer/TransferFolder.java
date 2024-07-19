package org.ase.transfer;

import lombok.AllArgsConstructor;
import org.ase.ftp.FtpAccessor;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;

@AllArgsConstructor
public class TransferFolder {

    private final FtpAccessor ftpAccessor;
    private final Retry retry;

    public void transfer(Path sourceFolder, Path destinationFolder, LocalDateTime lastBackupTime) {
        retry.callWithRetry(() -> {
            try {
                ftpAccessor.copyFilesFrom(sourceFolder, destinationFolder, lastBackupTime);
            } catch (IOException e) {
                System.err.println("ERROR copying folder: " + sourceFolder + " -> " + destinationFolder + "\n" + e);
                throw new RuntimeException(e);
            }
        });
    }
}
