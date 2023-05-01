package Tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.Map;
import java.util.zip.Adler32;

import Extract.Diff;
import Extract.DiffException;
import Extract.Extract;
import Extract.ExtractException;

/**
 * Service for patching files.
 */
public class PatchService {
    /*
     * Allows to use this module for creation of the CRC.ini
     */
    private static final boolean DO_CREATE_CRC = false;
    /*
     * File name of patching configuration
     */
    private static final String PATCH_INI_NAME = "patch.ini";
    /**
     * index for CRCs - static since multiple runs are possible
     */
    private static int crcNo = 0;
    /**
     * index for files to be extracted - static since multiple runs are possible
     */
    private static int extractNo = 0;
    /**
     * index for files to be checked - static since multiple runs are possible
     */
    private static int checkNo = 0;
    /**
     * index for files to be patched - static since multiple runs are possible
     */
    private static int patchNo = 0;
    /** path of the DIF files */
    private String patchPath;
    /** array of extensions to be ignored - read from ini */
    private String[] ignoreExt = { null };

    /**
     * Create DIF and CRC files, if enabled.
     * 
     * @param props           the Properties for the game.
     * @param referencePath   reference path for creation of DIF files
     * @param destinationPath Destination path (Lemmini resource) for extraction.
     * @param createdFiles    Map of files created.
     * @throws ExtractException if an extraction error occurs.
     * @throws ExportException  if an attempt to export a remote object fails.
     */
    public void createDifFiles(final Props props, final String referencePath, final String destinationPath,
            final Map<String, Object> createdFiles)
            throws ExtractException, ExportException {
        patchPath = Extract.exchangeSeparators(Extract.addSeparator("patch"));
        ignoreExt = props.get("ignore_ext", ignoreExt);

        if (referencePath != null) {
            // this is not needed by Lemmini, but to create the DIF files (and CRCs)
            if (DO_CREATE_CRC) {
                // create crc.ini
                Extract.out("\nCreate CRC ini");

                try (FileWriter fCRCList = new FileWriter(Extract.CRC_PATH + Extract.CRC_INI_NAME);) {
                    for (int i = 0; true; i++) {
                        String ppath;
                        ppath = props.get("pcrc_" + Integer.toString(i), "");

                        if (ppath.length() == 0) {
                            break;
                        }

                        createCRCs(Extract.SOURCE_PATH, ppath, fCRCList, ignoreExt);
                    }
                } catch (final IOException ex) {
                    throw new ExportException(
                            "Error processing " + Extract.CRC_PATH + Extract.CRC_INI_NAME + ": " + ex.getMessage());
                }

                Extract.checkCancel();
            }

            createPatches(props, patchPath, referencePath, destinationPath, createdFiles);
        }
    }

    /**
     * Step 8 of Extract thread run: use patch.ini to extract/patch all files.
     * 
     * @param destinationPath Destination path (Lemmini resource) for extraction.
     * 
     * @throws ExtractException if unable to use patch.ini to extract/patch all
     *                          files.
     */
    public void patchAllFiles(final String destinationPath) throws ExtractException {
        // step eight: use patch.ini to extract/patch all files
        // read patch.ini file
        final Props pprops = new Props();
        final URL fnp = Extract.findFile(patchPath + PATCH_INI_NAME/* , this */); // if it's in the JAR or local
                                                                                  // directory

        if (!pprops.load(fnp)) {
            throw new ExtractException("File " + PATCH_INI_NAME + " not found or error while reading");
        }

        // copy
        Extract.out("\nExtract files");

        for (int i = 0; true; i++) {
            String copy[] = { null, null };
            // 0: name 1: crc
            copy = pprops.get("extract_" + Integer.toString(i), copy);

            if (copy[0] == null) {
                break;
            }

            Extract.out(copy[0]);
            final String fnDecorated = copy[0].replace('/', '@');
            final URL fnc = Extract.findFile(patchPath + fnDecorated /* , pprops */);

            try {
                copyFile(fnc, destinationPath + copy[0]);
            } catch (final Exception ex) {
                throw new ExtractException("Copying " + patchPath + getFileName(copy[0]) + " to " + destinationPath
                        + copy[0] + " failed");
            }

            Extract.checkCancel();
        }

        patchFiles(pprops, destinationPath);
    }

