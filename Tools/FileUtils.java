package tools;

import java.io.File;

/**
 * Utility class for File operations.
 *
 * @author John Watne
 */
public final class FileUtils {
    /**
     * Private constructor for utility class.
     */
    private FileUtils() {

    }

    /**
     * Creates a directory at the specified pathname, if it does not already
     * exist. If unable to create the folder, write a message to the console.
     *
     * @param pathname the pathname fo the directory to be created.
     * @return a File object for the directory.
     */
    public static File makeDirIfItDoesNotExist(final String pathname) {
        final File dest = new File(pathname);

        if (!dest.exists()) {
            final boolean mkdirs = dest.mkdirs();

            if (!mkdirs) {
                System.out.println("Unable to make dir " + pathname);
            }
        }

        return dest;
    }
}
