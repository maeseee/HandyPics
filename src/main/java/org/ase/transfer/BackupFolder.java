package org.ase.transfer;

import java.nio.file.Path;

public record BackupFolder(Path sourceFolder, Path destinationSubFolder) {
}
