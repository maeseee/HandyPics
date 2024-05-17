package org.ase.ftp;

import java.nio.file.Path;
import java.time.LocalDateTime;

public record FileProperty(Path filePath, LocalDateTime modificationDate) {
}
