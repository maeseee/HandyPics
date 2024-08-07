package org.ase.config;

import org.ase.fileAccess.FileAccessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigReaderTest {

    @Mock
    private FileAccessor fileAccessor;
    @Mock
    private BufferedReader reader;

    @Test
    void shouldReadConfig_whenCorrect() throws IOException {
        when(fileAccessor.fileExists(any())).thenReturn(true);
        String ipAddress = "192.168.50.50";
        String folderName = "test";
        when(reader.readLine())
                .thenReturn(ipAddress)
                .thenReturn(folderName);
        ConfigReader testee = new ConfigReader(fileAccessor, Path.of("src"), reader);

        Config config = testee.readConfig();

        assertThat(config.ipAddress()).isEqualTo(ipAddress);
        assertThat(config.destinationRootFolder().getFileName().toString()).isEqualTo(folderName);
    }

    @Test
    void shouldRetryReadConfig_whenInputInvalid() throws IOException {
        when(fileAccessor.fileExists(any())).thenReturn(true);
        String ipAddressInvalid = "192.168.50.500";
        String ipAddress = "192.168.50.50";
        String folderNameInvalid = "5test";
        String folderName = "test";
        when(reader.readLine())
                .thenReturn(ipAddressInvalid)
                .thenReturn(ipAddress)
                .thenReturn(folderNameInvalid)
                .thenReturn(folderName);
        ConfigReader testee = new ConfigReader(fileAccessor, Path.of("src"), reader);

        Config config = testee.readConfig();

        assertThat(config.ipAddress()).isEqualTo(ipAddress);
        assertThat(config.destinationRootFolder().getFileName().toString()).isEqualTo(folderName);
    }

    @Test
    void shouldThrow_whenWorkingPathNotExists() {
        ConfigReader testee = new ConfigReader(fileAccessor, Path.of("invalid"), reader);

        assertThrows(RuntimeException.class, testee::readConfig);
    }

    @ParameterizedTest
    @CsvSource({
            "192.168.50.1",
            "192.168.50.0",
            "192.168.50.255",
            "::1"
    })
    void shouldBeValidIpAddress(String ipAddress) {
        ConfigReader testee = new ConfigReader(fileAccessor, Path.of("."), reader);

        boolean validIpAddress = testee.isValidIpAddress(ipAddress);

        assertTrue(validIpAddress);
    }

    @ParameterizedTest
    @CsvSource({
            "192.168.50.256",
            "192.a.50.256",
            "192.-5.50.256",
            "192.168.256",
            "192.168..5"
    })
    void shouldNotBeValide_whenIpAddressInvalid(String ipAddress) {
        ConfigReader testee = new ConfigReader(fileAccessor, Path.of("."), reader);

        boolean validIpAddress = testee.isValidIpAddress(ipAddress);

        assertFalse(validIpAddress);
    }

    @ParameterizedTest
    @CsvSource({
            "a1234",
            "abcd",
            "EFGH"
    })
    void shouldBeValidFolderName(String folderName) {
        ConfigReader testee = new ConfigReader(fileAccessor, Path.of("."), reader);

        boolean validFolderName = testee.isValidFolderName(folderName);

        assertTrue(validFolderName);
    }

    @ParameterizedTest
    @CsvSource({
            "1abc",
            "a.b",
            "z?"
    })
    void shouldNotBeValide_whenFolderNameInvalid(String folderName) {
        ConfigReader testee = new ConfigReader(fileAccessor, Path.of("."), reader);

        boolean validFolderName = testee.isValidFolderName(folderName);

        assertFalse(validFolderName);
    }
}