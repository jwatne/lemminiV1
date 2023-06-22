package game;

import java.awt.Component;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

import tools.ToolBox;
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
 * Used to manage the font for the explosion counter.
 *
 * @author Volker Oth
 */
public class ExplodeFont {
    /** Number of animation frames. */
    private static final int ANIMATION_FRAMES = 5;

    /**
     * Constructor.
     *
     * @param cmp the parent component (main frame of the application).
     * @throws ResourceException
     */
    ExplodeFont(final Component cmp) throws ResourceException {
        final Image sourceImg = Core.loadImage("misc/countdown.gif", cmp);
        img = ToolBox.getAnimation(sourceImg, ANIMATION_FRAMES,
                Transparency.BITMASK);
    }

    /**
     * Get image for a counter value (0..9).
     *
     * @param num counter value (0..9)
     * @return image for the counter value.
     */
    BufferedImage getImage(final int num) {
        return img[num];
    }

    /** array of images for each counter value. */
    private final BufferedImage[] img;
}
