package AWT;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import Graphics.GraphicsOperation;

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

public class AwtGraphicsOperation implements GraphicsOperation {

	private AffineTransform affineTransform = new AffineTransform();

	@Override
	public void setScale(double sx, double sy) {
		affineTransform.setToScale(sx, sy);
	}

	@Override
	public void translate(double tx, double ty) {
		affineTransform.translate(tx, ty);
	}

	@Override
	public void execute(BufferedImage sourc, BufferedImage destination) {
		AffineTransformOp op = new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		op.filter(sourc, destination);
	}

}
