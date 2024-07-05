package org.ase.transfer;

import java.nio.file.Path;

public record BackupFolder(Path sourceFolder, String destinationSubFolderName, boolean bestRating) {
}
