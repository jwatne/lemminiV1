package tools;

import java.io.File;
/*
 * Copyright 2009 Volker Oth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
