package org.ase.transfer;

import java.nio.file.Path;

public record BackupFolder(Path folder, String destinationSubFolder, boolean bestRating) {
}