    /**
     * Create CRCs for resources (development).
     * 
     * @param rPath     The root path with the files to create CRCs for
     * @param sDir      SubDir to create patches for
     * @param fCRCList  FileWriter to create crc.ini
     * @param ignoreExt Array of extensions to be ignored - read from ini.
     * @throws ExtractException if an extraction error occurs.
     */
    private void createCRCs(final String rPath, final String sDir, final FileWriter fCRCList,
            final String[] ignoreExt)
            throws ExtractException {
        // add separators and create missing directories
        final String rootPath = Extract.addSeparator(rPath + sDir);
        final File fSource = new File(rootPath);
        String out;
        final File[] files = fSource.listFiles();

        if (files == null) {
            throw new ExtractException("Path " + rootPath + " doesn't exist or IO error occured.");
        }

        final String subDir = Extract.addSeparator(sDir);

        outerLoop: for (int i = 0; i < files.length; i++) {
            int pos;

            // ignore directories
            if (files[i].isDirectory()) {
                continue;
            }

            // check extension
            pos = files[i].getName().lastIndexOf('.');

            if (pos > -1) {
                final String ext = files[i].getName().substring(pos + 1);

                for (int n = 0; n < ignoreExt.length; n++) {
                    if (ignoreExt[n].equalsIgnoreCase(ext)) {
                        continue outerLoop;
                    }
                }
            }

            final String fnIn = rootPath + files[i].getName();

            try {
                Extract.out(fnIn);
                // read src file
                final byte src[] = Extract.readFile(fnIn);
                final Adler32 crc32 = new Adler32();
                crc32.update(src);
                out = subDir + files[i].getName() + ", " + src.length + ", 0x" + Long.toHexString(crc32.getValue());
                fCRCList.write("crc_" + (Integer.toString(crcNo++)) + " = " + out + "\n");
            } catch (final Exception ex) {
                String msg = ex.getMessage();

                if (msg == null) {
                    msg = ex.toString();
                }

                throw new ExtractException(ex.getMessage());
            }
        }
    }

    /**
     * Step 7 of Extract thread run: create patches and patch.ini.
     * 
     * @param props           the Properties for the game.
     * @param patchPath       Path of the DIF files.
     * @param referencePath   Reference path for creation of DIF files.
     * @param destinationPath Destination path (Lemmini resource) for extraction.
     * @param createdFiles    Map of files created.
     * @throws ExtractException if unable to create patches or patch.ini.
     */
    private void createPatches(final Props props, final String patchPath, final String referencePath,
            final String destinationPath, final Map<String, Object> createdFiles)
            throws ExtractException {
        // step seven: create patches and patch.ini
        (new File(patchPath)).mkdirs();
        Extract.out("\nCreate patch ini");

        try (FileWriter fPatchList = new FileWriter(patchPath + PATCH_INI_NAME);) {
            for (int i = 0; true; i++) {
                String ppath;
                ppath = props.get("ppatch_" + Integer.toString(i), "");

                if (ppath.length() == 0) {
                    break;
                }

                createPatches(referencePath, destinationPath, ppath, patchPath, fPatchList, createdFiles);
            }
        } catch (final IOException ex) {
            throw new ExtractException("Error processing " + patchPath + PATCH_INI_NAME + ": " + ex.getMessage());
        }

        Extract.checkCancel();
    }

    /**
     * Create the DIF files from reference files and the extracted files
     * (development).
     * 
     * @param sPath        The path with the original (wanted) files
     * @param dPath        The patch with the differing (to be patched) files
     * @param subDir       SubDir to create patches for
     * @param pPath        The patch to write the patches to
     * @param fPatchList   FileWriter to create patch.ini
     * @param createdFiles Map of created files.
     * @throws ExtractException if an extraction error occurs.
     */
    private void createPatches(final String sPath, final String dPath, final String sDir, final String pPath,
            final FileWriter fPatchList, final Map<String, Object> createdFiles) throws ExtractException {
        // add separators and create missing directories
        Extract.SOURCE_PATH = Extract.addSeparator(sPath + sDir);
        final File fSource = new File(Extract.SOURCE_PATH);
        final String destPath = Extract.addSeparator(dPath + sDir);
        final File fDest = new File(destPath);
        fDest.mkdirs();
        patchPath = Extract.addSeparator(pPath);
        final File fPatch = new File(patchPath);
        fPatch.mkdirs();
        patchFiles(sDir, fPatchList, fSource, destPath, createdFiles);
    }

