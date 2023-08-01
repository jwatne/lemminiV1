package game.lemmings;

import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

import game.Core;
import game.ResourceException;
import game.level.Mask;
import lemmini.Constants;
import tools.Props;
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
 * Utility class for loading images of Lemmings. Code moved from Lemming by John
 * Watne 07/2023.
 */
public final class LemmingImageLoader {
    /** name of the configuration file. */
    private static final String LEMM_INI_STR = "misc/lemming.ini";

    /**
     * Private default constructor for utility class.
     */
    private LemmingImageLoader() {

    }

    /**
     * Load images used for Lemming animations.
     *
     * @param cmp parent component
     * @throws ResourceException
     */
    public static void loadLemmings(final Component cmp)
            throws ResourceException {
        Lemming.setExplodeFont(new ExplodeFont(cmp));
        final MediaTracker tracker = new MediaTracker(cmp);
        // read lemmings definition file
        final String fn = Core.findResource(LEMM_INI_STR);
        final Props p = new Props();

        if (!p.load(fn)) {
            throw new ResourceException(LEMM_INI_STR);
        }

        final LemmingResource[] lr = new LemmingResource[Lemming.NUM_RESOURCES];
        Lemming.setLemmings(lr);
        // read lemmings
        final int[] def = {-1};

        for (int i = 0; true; i++) {
            int[] val = p.get("lemm_" + i, def);
            int type;

            if (val.length == Constants.THREE) {
                // frames, directions, animation type
                type = i;

                if (lr[type] == null) {
                    final BufferedImage sourceImg = ToolBox.imageToBuffered(
                            Core.loadImage(tracker, "misc/lemm_" + i + ".gif"),
                            Transparency.BITMASK);
                    try {
                        tracker.waitForAll();
                    } catch (final InterruptedException ex) {
                    }

                    setAnimationMode(lr, val, type, sourceImg);
                }
            } else {
                break;
            }

            // read mask
            type = readMask(tracker, p, def, i, type);

            // read indestructible mask
            type = readIndestructibleMask(tracker, p, def, i, type);

            // read foot position and size
            val = p.get("pos_" + i, def);

            if (val.length == Constants.THREE) {
                setFootPositionAndSize(lr, val, type);
            } else {
                break;
            }
        }
    }

    private static void setAnimationMode(final LemmingResource[] lr,
            final int[] val, final int type, final BufferedImage sourceImg) {
        lr[type] = new LemmingResource(sourceImg, val[0], val[1]);
        lr[type].setAnimMode((val[2] == 0) ? Animation.LOOP : Animation.ONCE);
    }

    private static int readMask(final MediaTracker tracker, final Props p,
            final int[] def, final int i, final int initialType)
            throws ResourceException {
        final LemmingResource[] lr = Lemming.getLemmings();
        var type = initialType;
        int[] val;
        val = p.get("mask_" + i, def);

        if (val.length == Constants.THREE) {
            // mask_Y: frames, directions, step
            type = i;
            final Image sourceImg = Core.loadImage(tracker,
                    "misc/mask_" + i + ".gif");
            final Mask mask = new Mask(
                    ToolBox.imageToBuffered(sourceImg, Transparency.BITMASK),
                    val[0]);
            lr[type].setMask(Direction.RIGHT, mask);
            final int dirs = val[1];

            if (dirs > 1) {
                final Mask maskLeft = new Mask(ToolBox.flipImageX(ToolBox
                        .imageToBuffered(sourceImg, Transparency.BITMASK)),
                        val[0]);
                lr[type].setMask(Direction.LEFT, maskLeft);
            }

            lr[type].setMaskStep(val[2]);
        }

        return type;
    }

    private static int readIndestructibleMask(final MediaTracker tracker,
            final Props p, final int[] def, final int i, final int initialType)
            throws ResourceException {
        var type = initialType;
        int[] val;
        val = p.get("imask_" + i, def);

        if (val.length == 2) {
            // mask_Y: type, frames, directions, step
            type = i;
            final Image sourceImg = Core.loadImage(tracker,
                    "misc/imask_" + i + ".gif");
            final Mask mask = new Mask(
                    ToolBox.imageToBuffered(sourceImg, Transparency.BITMASK),
                    val[0]);
            final LemmingResource[] lr = Lemming.getLemmings();
            lr[type].setImask(Direction.RIGHT, mask);
            final int dirs = val[1];

            if (dirs > 1) {
                final Mask maskLeft = new Mask(ToolBox.flipImageX(ToolBox
                        .imageToBuffered(sourceImg, Transparency.BITMASK)),
                        val[0]);
                lr[type].setImask(Direction.LEFT, maskLeft);
            }
        }

        return type;
    }

    private static void setFootPositionAndSize(final LemmingResource[] lr,
            final int[] val, final int type) {
        lr[type].setFootX(val[0]);
        lr[type].setFootY(val[1]);
        lr[type].setSize(val[2]);
    }

}
