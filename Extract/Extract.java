package Extract;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.zip.Adler32;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import Tools.PatchService;
import Tools.Props;

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
 * Extraction of resources.
 *
 * @author Volker Oth
 */
public class Extract extends Thread {
	/** file name of extraction configuration */
	final private static String iniName = "extract.ini";
	/** file name of resource CRCs (WINLEMM) */
	final public static String CRC_INI_NAME = "crc.ini";
	/** output dialog */
	private static OutputDialog outputDiag;
	/** monitor the files created without erasing the target dir */
	private static HashMap<String, Object> createdFiles;
	/** source path (WINLEMM) for extraction */
	public static String SOURCE_PATH;
	/** destination path (Lemmini resource) for extraction */
	private static String destinationPath;
	/** reference path for creation of DIF files */
	private static String referencePath;
	/** path of the CRC ini (without the file name) */
	public static String CRC_PATH;
	/** exception caught in the thread */
	private static ExtractException threadException = null;
	/** static self reference to access thread from outside */
	private static Thread thisThread;
	/** reference to class loader */
	private static ClassLoader loader;

	/**
	 * Display an exception message box.
	 * 
	 * @param ex Exception
	 */
	private static void showException(final Throwable ex) {
		String m;
		m = "<html>";
		m += ex.getClass().getName() + "<p>";

		if (ex.getMessage() != null) {
			m += ex.getMessage() + "<p>";
		}

		final StackTraceElement ste[] = ex.getStackTrace();

		for (int i = 0; i < ste.length; i++) {
			m += ste[i].toString() + "<p>";
		}

		m += "</html>";
		ex.printStackTrace();
		JOptionPane.showMessageDialog(null, m, "Error", JOptionPane.ERROR_MESSAGE);
		ex.printStackTrace();
	}

	/**
	 * Extraction running in a Thread.
	 */
	@Override
	public void run() {
		createdFiles = new HashMap<String, Object>(); // to monitor the files created without erasing the target dir

		try {
			// read ini file
			final Props props = new Props();
			final URL fn = findFile(iniName);

			if (fn == null || !props.load(fn)) {
				throw new ExtractException("File " + iniName + " not found or error while reading");
			}

			// prolog_ check CRC
			validateWINLEMM();
			extractLevels(props);
			final ExtractSPR sprite = extractStyles(props);
			extractObjects(props, sprite);

			// if (false) { // debug only
			createDirectories(props);
			copyStuff(props);
			cloneFiles(props);
			final PatchService patchService = new PatchService();
			patchService.createDifFiles(props, referencePath, destinationPath, createdFiles);
			patchService.patchAllFiles(destinationPath);
			// } // debug only

			// finished
			out("\nSuccessfully finished!");
		} catch (final ExtractException ex) {
			threadException = ex;
			out(ex.getMessage());
		} catch (final Exception ex) {
			showException(ex);
			System.exit(1);
		} catch (final Error ex) {
			showException(ex);
			System.exit(1);
		}

		outputDiag.enableOk();
	}

	/**
	 * Validate that the program has access to a valid WINLEMM installation's files.
	 * 
	 * @throws ExtractException if an extraction error occurs.
	 */
	private void validateWINLEMM() throws ExtractException {
		out("\nValidating WINLEMM");
		final URL fncrc = findFile(CRC_INI_NAME);
		final Props cprops = new Props();

		if (fncrc == null || !cprops.load(fncrc)) {
			throw new ExtractException("File " + CRC_INI_NAME + " not found or error while reading");
		}

		for (int i = 0; true; i++) {
			String crcbuf[] = { null, null, null };
			// 0: name, 1:size, 2: crc
			crcbuf = cprops.get("crc_" + Integer.toString(i), crcbuf);

			if (crcbuf[0] == null) {
				break;
			}

			out(crcbuf[0]);
			final long len = new File(SOURCE_PATH + crcbuf[0]).length();

			if (len != Long.parseLong(crcbuf[1])) {
				throw new ExtractException("CRC error for file " + SOURCE_PATH + crcbuf[0] + ".\n");
			}

			final byte src[] = readFile(SOURCE_PATH + crcbuf[0]);
			final Adler32 crc32 = new Adler32();
			crc32.update(src);

			if (Long.toHexString(crc32.getValue()).compareToIgnoreCase(crcbuf[2].substring(2)) != 0) {
				throw new ExtractException("CRC error for file " + SOURCE_PATH + crcbuf[0] + ".\n");
			}

			checkCancel();
		}
	}

