package Tools;


import java.awt.Component;
import java.awt.image.BufferedImage;
import java.net.URL;

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
 * Selection of utility functions.
 *
 * @author Volker Oth
 */
public interface ToolBox {

	public static Instance INSTANCE = new Instance();
	
	/**
	 * Create a compatible buffered image.
	 * @param width width of image in pixels
	 * @param height height of image in pixels
	 * @param transparency {@link java.awt.Transparency}
	 * @return compatible buffered image
	 */
	public BufferedImage createImage(final int width, final int height, final int transparency);

	 /**
	  * Return an array of buffered images which contain an animation.
	  * @param img image containing all the frames one above each other
	  * @param frames number of frames
	  * @param transparency {@link java.awt.Transparency}
	  * @return an array of buffered images which contain an animation
	  */
	 public BufferedImage[] getAnimation(final BufferedImage img, final int frames, final int transparency);

	 /**
	  * Return an array of buffered images which contain an animation.
	  * @param img image containing all the frames one above each other
	  * @param frames number of frames
	  * @param transparency {@link java.awt.Transparency}
	  * @param width image width
	  * @return an array of buffered images which contain an animation
	  */
	 public BufferedImage[] getAnimation(final BufferedImage img, final int frames, final int transparency, final int width);

	 /**
	  * Flip image in X direction.
	  * @param img image to flip
	  * @return flipped image
	  */
	 public BufferedImage flipImageX(final BufferedImage img);

	 /**
	  * Use the Loader to find a file.
	  * @param fname file name
	  * @return URL of the file
	  */
	 public URL findFile(final String fname);

	 /**
	  * Add (system default) path separator to string (if there isn't one already).
	  * @param fName String containing path name
	  * @return String that ends with the (system default) path separator for sure
	  */
	 public String addSeparator(final String fName);

	 /**
	  * Exchange any DOS style path separator ("\") with a Unix style separator ("/").
	  * @param fName String containing file/path name
	  * @return String with only Unix style path separators
	  */
	 public String exchangeSeparators(final String fName);

	 /**
	  * Return file name from path.
	  * @param path String of a path with a file name
	  * @return String containing only the file name
	  */
	 public String getFileName(final String path);

	 /**
	  * Returns the extension (".XXX") of a filename without the dot.
	  * @param path String containing file name
	  * @return String containing only the extension (without the dot) or null (if no extension found)
	  */
	 public String getExtension(final String path);

	 /**
	  * Returns the first few bytes of a file to check its type.
	  * @param fname Filename of the file
	  * @param num Number of bytes to return
	  * @return Array of bytes (size num) from the beginning of the file
	  */
	 public byte[] getFileID(final String fname, final int num);

	 /**
	  * Get path name from absolute file name.
	  * @param path absolute file name
	  * @return path name without the separator
	  */
	 public String getPathName(final String path);

	 /**
	  * Show exception message box.
	  * @param ex exception
	  */
	 public void showException(final Throwable ex);

	 /**
	  * Open file dialog.
	  * @param parent parent frame
	  * @param path default file name
	  * @param ext array of allowed extensions
	  * @param load true: load, false: save
	  * @return absolute file name of selected file or null
	  */
	 public String getFileName(final Component parent, final String path, final String ext[], final boolean load);

	 public static class Instance {
		 
		 private ToolBox toolBox;
		 
		 private Instance() {
			 //prevent instantiation
		 }
		 
		 public ToolBox get() {
			 return toolBox;
		 }
		 
		 public void set(ToolBox toolBox) {
			 this.toolBox = toolBox;
		 }
	 }
}
