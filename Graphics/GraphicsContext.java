package Graphics;

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

public interface GraphicsContext {

	void drawImage(Image image, int x, int y);

	void drawImage(Image image, int x, int y, int width, int height);

	void drawImage(Image image, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2);

	void setClip(int x, int y, int width, int height);

	void setBackground(Color color);

	void clearRect(int x, int y, int width, int height);

	void setColor(Color color);

	void drawRect(int x, int y, int width, int height);

	void dispose();

	void fillRect(int x, int y, int width, int height);

	void grabPixels(Image image, int x, int y, int w, int h, int[] pix, int off, int scansize);

	void copy(Image source, Image target);

}
