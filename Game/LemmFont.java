package Game;

import Graphics.GraphicsContext;
import Graphics.Image;
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
 * Handle the main bitmap font.
 *
 * @author Volker Oth
 */
public class LemmFont {

	/** Colors */
	public static enum Color {
		/** green color */
		GREEN,
		/** blue color */
		BLUE,
		/** red color */
		RED,
		/** brown/yellow color */
		BROWN,
		/** turquoise/cyan color */
		TURQUOISE,
		/** violet color */
		VIOLET
	}

	/** default width of one character in pixels */
	private final static int SPACING = 18;
	/** character map */
	private final static String CHARS = "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_´abcdefghijklmnopqrstuvwxyz{|}~";

	/** width of one character in pixels */
	private static int width;
	/** height of one character pixels */
	private static int height;
	/** array of array of images [color,character] */
	static private Image img[][];

	/**
	 * Initialization.
	 * @throws ResourceException
	 */
	static public void init() throws ResourceException {
		Image sourceImg = Core.INSTANCE.get().loadBitmaskImage("misc/lemmfont.gif");

		width = SPACING; //sourceImg.getWidth(null);
		height = sourceImg.getHeight()/CHARS.length();
		Image redImg = ToolBox.INSTANCE.get().createBitmaskImage(sourceImg.getWidth(), sourceImg.getHeight());
		Image blueImg = ToolBox.INSTANCE.get().createBitmaskImage(sourceImg.getWidth(), sourceImg.getHeight());
		Image turquoiseImg = ToolBox.INSTANCE.get().createBitmaskImage(sourceImg.getWidth(), sourceImg.getHeight());
		Image brownImg = ToolBox.INSTANCE.get().createBitmaskImage(sourceImg.getWidth(), sourceImg.getHeight());
		Image violetImg = ToolBox.INSTANCE.get().createBitmaskImage(sourceImg.getWidth(), sourceImg.getHeight());
		img = new Image[7][];
		img[0] = ToolBox.INSTANCE.get().getAnimation(sourceImg,CHARS.length(),width);
		for (int xp=0; xp<sourceImg.getWidth(); xp++)
			for (int yp=0; yp<sourceImg.getHeight(); yp++) {
				int col = sourceImg.getRGB(xp, yp); // A R G B
				int a = col & 0xff000000; // transparent part
				int r = (col >> 16) & 0xff;
				int g = (col >> 8) & 0xff;
				int b = col & 0xff;
				// patch image to red version by swapping red and green components
				col = a | (g<<16) | (r<<8) | b;
				redImg.setRGB(xp, yp, col);
				// patch image to blue version by swapping blue and green components
				col = a | (r<<16) | (b<<8) | g;
				blueImg.setRGB(xp, yp, col);
				// patch image to turquoise version by setting blue component to value of green component
				col = a | (r<<16) | (g<<8) | g;
				turquoiseImg.setRGB(xp, yp, col);
				// patch image to yellow version by setting red component to value of green component
				col = a | (g<<16) | (g<<8) | b;
				brownImg.setRGB(xp, yp, col);
				// patch image to violet version by exchanging red and blue with green
				col = a | (g<<16) | (((r+b)<<7)&0xff00) | g;
				violetImg.setRGB(xp, yp, col);
			}
		img[Color.RED.ordinal()] = ToolBox.INSTANCE.get().getAnimation(redImg,CHARS.length(),width);
		img[Color.BLUE.ordinal()] = ToolBox.INSTANCE.get().getAnimation(blueImg,CHARS.length(),width);
		img[Color.TURQUOISE.ordinal()] = ToolBox.INSTANCE.get().getAnimation(turquoiseImg,CHARS.length(),width);
		img[Color.BROWN.ordinal()] = ToolBox.INSTANCE.get().getAnimation(brownImg,CHARS.length(),width);
		img[Color.VIOLET.ordinal()] = ToolBox.INSTANCE.get().getAnimation(violetImg,CHARS.length(),width);
	}

	/**
	 * Draw string into graphics object in given color.
	 * @param g graphics object to draw to.
	 * @param s string to draw.
	 * @param sx x coordinate in pixels
	 * @param sy y coordinate in pixels
	 * @param color Color
	 */
	static public void strImage(final GraphicsContext g, final String s, final int sx, final int sy, final Color color) {
		for (int i=0, x = sx; i<s.length();i++,x+=SPACING) {
			char c = s.charAt(i);
			if (c==' ')
				continue;
			int pos = CHARS.indexOf(c);
			if (pos > -1 && pos < CHARS.length()) {
				g.drawImage(img[color.ordinal()][pos], x, sy);
			}
		}
		return;
	}

	/**
	 * Draw string into graphics object in given color.
	 * @param g graphics object to draw to.
	 * @param s string to draw.
	 * @param color Color
	 */
	static public void strImage(final GraphicsContext g, final String s, final Color color) {
		strImage(g, s, 0, 0, color);
		return;
	}

	/**
	 * Create image of string in given color.
	 * @param s string to draw
	 * @param color Color
	 * @return a buffered image of the needed size that contains an image of the given string
	 */
	static public Image strImage(final String s, final Color color) {
		Image image = ToolBox.INSTANCE.get().createBitmaskImage(width*s.length(), height);
		strImage(image.createGraphicsContext(), s, color);
		return image;
	}

	/**
	 * Create image of string in default color (green).
	 * @param s string to draw
	 * @return a buffered image of the needed size that contains an image of the given string
	 */
	static public Image strImage(final String s) {
		return strImage(s, Color.GREEN);
	}

	/**
	 * Draw string into graphics object in default color (green).
	 * @param g graphics object to draw to.
	 * @param s string to draw.
	 */
	static public void strImage(final GraphicsContext g, final String s) {
		strImage(g, s, Color.GREEN);
	}

	/**
	 * Get width of one character in pixels.
	 * @return width of one character in pixels
	 */
	public static int getWidth() {
		return width;
	}

	/**
	 * Get height of one character in pixels.
	 * @return height of one character in pixels
	 */
	public static int getHeight() {
		return height;
	}

}
