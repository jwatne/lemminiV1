package Tools;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import Graphics.GraphicsOperation;
import Graphics.Image;

/*
 * Copyright 2010 Arne Limburg
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

public abstract class AbstractToolBox implements ToolBox {

	 /**
	  * Return an array of buffered images which contain an animation.
	  * @param img image containing all the frames one above each other
	  * @param frames number of frames
	  * @param transparency {@link java.awt.Transparency}
	  * @return an array of buffered images which contain an animation
	  */
	 public Image[] getAnimation(final Image img, final int frames) {
		 return getAnimation(img, frames, img.getWidth());
	 }

	 /**
	  * Flip image in X direction.
	  * @param source image to flip
	  * @return flipped image
	  */
	 public Image flipImageX(final Image source) {
		 Image target = createBitmaskImage(source.getWidth(), source.getHeight());
		 GraphicsOperation operation = createGraphicsOperation();
		 operation.setScale(-1, 1);
		 operation.translate(-source.getWidth(), 0);
		 operation.execute(source, target);
		 return target;
	 }

	 /**
	  * Use the Loader to find a file.
	  * @param fname file name
	  * @return URL of the file
	  */
	 public URL findFile(final String fname) {
		 ClassLoader loader = getClass().getClassLoader();
		 return loader.getResource(fname);
	 }

	 /**
	  * Add (system default) path separator to string (if there isn't one already).
	  * @param fName String containing path name
	  * @return String that ends with the (system default) path separator for sure
	  */
	 public String addSeparator(final String fName) {
		 int pos = fName.lastIndexOf(File.separator);
		 if (pos != fName.length()-1)
			 pos = fName.lastIndexOf("/");
		 if (pos != fName.length()-1)
			 return fName + "/";
		 else return fName;
	 }

	 /**
	  * Exchange any DOS style path separator ("\") with a Unix style separator ("/").
	  * @param fName String containing file/path name
	  * @return String with only Unix style path separators
	  */
	 public String exchangeSeparators(final String fName) {
		 int pos;
		 StringBuffer sb = new StringBuffer(fName);
		 while ( (pos = sb.indexOf("\\")) != -1 )
			 sb.setCharAt(pos,'/');
		 return sb.toString();
	 }

	 /**
	  * Return file name from path.
	  * @param path String of a path with a file name
	  * @return String containing only the file name
	  */
	 public String getFileName(final String path) {
		 int p1 = path.lastIndexOf("/");
		 int p2 = path.lastIndexOf("\\");
		 if (p2 > p1)
			 p1 = p2;
		 if (p1 < 0)
			 p1 = 0;
		 else
			 p1++;
		 return path.substring(p1);
	 }

	 /**
	  * Returns the extension (".XXX") of a filename without the dot.
	  * @param path String containing file name
	  * @return String containing only the extension (without the dot) or null (if no extension found)
	  */
	 public String getExtension(final String path) {
		 int p1 = path.lastIndexOf("/");
		 int p2 = path.lastIndexOf("\\");
		 int p = path.lastIndexOf(".");
		 if (p==-1 || p<p1 || p<p2)
			 return null;
		 return path.substring(p+1);
	 }


	 /**
	  * Returns the first few bytes of a file to check its type.
	  * @param fname Filename of the file
	  * @param num Number of bytes to return
	  * @return Array of bytes (size num) from the beginning of the file
	  */
	 public byte[] getFileID(final String fname, final int num) {
		 byte buf[] = new byte[num];
		 File f = new File(fname);
		 if (f.length() < num)
			 return null;
		 try {
			 FileInputStream fi = new FileInputStream(fname);
			 fi.read(buf);
			 fi.close();
		 } catch (Exception ex) {
			 return null;
		 }
		 return buf;
	 }

	 /**
	  * Get path name from absolute file name.
	  * @param path absolute file name
	  * @return path name without the separator
	  */
	 public String getPathName(final String path) {
		 int p1 = path.lastIndexOf("/");
		 int p2 = path.lastIndexOf("\\");
		 if (p2 > p1)
			 p1 = p2;
		 if (p1 < 0)
			 p1 = 0;
		 return path.substring(0,p1);
	 }

}
