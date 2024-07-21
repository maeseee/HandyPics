package org.ase.history;

import org.ase.fileAccess.FileAccessor;
import org.ase.ftp.FtpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastBackupTest {

    @Mock
    private FtpClient ftpClient;
    @Mock
    private FileAccessor fileAccessor;

    @Test
    void shouldTakeLastBackupFileFromDestination_whenAlreadyThere() {
        Path backupFilePath = Path.of("./LastBackup.txt");
        when(fileAccessor.fileExists(backupFilePath)).thenReturn(true);
        LastBackup testee = new LastBackup(ftpClient, fileAccessor, Path.of("."));

        testee.loadLastBackup();

        verifyNoInteractions(ftpClient);
    }

    @Test
    void shouldLoadLastBackupFile_whenNotHavingOne() throws IOException {
        Path backupFilePath = Path.of("./LastBackup.txt");
        when(fileAccessor.fileExists(backupFilePath)).thenReturn(false);
        LastBackup testee = new LastBackup(ftpClient, fileAccessor, Path.of("."));

        testee.loadLastBackup();

        verify(ftpClient).downloadFile(any(), eq(backupFilePath));
    }

    @Test
    void shouldReadLastBackupTimeFromStartOfEpoch_whenFileDoesNotExist() {
        Path backupFilePath = Path.of("./LastBackup.txt");
        when(fileAccessor.fileExists(backupFilePath)).thenReturn(false);
        LastBackup testee = new LastBackup(ftpClient, fileAccessor, Path.of("."));

        LocalDateTime lastBackupTime = testee.readLastBackupTimeFromFile();

        assertThat(lastBackupTime)
                .isBefore(LocalDateTime.of(1971, 3, 3, 10, 0))
                .isAfter(LocalDateTime.of(1969, 3, 3, 8, 0));
        verifyNoInteractions(ftpClient);
    }

    @Test
    void shouldReadLastBackupTimeFromFile_whenThere() {
        Path backupFilePath = Path.of("./LastBackup.txt");
        when(fileAccessor.fileExists(backupFilePath)).thenReturn(true);
        when(fileAccessor.readFirstLineInFile(backupFilePath)).thenReturn("1721030400");
        LastBackup testee = new LastBackup(ftpClient, fileAccessor, Path.of("."));

        LocalDateTime lastBackupTime = testee.readLastBackupTimeFromFile();

        assertThat(lastBackupTime)
                .isBefore(LocalDateTime.of(2024, 7, 16, 10, 0))
                .isAfter(LocalDateTime.of(2024, 7, 14, 8, 0));
        verifyNoInteractions(ftpClient);
    }

    @Test
    void shouldThrow_whenLastBackupObjectIsEmpty() {
        Path backupFilePath = Path.of("./LastBackup.txt");
        when(fileAccessor.fileExists(backupFilePath)).thenReturn(true);
        when(fileAccessor.readFirstLineInFile(backupFilePath)).thenReturn("");
        LastBackup testee = new LastBackup(ftpClient, fileAccessor, Path.of("."));

        assertThrows(RuntimeException.class, testee::readLastBackupTimeFromFile);

        verifyNoInteractions(ftpClient);
    }
}