    /**
     * Loop through the List of Files to patch and patch them.
     * 
     * @param sDir         The path with the original (wanted) files
     * @param fPatchList   FileWriter to create patch.ini
     * @param fSource      a File representation of the folder containing the source
     *                     files.
     * @param destPath     the folder to which the patch files are to be written.
     * @param createdFiles Map of files created.
     * @throws ExtractException if an extraction error occurs.
     */
    private void patchFiles(final String sDir, final FileWriter fPatchList, final File fSource,
            final String destPath, final Map<String, Object> createdFiles) throws ExtractException {
        String out;
        final File[] files = fSource.listFiles();

        if (files == null) {
            throw new ExtractException("Path " + Extract.SOURCE_PATH + " doesn't exist or IO error occured.");
        }

        Diff.setParameters(512, 4);
        final String subDir = Extract.addSeparator(sDir);
        final String subDirDecorated = subDir.replace('/', '@');

        outerLoop: for (int i = 0; i < files.length; i++) {
            int pos;

            // ignore directories
            if (files[i].isDirectory()) {
                continue;
            }

            // check extension
            pos = files[i].getName().lastIndexOf('.');

            if (pos > -1) {
                final String ext = files[i].getName().substring(pos + 1);

                for (int n = 0; n < ignoreExt.length; n++) {
                    if (ignoreExt[n].equalsIgnoreCase(ext)) {
                        continue outerLoop;
                    }
                }
            }

            final String fnIn = Extract.SOURCE_PATH + files[i].getName();
            final String fnOut = destPath + files[i].getName();
            String fnPatch = files[i].getName();
            pos = fnPatch.toLowerCase().lastIndexOf('.');

            if (pos == -1) {
                pos = fnPatch.length();
            }

            fnPatch = patchPath + subDirDecorated + (fnPatch.substring(0, pos) + ".dif").toLowerCase();

            try {
                Extract.out(fnIn);
                // read src file
                final byte src[] = Extract.readFile(fnIn);
                byte trg[] = null;
                // read target file
                boolean fileExists = targetFileExists(fnOut, createdFiles);

                if (fileExists) {
                    try {
                        trg = Extract.readFile(fnOut);
                    } catch (final ExtractException ex) {
                        fileExists = false;
                    }
                }

                if (!fileExists) {
                    out = copyMissingFilesToPatchDirectory(fPatchList, files, subDirDecorated, i, src);
                    continue;
                }

                // create diff
                final byte patch[] = Diff.diffBuffers(trg, src);
                final int crc = Diff.targetCRC; // crc of target buffer
                out = subDir + files[i].getName() + ", 0x" + Integer.toHexString(crc);
                writePatchFileIfPatchExists(fPatchList, out, fnPatch, trg, patch);
            } catch (final Exception ex) {
                String msg = ex.getMessage();

                if (msg == null) {
                    msg = ex.toString();
                }

                throw new ExtractException(ex.getMessage());
            }
        }
    }

    /**
     * Indicates whether the target files exists.
     * 
     * @param fnOut        the name of the target file.
     * @param createdFiles Map of the files created.
     * @return <code>true</code> if the target file exists.
     */
    private boolean targetFileExists(final String fnOut, final Map<String, Object> createdFiles) {
        boolean fileExists;

        if (createdFiles.containsKey(fnOut.toLowerCase())) {
            fileExists = true;
        } else {
            fileExists = false;
        }
        return fileExists;
    }

    /**
     * Given that the specified file does not exist, mark it as missing and needing
     * to be extracted from the JAR file. Then, copy missing files to the patch dir.
     * 
     * @param fPatchList FileWriter to create patch.ini.
     * @param files      A List of Files in the source path.
     * @param subDir     SubDir to create patches for.
     * @param filesIndex Index into <code>files</code> for the File currently being
     *                   processed.
     * @param src        The bytes read from the source file being processed.
     */
    private String copyMissingFilesToPatchDirectory(final FileWriter fPatchList, final File[] files,
            final String subDir, final int filesIndex, final byte[] src)
            throws IOException, FileNotFoundException {
        final String subDirDecorated = subDir.replace('/', '@');
        final String fnIn = Extract.SOURCE_PATH + files[filesIndex].getName();
        String out;
        // mark missing files: needs to be extracted from JAR
        final Adler32 crc = new Adler32();
        crc.update(src);
        out = subDir + files[filesIndex].getName() + ", 0x" + Integer.toHexString((int) crc.getValue());
        fPatchList.write("extract_" + (Integer.toString(extractNo++)) + " = " + out + "\n");
        // copy missing files to patch dir
        Extract.copyFile(fnIn, patchPath + subDirDecorated + files[filesIndex].getName());
        return out;
    }

