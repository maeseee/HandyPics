package org.ase.config;

import java.nio.file.Path;

public record Config(String ipAddress, Path destinationRootFolder) {
}
