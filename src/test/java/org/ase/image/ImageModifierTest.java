package org.ase.image;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputField;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImageModifierTest {

    private final static Path INPUT_FOLDER = Path.of("resources");
    private final static Path OUTPUT_FOLDER = Path.of("output");

    @BeforeEach
    void addArtifactFolder() throws IOException {
        Files.createDirectory(OUTPUT_FOLDER);
    }

    @AfterEach
    void deleteGeneratedArtifacts() throws IOException {
        Files.walk(OUTPUT_FOLDER)
                .sorted((p1, p2) -> -p1.compareTo(p2)) // Sort in reverse order
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        throw new RuntimeException();
                    }
                });
    }

    @Test
    void shouldAddFiveStars_whenAskedFor() throws ImageWriteException, IOException, ImageReadException {
        String fileName = "smile.jpg";
        Path imageFile = INPUT_FOLDER.resolve(fileName);
        Path outputFile = OUTPUT_FOLDER.resolve(fileName);
        ImageModifier testee = new ImageModifier();

        testee.setJpegRating(imageFile, outputFile, 5);

        assertThat(Files.exists(outputFile)).isTrue();
        assertRatingOnImage(testee, outputFile);
    }

    @Test
    void shouldIgnorFile_whenPngFormat() throws ImageWriteException, IOException, ImageReadException {
        String fileName = "smile.png";
        Path imageFile = INPUT_FOLDER.resolve(fileName);
        Path outputFile = OUTPUT_FOLDER.resolve(fileName);
        ImageModifier testee = new ImageModifier();

        testee.setJpegRating(imageFile, outputFile, 5);

        assertThat(Files.exists(outputFile)).isFalse();
    }

    @Test
    void shouldThrow_whenFileNotFound() throws ImageWriteException, IOException, ImageReadException {
        String fileName = "invalid.jpg";
        Path imageFile = INPUT_FOLDER.resolve(fileName);
        Path outputFile = OUTPUT_FOLDER.resolve(fileName);
        ImageModifier testee = new ImageModifier();

        assertThrows(FileNotFoundException.class, () -> testee.setJpegRating(imageFile, outputFile, 5));
    }

    private void assertRatingOnImage(ImageModifier testee, Path outputFile) throws ImageWriteException, IOException, ImageReadException {
        TiffOutputSet outputSet = testee.getTiffOutputSet(outputFile);
        TiffOutputDirectory exifDirectory = outputSet.getOrCreateRootDirectory();
        List<TiffOutputField> fields = exifDirectory.getFields();

        assertRating(fields, "Rating", 0x4746, 5);

        assertRating(fields, "RatingPercent", 0x4749, 99);
    }

    private static void assertRating(List<TiffOutputField> fields, String tagName, int tagCode, int expectedRating) {
        Optional<TiffOutputField> ratingPercent =
                fields.stream().filter(tiffOutputField -> tagName.equals(tiffOutputField.tagInfo.name)).findFirst();
        assertThat(ratingPercent).isPresent();
        assertThat(ratingPercent.get().tag).isEqualTo(tagCode);
        byte[] ratingBytes = new byte[2];
        ratingBytes[0] = (byte) expectedRating;
        assertThat(ratingPercent.get().bytesEqual(ratingBytes)).isTrue();
    }
}