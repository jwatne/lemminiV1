package AWT;

import java.awt.Graphics2D;
import java.awt.image.PixelGrabber;
import java.awt.image.WritableRaster;

import Graphics.Color;
import Graphics.GraphicsContext;
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

public class AwtGraphicsContext implements GraphicsContext {

	private Graphics2D graphics;
	
	public AwtGraphicsContext(Graphics2D graphics) {
		this.graphics = graphics;
	}
	
	@Override
	public void clearRect(int x, int y, int width, int height) {
		graphics.clearRect(x, y, width, height);
	}

	@Override
	public void dispose() {
		graphics.dispose();
	}

	@Override
	public void drawImage(Image image, int x, int y) {
		graphics.drawImage(((AwtImage)image).getImage(), x, y, null);
	}

	@Override
	public void drawImage(Image image, int x, int y, int width, int height) {
		graphics.drawImage(((AwtImage)image).getImage(), x, y, width, height, null);
	}

	@Override
	public void drawImage(Image image, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2) {
		graphics.drawImage(((AwtImage)image).getImage(), dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
	}

	@Override
	public void drawRect(int x, int y, int width, int height) {
		graphics.drawRect(x, y, width, height);
	}

	@Override
	public void fillRect(int x, int y, int width, int height) {
		graphics.fillRect(x, y, width, height);
	}

	@Override
	public void setBackground(Color bgColor) {
		graphics.setBackground(new java.awt.Color(bgColor.getARGB(), true));
	}

	@Override
	public void setClip(int x, int y, int width, int height) {
		graphics.setClip(x, y, width, height);
	}

	@Override
	public void setColor(Color color) {
		graphics.setColor(new java.awt.Color(color.getARGB(), true));
	}

	public void grabPixels(Image image, int x, int y, int w, int h, int[] pix, int off, int scansize) {
		PixelGrabber pixelgrabber = new PixelGrabber(((AwtImage)image).getImage(), x, y, w, h, pix, off, scansize);
		try {
			pixelgrabber.grabPixels();
		} catch (InterruptedException interruptedexception) {}
	}

	@Override
	public void copy(Image source, Image target) {
		WritableRaster rImgSpr = ((AwtImage)target).getImage().getRaster();
		rImgSpr.setRect(((AwtImage)source).getImage().getRaster()); // just copy
	}
}
