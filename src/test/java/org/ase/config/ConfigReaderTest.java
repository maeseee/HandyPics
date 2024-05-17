package org.ase.config;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigReaderTest {

    @ParameterizedTest
    @CsvSource({
            "192.168.50.1",
            "192.168.50.0",
            "192.168.50.255",
            "::1"
    })
    void shouldBeValidIpAddress(String ipAddress) {
        ConfigReader testee = new ConfigReader(Path.of("."));

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
        ConfigReader testee = new ConfigReader(Path.of("."));

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
        ConfigReader testee = new ConfigReader(Path.of("."));

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
        ConfigReader testee = new ConfigReader(Path.of("."));

        boolean validFolderName = testee.isValidFolderName(folderName);

        assertFalse(validFolderName);
    }
}