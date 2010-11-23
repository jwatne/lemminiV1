package AWT;

import java.awt.image.BufferedImage;

import Graphics.GraphicsContext;

public class AwtImage implements Graphics.Image {

	private BufferedImage image;
	
	public AwtImage(BufferedImage image) {
		this.image = image;
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	@Override
	public GraphicsContext createGraphicsContext() {
		return new AwtGraphicsContext(image.createGraphics());
	}

	@Override
	public int getWidth() {
		return image.getWidth();
	}

	@Override
	public int getHeight() {
		return image.getHeight();
	}

	@Override
	public int getRGB(int x, int y) {
		return image.getRGB(x, y);
	}

	@Override
	public void setRGB(int x, int y, int rgb) {
		image.setRGB(x, y, rgb);
	}
}
