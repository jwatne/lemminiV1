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

public class Color {

	public static final Color BLACK = new Color(0, 0, 0);

	public static final Color RED = new Color(255, 0, 0);

	public static final Color YELLOW = new Color(255, 255, 0);
	
	private int argb;
	
	public Color(int rgb) {
		this.argb = 0xff000000 | rgb;
	}
	
    public Color(int r, int g, int b) {
        this(r, g, b, 255);
    }

    public Color(int r, int g, int b, int a) {
    	argb = ((a & 0xff) << 24) | ((r & 0xff) << 16) | ((g & 0xff) << 8) | ((b & 0xFf) << 0);
	}

	public int getARGB() {
		return argb;
	}
}