    /**
     * Writes the patch file if the patch is not null, or writes the appropriate
     * check Property if it is null.
     * 
     * @param fPatchList FileWriter to create patch.ini.
     * @param out        String with full path and filename + CRC for patch file.
     * @param fnPatch    Path and filename for .DIF file.
     * @param trg        Bytes read from existing destination file.
     * @param patch      Buffer with differences between source and target buffer.
     * @throws IOException      if an I/O error occurs.
     * @throws DiffException    if a Diff error occurs.
     * @throws ExtractException if an extraction error occurs.
     */
    private void writePatchFileIfPatchExists(final FileWriter fPatchList, final String out, final String fnPatch,
            final byte[] trg,
            final byte[] patch) throws IOException, DiffException, ExtractException {
        if (patch == null) {
            // Extract.out("src and trg are identical");
            fPatchList.write("check_" + (Integer.toString(checkNo++)) + " = " + out + "\n");
        } else {
            // apply patch to test it's ok
            Diff.patchbuffers(trg, patch);
            // write patch file
            writeFile(fnPatch, patch);
            fPatchList.write("patch_" + (Integer.toString(patchNo++)) + " = " + out + "\n");
        }
    }

    /**
     * Patch files, if enabled.
     * 
     * @param pprops          Properties from the patch.ini file.
     * @param destinationPath Destination path (Lemmini resource) for extraction.
     * @throws ExtractException if unable to patch files.
     */
    private void patchFiles(final Props pprops, final String destinationPath) throws ExtractException {
        // patch
        Extract.out("\nPatch files");

        for (int i = 0; true; i++) {
            String ppath[] = { null, null };
            // 0: name 1: crc
            ppath = pprops.get("patch_" + Integer.toString(i), ppath);

            if (ppath[0] == null) {
                break;
            }

            Extract.out(ppath[0]);
            String fnDif = ppath[0].replace('/', '@'); // getFileName(ppath[0]);
            int pos = fnDif.toLowerCase().lastIndexOf('.');

            if (pos == -1) {
                pos = fnDif.length();
            }

            fnDif = fnDif.substring(0, pos) + ".dif";
            final URL urlDif = Extract.findFile(patchPath + fnDif);

            if (urlDif == null) {
                throw new ExtractException("Patching of file " + destinationPath + ppath[0] + " failed.\n");
            }

            final byte dif[] = readFile(urlDif);
            final byte src[] = Extract.readFile(destinationPath + ppath[0]);

            try {
                final byte trg[] = Diff.patchbuffers(src, dif);
                // write new file
                writeFile(destinationPath + ppath[0], trg);
            } catch (final DiffException ex) {
                throw new ExtractException("Patching of file " + destinationPath + ppath[0] + " failed.\n" +
                        ex.getMessage());
            }

            Extract.checkCancel();
        }
    }

    /**
     * Get only the name of the file from an absolute path.
     * 
     * @param path absolute path of a file
     * @return file name without the path
     */
    private String getFileName(final String path) {
        int p1 = path.lastIndexOf("/");
        final int p2 = path.lastIndexOf("\\");

        if (p2 > p1) {
            p1 = p2;
        }

        if (p1 < 0) {
            p1 = 0;
        } else {
            p1++;
        }

        return path.substring(p1);
    }

    /**
     * Copy a file.
     * 
     * @param source      URL of source file
     * @param destination full destination file name including path
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void copyFile(final URL source, final String destination) throws FileNotFoundException, IOException {
        try (final InputStream fSrc = source.openStream();
                final FileOutputStream fDest = new FileOutputStream(destination);) {
            Extract.writeFromInputStreamToOutputStream(fSrc, fDest);
        }
    }

    /**
     * Read file into an array of byte.
     * 
     * @param fname file name as URL
     * @return array of byte
     * @throws ExtractException
     */
    private byte[] readFile(final URL fname) throws ExtractException {
        byte buf[] = null;

        try (final InputStream f = fname.openStream();) {
            final byte buffer[] = new byte[4096];
            // URLs/InputStreams suck: we can't read a length
            int len;
            final ArrayList<Byte> lbuf = new ArrayList<Byte>();

            while ((len = f.read(buffer)) != -1) {
                for (int i = 0; i < len; i++) {
                    lbuf.add(buffer[i]);
                }
            }

            // reconstruct byte array from ArrayList
            buf = new byte[lbuf.size()];

            for (int i = 0; i < lbuf.size(); i++) {
                buf[i] = lbuf.get(i).byteValue();
            }
        } catch (final FileNotFoundException ex) {
            throw new ExtractException("File " + fname + " not found");
        } catch (final IOException ex) {
            throw new ExtractException("IO exception while reading file " + fname);
        }

        return buf;
    }

    /**
     * Write array of byte to file.
     * 
     * @param fname file name
     * @param buf   array of byte
     * @throws ExtractException
     */
    private void writeFile(final String fname, final byte buf[]) throws ExtractException {
        try (final FileOutputStream f = new FileOutputStream(fname);) {
            f.write(buf);
        } catch (final IOException ex) {
            throw new ExtractException("IO exception while writing file " + fname);
        }
    }

}
