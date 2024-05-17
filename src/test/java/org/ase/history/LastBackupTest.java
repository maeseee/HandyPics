package org.ase.history;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LastBackupTest {

    @Test
    void shouldCreateLastBackupObject_whenLastBackupFileExists() throws IOException {
        Path backupFilePath = Path.of("LastBackup.txt");
        String timeStamp = "1709455921.059432";
        Files.write(backupFilePath, timeStamp.getBytes(), StandardOpenOption.CREATE);
        LastBackup testee = new LastBackup(Path.of("."));

        LocalDateTime lastBackupTime = testee.readLastBackupTimeFromFile();

        assertThat(lastBackupTime)
                .isBefore(LocalDateTime.of(2024, 3, 3, 10, 0))
                .isAfter(LocalDateTime.of(2024, 3, 3, 8, 0));
        backupFilePath.toFile().delete(); // Must be deleted immediately
    }

    @Test
    void shouldCreateLastBackupObjectFromStartOfEpoch_whenLastBackupFileDoesNotExist() {
        LastBackup testee = new LastBackup(Path.of("."));

        LocalDateTime lastBackupTime = testee.readLastBackupTimeFromFile();

        assertThat(lastBackupTime)
                .isBefore(LocalDateTime.of(1970, 1, 1, 1, 0));
    }

    @Test
    void shouldThrow_whenLastBackupObjectIsEmpty() throws IOException {
        Path backupFilePath = Path.of("LastBackup.txt");
        Files.createFile(backupFilePath);
        LastBackup testee = new LastBackup(Path.of("."));

        assertThrows(RuntimeException.class, testee::readLastBackupTimeFromFile);

        backupFilePath.toFile().delete(); // Must be deleted immediately
    }
}