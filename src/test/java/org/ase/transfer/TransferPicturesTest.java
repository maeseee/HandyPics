package org.ase.transfer;

import org.ase.fileAccess.FileAccessor;
import org.ase.ftp.FtpAccessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransferPicturesTest {

    private final LocalDateTime lastBackupTime = LocalDateTime.now().minusDays(1);
    private final Path destinationRootFolder = Path.of("test");
    private final Path sourceFolder = Path.of("source");
    private final List<BackupFolder> backupFolders = List.of(new BackupFolder(sourceFolder, "subFolder"));

    @Mock
    private FtpAccessor ftpAccessor;
    @Mock
    private FileAccessor fileAccessor;

    @Test
    void shouldCopyFilesFromSourceToDestination() throws IOException {
        TransferPictures testee = new TransferPictures(ftpAccessor, fileAccessor, lastBackupTime, destinationRootFolder, backupFolders);

        testee.copy(false);

        Path expectedDestinationFolder = Path.of("test/subFolder");
        verify(ftpAccessor).copyFilesFrom(sourceFolder, expectedDestinationFolder, lastBackupTime);
    }

    @Test
    void shouldCopyFilesFromSourceToFavoritDestination_whenIsFavorit() throws IOException {
        TransferPictures testee = new TransferPictures(ftpAccessor, fileAccessor, lastBackupTime, destinationRootFolder, backupFolders);

        testee.copy(true);

        Path expectedDestinationFolder = Path.of("test/subFolderFavorite");
        verify(ftpAccessor).copyFilesFrom(sourceFolder, expectedDestinationFolder, lastBackupTime);
    }
}