	/**
	 * Step 6 of Extract thread run: clone files inside destination dir.
	 * 
	 * @param props the Properties for the game.
	 * @throws ExtractException if an extraction error occurs.
	 */
	private void cloneFiles(final Props props) throws ExtractException {
		// step six: clone files inside destination dir
		out("\nClone files");

		for (int i = 0; true; i++) {
			String clone[] = { null, null };
			// 0: srcName, 1: destName
			clone = props.get("clone_" + Integer.toString(i), clone);

			if (clone[0] == null) {
				break;
			}

			try {
				copyFile(destinationPath + clone[0], destinationPath + clone[1]);
				createdFiles.put((destinationPath + clone[1]).toLowerCase(), null);
			} catch (final Exception ex) {
				throw new ExtractException(
						"Cloning " + destinationPath + clone[0] + " to " + destinationPath + clone[1] + " failed");
			}

			checkCancel();
		}
	}

	/**
	 * Step 5 of Extract thread run: copy stuff.
	 * 
	 * @param props the Properties for the game.
	 * @throws ExtractException if unable to copy stuff.
	 */
	private void copyStuff(final Props props) throws ExtractException {
		// step five: copy stuff
		out("\nCopy files");

		for (int i = 0; true; i++) {
			String copy[] = { null, null };
			// 0: srcName, 1: destName
			copy = props.get("copy_" + Integer.toString(i), copy);

			if (copy[0] == null) {
				break;
			}

			try {
				copyFile(SOURCE_PATH + copy[0], destinationPath + copy[1]);
				createdFiles.put((destinationPath + copy[1]).toLowerCase(), null);
			} catch (final Exception ex) {
				throw new ExtractException(
						"Copying " + SOURCE_PATH + copy[0] + " to " + destinationPath + copy[1] + " failed");
			}

			checkCancel();
		}
	}

	/**
	 * Step 4 of Extract thread run: create directories.
	 * 
	 * @param props the Properties for the game.
	 * @throws ExtractException if unable to create directories.
	 */
	private void createDirectories(final Props props) throws ExtractException {
		// step four: create directories
		out("\nCreate directories");

		for (int i = 0; true; i++) {
			// 0: path
			final String path = props.get("mkdir_" + Integer.toString(i), "");

			if (path.length() == 0) {
				break;
			}

			out(path);
			final String destFolder = destinationPath + path;
			final File dest = new File(destFolder);
			final boolean mkdirs = dest.mkdirs();

			if (!mkdirs) {
				System.out.println("Unable to make dir " + dest);
			}

			checkCancel();
		}
	}

	/**
	 * Step 3 of Extract thread run: extract the objects.
	 * 
	 * @param props  the Properties for the game.
	 * @param sprite Extract graphics from "Lemming for Win95" SPR data files.
	 * @throws ExtractException if unable to extract the objects.
	 */
	private void extractObjects(final Props props, final ExtractSPR sprite) throws ExtractException {
		// step three: extract the objects
		out("\nExtracting objects");

		for (int i = 0; true; i++) {
			String object[] = { null, null, null, null };
			// 0:SPR, 1:PAL, 2:resource, 3:path
			object = props.get("objects_" + Integer.toString(i), object);

			if (object[0] == null) {
				break;
			}

			out(object[0]);
			final String pathname = destinationPath + object[3];
			final File dest = new File(pathname);
			final boolean mkdirs = dest.mkdirs();

			if (!mkdirs) {
				System.out.println("Unable to make dir " + pathname);
			}

			// load palette and sprite
			sprite.loadPalette(SOURCE_PATH + object[1]);
			sprite.loadSPR(SOURCE_PATH + object[0]);

			for (int j = 0; true; j++) {
				String member[] = { null, null, null };
				// 0:idx, 1:frames, 2:name
				member = props.get(object[2] + "_" + Integer.toString(j), member);

				if (member[0] == null) {
					break;
				}

				// save object
				createdFiles.put((destinationPath + addSeparator(object[3]) + member[2]).toLowerCase(), null);
				sprite.saveAnim(destinationPath + addSeparator(object[3]) + member[2],
						Integer.parseInt(member[0]), Integer.parseInt(member[1]));
				checkCancel();
			}
		}
	}

