package org.ase.history;

import org.ase.ftp.FtpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class LastBackupTest {

    @Mock
    private FtpClient ftpClient;

    @Test
    @SuppressWarnings("unused")
    void shouldCreateLastBackupObject_whenLastBackupFileExists() throws IOException {
        Path backupFilePath = Path.of("LastBackup.txt");
        String timeStamp = "1709455921.059432";
        Files.write(backupFilePath, timeStamp.getBytes(), StandardOpenOption.CREATE);
        LastBackup testee = new LastBackup(ftpClient, Path.of("."));

        LocalDateTime lastBackupTime = testee.readLastBackupTimeFromFile();

        assertThat(lastBackupTime)
                .isBefore(LocalDateTime.of(2024, 3, 3, 10, 0))
                .isAfter(LocalDateTime.of(2024, 3, 3, 8, 0));
        verifyNoInteractions(ftpClient);
        boolean deleted = backupFilePath.toFile().delete(); // Must be deleted immediately
    }

    @Test
    void shouldCreateLastBackupObjectFromStartOfEpoch_whenLastBackupFileDoesNotExist() {
        LastBackup testee = new LastBackup(ftpClient, Path.of("."));

        LocalDateTime lastBackupTime = testee.readLastBackupTimeFromFile();

        assertThat(lastBackupTime)
                .isBefore(LocalDateTime.of(1970, 1, 1, 1, 0));
        verifyNoInteractions(ftpClient);
    }

    @Test
    @SuppressWarnings("unused")
    void shouldThrow_whenLastBackupObjectIsEmpty() throws IOException {
        Path backupFilePath = Path.of("LastBackup.txt");
        Files.createFile(backupFilePath);
        LastBackup testee = new LastBackup(ftpClient, Path.of("."));

        assertThrows(RuntimeException.class, testee::readLastBackupTimeFromFile);

        verifyNoInteractions(ftpClient);
        boolean deleted = backupFilePath.toFile().delete(); // Must be deleted immediately
    }
}