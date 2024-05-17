package org.ase.picture;

import java.nio.file.Path;

public record BackupFolder(Path folder, String destinationSubFolder) {
}
