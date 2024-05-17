package org.ase.ftp;

import java.time.LocalDateTime;

public record FileProperty(String fileName, LocalDateTime modificationDate) {
}
