package org.ase.image;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.constants.TiffDirectoryType;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfoShort;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

public class ImageModifier {

    private static final TagInfoShort RATING_TAG = new TagInfoShort("Rating", 0x4746, TiffDirectoryType.EXIF_DIRECTORY_IFD0);
    private static final TagInfoShort RATING_PERCENT_TAG = new TagInfoShort("RatingPercent", 0x4749, TiffDirectoryType.EXIF_DIRECTORY_IFD0);

    public void setJpegRating(Path imageFile, Path outputFile, int rating) throws IOException, ImageReadException, ImageWriteException {
        validateRating(rating);
        TiffOutputSet outputSet = getTiffOutputSet(imageFile);
        setRating(rating, outputSet);
        writeOutputFile(imageFile, outputFile, outputSet);
    }

    private void validateRating(int rating) {
        if (rating < 0 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 0 and 5");
        }
    }

    @VisibleForTesting
    TiffOutputSet getTiffOutputSet(Path imageFile) throws ImageReadException, IOException, ImageWriteException {
        TiffOutputSet outputSet = null;
        ImageMetadata metadata = Imaging.getMetadata(imageFile.toFile());
        if (metadata instanceof JpegImageMetadata jpegMetadata) {
            outputSet = jpegMetadata.getExif().getOutputSet();
        }
        if (outputSet == null) {
            outputSet = new TiffOutputSet();
        }
        return outputSet;
    }

    private void setRating(int rating, TiffOutputSet outputSet) throws ImageWriteException {
        TiffOutputDirectory exifDirectory = outputSet.getOrCreateRootDirectory();
        exifDirectory.removeField(RATING_TAG);  // Remove old value if present
        exifDirectory.add(RATING_TAG, (short) rating);
        exifDirectory.removeField(RATING_PERCENT_TAG);  // Remove old value if present
        exifDirectory.add(RATING_PERCENT_TAG, getPercentRating(rating));
    }

    private short getPercentRating(int rating) {
        return (short) ((rating * 20) - 1);
    }

    private void writeOutputFile(Path imageFile, Path outputFile, TiffOutputSet outputSet)
            throws IOException, ImageReadException, ImageWriteException {
        try (FileOutputStream fos = new FileOutputStream(outputFile.toFile())) {
            new ExifRewriter().updateExifMetadataLossless(imageFile.toFile(), fos, outputSet);
        }
    }

    public static void main(String[] args) {
        Path jpegFile = Path.of("IMG_20240104_155120.jpg");
        Path outputFile = Path.of("IMG_20240104_155120_rated.jpg");
        int rating = 5;  // 5-star rating
        ImageModifier imageModifier = new ImageModifier();

        try {
            imageModifier.setJpegRating(jpegFile, outputFile, rating);
            System.out.println("Rating set successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
