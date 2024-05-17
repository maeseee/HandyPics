package org.ase.config;

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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigReaderTest {

    @Mock
    private BufferedReader reader;

    @Test
    void shouldReadConfig_whenCorrect() throws IOException {
        String ipAddress = "192.168.50.50";
        String folderName = "test";
        when(reader.readLine())
                .thenReturn(ipAddress)
                .thenReturn(folderName);
        ConfigReader testee = new ConfigReader(Path.of("src"), reader);

        Config config = testee.read();

        assertThat(config.ipAddress()).isEqualTo(ipAddress);
        assertThat(config.destinationWorkPath().getFileName().toString()).isEqualTo(folderName);
    }

    @Test
    void shouldRetryReadConfig_whenInputInvalid() throws IOException {
        String ipAddressInvalid = "192.168.50.500";
        String ipAddress = "192.168.50.50";
        String folderNameInvalid = "5test";
        String folderName = "test";
        when(reader.readLine())
                .thenReturn(ipAddressInvalid)
                .thenReturn(ipAddress)
                .thenReturn(folderNameInvalid)
                .thenReturn(folderName);
        ConfigReader testee = new ConfigReader(Path.of("src"), reader);

        Config config = testee.read();

        assertThat(config.ipAddress()).isEqualTo(ipAddress);
        assertThat(config.destinationWorkPath().getFileName().toString()).isEqualTo(folderName);
    }

    @Test
    void shouldThrow_whenWorkingPathNotExists() {
        ConfigReader testee = new ConfigReader(Path.of("invalid"), reader);

        assertThrows(RuntimeException.class, testee::read);
    }

    @ParameterizedTest
    @CsvSource({
            "192.168.50.1",
            "192.168.50.0",
            "192.168.50.255",
            "::1"
    })
    void shouldBeValidIpAddress(String ipAddress) {
        ConfigReader testee = new ConfigReader(Path.of("."), reader);

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
        ConfigReader testee = new ConfigReader(Path.of("."), reader);

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
        ConfigReader testee = new ConfigReader(Path.of("."), reader);

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
        ConfigReader testee = new ConfigReader(Path.of("."), reader);

        boolean validFolderName = testee.isValidFolderName(folderName);

        assertFalse(validFolderName);
    }
}