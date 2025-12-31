package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility for reading image files as byte arrays.
 */
public class ImageUtil
{
    public static byte[] readImageAsBytes(String filePath)
    {
        try {
            return Files.readAllBytes(Path.of(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}