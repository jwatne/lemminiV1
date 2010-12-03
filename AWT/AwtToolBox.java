package AWT;


import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import Graphics.GraphicsContext;
import Graphics.GraphicsOperation;
import Graphics.Image;
import Tools.AbstractToolBox;
import Tools.Cursor;
import Tools.JFileFilter;
import Tools.ToolBox;

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
public class AwtToolBox extends AbstractToolBox implements ToolBox {

	public static Instance INSTANCE = new Instance(); 
	
	private GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

	/**
	 * Creates a graphics operation
	 * @return the graphics operation
	 */
	public GraphicsOperation createGraphicsOperation() {
		return new AwtGraphicsOperation();
	}

	/**
	 * Creates a custom cursor from the image
	 * @param image
	 * @param width
	 * @param height
	 * @return the cursor
	 */
	public Cursor createCursor(Image image, int width, int height) {
		return new AwtCursor(Toolkit.getDefaultToolkit().createCustomCursor(getBufferedImage(image), new Point(width, height), ""));
	}

	/**
	 * Create a compatible buffered image.
	 * @param width width of image in pixels
	 * @param height height of image in pixels
	 * @param transparency {@link java.awt.Transparency}
	 * @return compatible buffered image
	 */
	public BufferedImage createImage(final int width, final int height, final int transparency) {
		BufferedImage b = gc.createCompatibleImage(width, height, transparency);
		return b;
	}

	public AwtImage createBackgroundImage(final int width, final int height) {
		return createBitmaskImage(width, height);
	}

	/**
	 * Create a compatible buffered image.
	 * @param width width of image in pixels
	 * @param height height of image in pixels
	 * @param transparency {@link java.awt.Transparency}
	 * @return compatible buffered image
	 */
	public AwtImage createBitmaskImage(final int width, final int height) {
		return new AwtImage(createImage(width, height, Transparency.BITMASK));
	}

	/**
	 * Create a compatible buffered image.
	 * @param width width of image in pixels
	 * @param height height of image in pixels
	 * @param transparency {@link java.awt.Transparency}
	 * @return compatible buffered image
	 */
	public AwtImage createOpaqueImage(final int width, final int height) {
		return new AwtImage(createImage(width, height, Transparency.OPAQUE));
	}

	/**
	 * Create a compatible buffered image.
	 * @param width width of image in pixels
	 * @param height height of image in pixels
	 * @param transparency {@link java.awt.Transparency}
	 * @return compatible buffered image
	 */
	public AwtImage createTranslucentImage(final int width, final int height) {
		return new AwtImage(createImage(width, height, Transparency.TRANSLUCENT));
	}

	/**
	 * Create a compatible buffered image from an image.
	 * @param img existing {@link java.awt.Image}
	 * @param transparency {@link java.awt.Transparency}
	 * @return compatible buffered image
	 */	public Image ImageToBuffered(final java.awt.Image img, final int transparency) {
		 BufferedImage bImg = createImage(img.getWidth(null), img.getHeight(null), transparency);
		 Graphics2D g = bImg.createGraphics();
		 g.drawImage(img, 0, 0, null);
		 g.dispose();
		 return new AwtImage(bImg);
	 }

	 /**
	  * Return an array of buffered images which contain an animation.
	  * @param img image containing all the frames one above each other
	  * @param frames number of frames
	  * @param transparency {@link java.awt.Transparency}
	  * @param width image width
	  * @return an array of buffered images which contain an animation
	  */
	 public Image[] getAnimation(final Image img, final int frames, final int width) {
		 return getAnimation(img, frames, AwtToolBox.INSTANCE.get().getBufferedImage(img).getColorModel().getTransparency(), width);
	 }

	 /**
	  * Return an array of buffered images which contain an animation.
	  * @param img image containing all the frames one above each other
	  * @param frames number of frames
	  * @param transparency {@link java.awt.Transparency}
	  * @param width image width
	  * @return an array of buffered images which contain an animation
	  */
	 public Image[] getAnimation(final Image img, final int frames, final int transparency, final int width) {
		 int height = img.getHeight()/frames;
		 // characters stored one above the other - now separate them into single images
		 ArrayList<Image> arrImg = new ArrayList<Image>(frames);
		 int y0 = 0;
		 for (int i=0; i<frames; i++, y0+=height) {
			 Image frame = new AwtImage(createImage(width, height, transparency));
			 GraphicsContext g = frame.createGraphicsContext();
			 g.drawImage(img, 0, 0, width, height, 0, y0, width, y0+height);
			 arrImg.add(frame);
			 g.dispose();
		 }
		 Image images[] = new Image[arrImg.size()];
		 return arrImg.toArray(images);
	 }

	 /**
	  * Show exception message box.
	  * @param ex exception
	  */
	 public void showException(final Throwable ex) {
		 String m;
		 m = "<html>";
		 m += ex.getClass().getName()+"<p>";
		 if (ex.getMessage() != null)
			 m += ex.getMessage() +"<p>";
		 StackTraceElement ste[] = ex.getStackTrace();
		 for (int i=0; i<ste.length; i++)
			 m += ste[i].toString()+"<p>";
		 m += "</html>";
		 ex.printStackTrace();
		 JOptionPane.showMessageDialog( null, m, "Error", JOptionPane.ERROR_MESSAGE );
		 ex.printStackTrace();
	 }

	 /**
	  * Open file dialog.
	  * @param parent parent frame
	  * @param path default file name
	  * @param ext array of allowed extensions
	  * @param load true: load, false: save
	  * @return absolute file name of selected file or null
	  */
	 public String getFileName(final Object parent, final String path, final String ext[], final boolean load) {
		 String p = path;
		 if (p.length() == 0)
			 p = ".";
		 JFileChooser jf = new JFileChooser(p);
		 if (ext != null) {
			 JFileFilter filter = new JFileFilter();
			 for (int i=0; i<ext.length; i++)
				 filter.addExtension(ext[i]);
			 jf.setFileFilter(filter);
		 }
		 jf.setFileSelectionMode(JFileChooser.FILES_ONLY );
		 if (!load)
			 jf.setDialogType(JFileChooser.SAVE_DIALOG);
		 int returnVal = jf.showDialog((Component)parent,null);
		 if(returnVal == JFileChooser.APPROVE_OPTION) {
			 File f = jf.getSelectedFile();
			 if (f != null)
				 return f.getAbsolutePath();
		 }
		 return null;
	 }
	 
	 public BufferedImage getBufferedImage(Image image) {
		 if (!(image instanceof AwtImage)) {
			 throw new IllegalArgumentException("image must be of type " + AwtImage.class.getName());
		 }
		 return ((AwtImage)image).getImage();
	 }

	 public static class Instance {
		 
		 private AwtToolBox toolBox;
		 
		 private Instance() {
			 //prevent instantiation
		 }
		 
		 public AwtToolBox get() {
			 return toolBox;
		 }
		 
		 public void set(AwtToolBox toolBox) {
			 this.toolBox = toolBox;
			 ToolBox.INSTANCE.set(toolBox);
		 }
	 }
}