	/**
	 * Step 2 of Extract thread run: extract the styles and return Extract graphics
	 * from "Lemming for Win95" SPR data files.
	 * 
	 * @param props the Properties for the game.
	 * @return Extract graphics from "Lemming for Win95" SPR data files.
	 * @throws ExtractException if unable to extract the styles.
	 */
	private ExtractSPR extractStyles(final Props props) throws ExtractException {
		// step two: extract the styles
		out("\nExtracting styles");
		final ExtractSPR sprite = new ExtractSPR();

		for (int i = 0; true; i++) {
			String styles[] = { null, null, null, null };
			// 0:SPR, 1:PAL, 2:path, 3:fname
			styles = props.get("style_" + Integer.toString(i), styles);

			if (styles[0] == null) {
				break;
			}

			out(styles[3]);
			final String pathname = destinationPath + styles[2];
			final File dest = new File(pathname);
			final boolean mkdirs = dest.mkdirs();

			if (!mkdirs) {
				System.out.println("Unable to make dir " + pathname);
			}

			// load palette and sprite
			sprite.loadPalette(SOURCE_PATH + styles[1]);
			sprite.loadSPR(SOURCE_PATH + styles[0]);
			final String files[] = sprite.saveAll(destinationPath + addSeparator(styles[2]) + styles[3], false);

			for (int j = 0; j < files.length; j++) {
				createdFiles.put(files[j].toLowerCase(), null);
			}

			checkCancel();
		}

		return sprite;
	}

	/**
	 * Step 1 of Extract thread run: extract the levels.
	 * 
	 * @param props the properties for the game.
	 * @throws ExtractException if unable to retrieve the levels.
	 */
	private void extractLevels(final Props props) throws ExtractException {
		// step one: extract the levels
		out("\nExtracting levels");

		for (int i = 0; true; i++) {
			String lvls[] = { null, null };
			// 0: srcPath, 1: destPath
			lvls = props.get("level_" + Integer.toString(i), lvls);

			if (lvls[0] == null) {
				break;
			}

			extractLevels(SOURCE_PATH + lvls[0], destinationPath + lvls[1]);
			checkCancel();
		}
	}

	/**
	 * Get source path (WINLEMM) for extraction.
	 * 
	 * @return source path (WINLEMM) for extraction
	 */
	public static String getSOURCE_PATH() {
		return SOURCE_PATH;
	}

	/**
	 * Get destination path (Lemmini resource) for extraction.
	 * 
	 * @return destination path (Lemmini resource) for extraction
	 */
	public static String getResourcePath() {
		return destinationPath;
	}

	/**
	 * Extract all resources and create patch.ini if referencePath is not null
	 * 
	 * @param frame   parent frame
	 * @param srcPath WINLEMM directory
	 * @param dstPath target (installation) directory. May also be a relative path
	 *                inside JAR
	 * @param refPath the reference path with the original (wanted) files
	 * @throws ExtractException
	 */
	public static void extract(final JFrame frame, final String srcPath, final String dstPath, final String refPath)
			throws ExtractException {
		SOURCE_PATH = exchangeSeparators(addSeparator(srcPath));
		destinationPath = exchangeSeparators(addSeparator(dstPath));

		if (refPath != null) {
			referencePath = exchangeSeparators(addSeparator(refPath));
		}

		CRC_PATH = destinationPath; // ok, this is the wrong path, but this is executed once in a lifetime
		loader = Extract.class.getClassLoader();
		FolderDialog fDiag;

		do {
			fDiag = new FolderDialog(frame, true);
			fDiag.setParameters(SOURCE_PATH, destinationPath);
			fDiag.setVisible(true);

			if (!fDiag.getSuccess()) {
				throw new ExtractException("Extraction cancelled by user");
			}

			SOURCE_PATH = exchangeSeparators(addSeparator(fDiag.getSource()));
			destinationPath = exchangeSeparators(addSeparator(fDiag.getTarget()));
			// check if source path exists
			final File fSrc = new File(SOURCE_PATH);

			if (fSrc.exists()) {
				break;
			}

			JOptionPane.showMessageDialog(frame, "Source path " + SOURCE_PATH + " doesn't exist!", "Error",
					JOptionPane.ERROR_MESSAGE);
		} while (true);

		// open output dialog
		outputDiag = new OutputDialog(frame, true);

		// start thread
		threadException = null;
		thisThread = new Thread(new Extract());
		thisThread.start();
		outputDiag.setVisible(true);

		while (thisThread.isAlive()) {
			try {
				Thread.sleep(200);
			} catch (final InterruptedException ex) {
			}
		}

		if (threadException != null) {
			throw threadException;
		}
	}

