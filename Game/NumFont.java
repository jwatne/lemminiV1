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
 * Handle small number font.
 * Meant to print out values between 0 and 99.
 *
 * @author Volker Oth
 */
public class NumFont {

	/** width in pixels */
	private static int width;
	/** height in pixels */
	private static int height;
	/** array of images - one for each cipher 0..9 */
	private static Image numImg[];

	/**
	 * Load and initialize the font.
	 * @throws ResourceException
	 */
	public static void init() throws ResourceException {
		Image sourceImg = Core.INSTANCE.get().loadOpaqueImage("misc/numfont.gif");
		Image img[] = ToolBox.INSTANCE.get().getAnimation(sourceImg,10);
		width = sourceImg.getWidth();
		height = sourceImg.getHeight()/10;
		numImg = new Image[100];
		for (int i=0; i<100; i++) {
			numImg[i] = ToolBox.INSTANCE.get().createOpaqueImage(width*2, height);
			GraphicsContext g = numImg[i].createGraphicsContext();
			g.drawImage(img[i/10], 0, 0);
			g.drawImage(img[i%10], width, 0);
			g.dispose();
		}
	}

	/**
	 * Get an image for a number between 0 and 99
	 * @param n number (0..99)
	 * @return image of the number
	 */
	public static Image numImage(final int n) {
		int num;
		if (n>99)
			num = 99;
		else if (n<0)
			num = 0;
		else num = n;
		return numImg[num];
	}
}
