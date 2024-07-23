package org.ase;

import org.ase.history.LastBackup;
import org.ase.transfer.Transfer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HandyPicsTest {

    @Mock
    private Transfer transfer;
    @Mock
    LastBackup lastBackup;

    @Test
    void shouldTransfer() {
        LocalDateTime now = LocalDateTime.now();
        when(lastBackup.readLastBackupTimeFromFile()).thenReturn(now);
        Path path = Path.of("test");
        when(lastBackup.createFileFromNow()).thenReturn(path);
        HandyPics testee = new HandyPics(transfer, lastBackup);

        testee.transferImagesFromHandy();

        verify(lastBackup).loadLastBackup();
        verify(lastBackup).readLastBackupTimeFromFile();
        verify(transfer).backupPicturesInFolders(any(), eq(now), eq(true));
        verify(transfer).backupPicturesInFolders(any(), eq(now), eq(false));
        verify(lastBackup).createFileFromNow();
        verify(lastBackup).storeNowFile(path);
    }
}