	/**
	 * Extract the level INI files from LVL files
	 * 
	 * @param r    name of root folder (source of LVL files)
	 * @param dest destination folder for extraction (resource folder)
	 * @throws ExtractException
	 */
	private static void extractLevels(final String r, final String destin) throws ExtractException {
		// first extract the levels
		final File fRoot = new File(r);
		final FilenameFilter ff = new LvlFilter();
		final String root = addSeparator(r);
		final String destination = addSeparator(destin);
		final File dest = new File(destination);
		final boolean mkdirs = dest.mkdirs();

		if (!mkdirs) {
			System.out.println("Unable to make dir " + destination);
		}

		final File[] levels = fRoot.listFiles(ff);

		if (levels == null) {
			throw new ExtractException("Path " + root + " doesn't exist or IO error occured.");
		}

		for (int i = 0; i < levels.length; i++) {
			int pos;
			final String fIn = root + levels[i].getName();
			String fOut = levels[i].getName();
			pos = fOut.toLowerCase().indexOf(".lvl"); // MUST be there because of file filter
			fOut = destination + (fOut.substring(0, pos) + ".ini").toLowerCase();
			createdFiles.put(fOut.toLowerCase(), null);

			try {
				out(levels[i].getName());
				ExtractLevel.convertLevel(fIn, fOut);
			} catch (final Exception ex) {
				final String msg = ex.getMessage();

				if (msg != null && msg.length() > 0) {
					out(ex.getMessage());
				} else {
					out(ex.toString());
				}

				throw new ExtractException(msg);
			}
		}
	}

	/**
	 * Add separator "/" to path name (if there isn't one yet)
	 * 
	 * @param fName path name with or without separator
	 * @return path name with separator
	 */
	public static String addSeparator(final String fName) {
		int pos = fName.lastIndexOf(File.separator);

		if (pos != fName.length() - 1) {
			pos = fName.lastIndexOf("/");
		}

		if (pos != fName.length() - 1) {
			return fName + "/";
		} else {
			return fName;
		}
	}

	/**
	 * Exchange all Windows style file separators ("\") with Unix style seaparators
	 * ("/")
	 * 
	 * @param fName file name
	 * @return file name with only Unix style separators
	 */
	public static String exchangeSeparators(final String fName) {
		int pos;
		final StringBuffer sb = new StringBuffer(fName);

		while ((pos = sb.indexOf("\\")) != -1) {
			sb.setCharAt(pos, '/');
		}

		return sb.toString();
	}

	/**
	 * Copy a file.
	 * 
	 * @param source      full source file name including path
	 * @param destination full destination file name including path
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void copyFile(final String source, final String destination)
			throws FileNotFoundException, IOException {
		try (final FileInputStream fSrc = new FileInputStream(source);
				final FileOutputStream fDest = new FileOutputStream(destination);) {
			writeFromInputStreamToOutputStream(fSrc, fDest);
		}
	}

	/**
	 * Read data from the already opened input stream and write to the already
	 * opened output stream. Calling code is responsible for closing both, most
	 * likely by initializing them both in a try-with-resources block.
	 * 
	 * @param source      the source input stream.
	 * @param destination the destination output stream.
	 * @throws IOException if an I/O error occurs.
	 */
	public static void writeFromInputStreamToOutputStream(final InputStream source, final FileOutputStream destination)
			throws IOException {
		final byte buffer[] = new byte[4096];
		int len;

		while ((len = source.read(buffer)) != -1) {
			destination.write(buffer, 0, len);
		}
	}

	/**
	 * Read file into an array of byte.
	 * 
	 * @param fname file name
	 * @return array of byte
	 * @throws ExtractException
	 */
	public static byte[] readFile(final String fname) throws ExtractException {
		byte buf[] = null;

		try {
			final int len = (int) (new File(fname).length());

			try (final FileInputStream f = new FileInputStream(fname);) {
				buf = new byte[len];
				f.read(buf);
			}

			return buf;
		} catch (final FileNotFoundException ex) {
			throw new ExtractException("File " + fname + " not found");
		} catch (final IOException ex) {
			throw new ExtractException("IO exception while reading file " + fname);
		}
	}

	/**
	 * Find a file.
	 * 
	 * @param fname File name (without absolute path)
	 * @return URL to file
	 */
	public static URL findFile(final String fname) {
		URL retval = loader.getResource(fname);

		try {
			if (retval == null) {
				retval = new File(fname).toURI().toURL();
			}

			return retval;
		} catch (final MalformedURLException ex) {
		}

		return null;
	}

	/**
	 * Print string to output dialog.
	 * 
	 * @param s string to print
	 */
	public static void out(final String s) {
		// System.out.println(s);
		if (outputDiag != null) {
			outputDiag.print(s + "\n");
		}
	}

	/**
	 * Return cancel state of output dialog
	 * 
	 * @throws ExtractException
	 */
	public static void checkCancel() throws ExtractException {
		if (outputDiag.isCancelled()) {
			throw new ExtractException("Extraction cancelled by user");
		}
	}
}
