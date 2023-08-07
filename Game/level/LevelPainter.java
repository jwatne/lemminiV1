package game.level;

import java.awt.Component;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.util.List;

import game.Steel;
import game.Terrain;
import lemmini.Constants;

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
 * Class for painting a Level. Code moved from Level by John Watne 08/2023.
 */
public class LevelPainter {
    /**
     * The Level to be painted.
     */
    private final Level level;

    /**
     * Constructs a LevelPainter for the specified Level.
     *
     * @param levelToPaint the Level to be painted.
     */
    public LevelPainter(final Level levelToPaint) {
        this.level = levelToPaint;
    }

    /**
     * Paint a level.
     *
     * @param bgImage background image to draw into
     * @param cmp     parent component
     * @param s       stencil to reuse
     * @return stencil of this level
     */
    public Stencil paintLevel(final BufferedImage bgImage, final Component cmp,
            final Stencil s) {
        // flush all resources
        flushLevelResources();
        // the screenBuffer should be big enough to hold the level
        // returns stencil buffer;
        final int bgWidth = bgImage.getWidth();
        final int bgHeight = bgImage.getHeight();
        // try to reuse old stencil
        final Stencil stencil = getStencil(bgImage, s);
        // paint terrain
        paintTerrain(bgImage, stencil);
        // now for the animated objects
        final ObjectProcessor objectProcessor = paintAnimatedObjects(bgImage,
                stencil);
        // paint steel tiles into stencil
        paintSteelTilesIntoStencil(bgWidth, bgHeight, stencil);
        SpriteObject[] sprObjects = getCombinedObjects(objectProcessor);
        level.setSprObjects(sprObjects);
        SpriteObject[] sprObjFront = getForegroundObjects(objectProcessor);
        level.setSprObjFront(sprObjFront);
        SpriteObject[] sprObjBehind = getBackgroundObjects(objectProcessor);
        level.setSprObjBehind(sprObjBehind);
        return stencil;
    }

    private SpriteObject[] getBackgroundObjects(
            final ObjectProcessor objectProcessor) {
        final List<SpriteObject> oBehind = objectProcessor.getoBehind();
        SpriteObject[] sprObjBehind = new SpriteObject[oBehind.size()];
        sprObjBehind = oBehind.toArray(sprObjBehind);
        return sprObjBehind;
    }

    private SpriteObject[] getForegroundObjects(
            final ObjectProcessor objectProcessor) {
        final List<SpriteObject> oFront = objectProcessor.getoFront();
        SpriteObject[] sprObjFront = new SpriteObject[oFront.size()];
        sprObjFront = oFront.toArray(sprObjFront);
        return sprObjFront;
    }

    private SpriteObject[] getCombinedObjects(
            final ObjectProcessor objectProcessor) {
        final List<SpriteObject> oCombined = objectProcessor.getoCombined();
        SpriteObject[] sprObjects = new SpriteObject[oCombined.size()];
        sprObjects = oCombined.toArray(sprObjects);
        return sprObjects;
    }

    private void flushLevelResources() {
        level.setSprObjFront(null);
        level.setSprObjBehind(null);
        level.setSprObjects(null);
        level.setEntries(null);
    }

    private Stencil getStencil(final BufferedImage bgImage, final Stencil s) {
        Stencil stencil;
        final int bgWidth = bgImage.getWidth();

        if (s != null && s.getWidth() == bgWidth
                && s.getHeight() == bgImage.getHeight()) {
            s.clear();
            stencil = s;
        } else {
            stencil = new Stencil(bgWidth, bgImage.getHeight());
        }
        return stencil;
    }

    private void paintTerrain(final BufferedImage bgImage,
            final Stencil stencil) {
        final List<Terrain> terrain = level.getTerrain();

        for (int n = 0; n < terrain.size(); n++) {
            final Terrain t = terrain.get(n);
            final Image i = level.getTiles()[t.getId()];
            final int width = i.getWidth(null);
            final int height = i.getHeight(null);

            final int[] source = new int[width * height];
            final PixelGrabber pixelgrabber = new PixelGrabber(i, 0, 0, width,
                    height, source, 0, width);

            try {
                pixelgrabber.grabPixels();
            } catch (final InterruptedException interruptedexception) {
            }

            try {
                paintTerrain(bgImage, stencil, i, source, t);
            } catch (final ArrayIndexOutOfBoundsException ex) {
            }
        }
    }

    private ObjectProcessor paintAnimatedObjects(final BufferedImage bgImage,
            final Stencil stencil) {
        final ObjectProcessor objectProcessor = new ObjectProcessor(level);
        final List<Entry> entry = objectProcessor.processObjects(bgImage,
                stencil);
        Entry[] entries = new Entry[entry.size()];
        entries = entry.toArray(entries);
        level.setEntries(entries);
        return objectProcessor;
    }

    private void paintSteelTilesIntoStencil(final int bgWidth,
            final int bgHeight, final Stencil stencil) {
        final List<Steel> steel = level.getSteel();

        for (int n = 0; n < steel.size(); n++) {
            final Steel stl = steel.get(n);
            final int sx = stl.getxPos();
            final int sy = stl.getyPos();

            for (int y = 0; y < stl.getHeight(); y++) {
                if (y + sy < 0 || y + sy >= bgHeight) {
                    continue;
                }

                final int yLineStencil = (y + sy) * bgWidth;

                for (int x = 0; x < stl.getWidth(); x++) {
                    if (x + sx < 0 || x + sx >= bgWidth) {
                        continue;
                    }

                    int stencilVal = stencil.get(yLineStencil + x + sx);
                    // only allow steel on brick
                    if ((stencilVal & Stencil.MSK_BRICK) != 0) {
                        stencilVal &= ~Stencil.MSK_BRICK;
                        stencilVal |= Stencil.MSK_STEEL;
                        stencil.set(yLineStencil + x + sx, stencilVal);
                    }
                }
            }
        }
    }

    private void paintTerrain(final BufferedImage bgImage,
            final Stencil stencil, final Image i, final int[] source,
            final Terrain t) {
        final int width = i.getWidth(null);
        final int height = i.getHeight(null);
        final int bgWidth = bgImage.getWidth();
        final int bgHeight = bgImage.getHeight();
        final int tx = t.getxPos();
        final int ty = t.getyPos();
        final boolean upsideDown = (t.getModifier()
                & Terrain.MODE_UPSIDE_DOWN) != 0;
        final boolean overwrite = (t.getModifier()
                & Terrain.MODE_NO_OVERWRITE) == 0;
        final boolean remove = (t.getModifier() & Terrain.MODE_REMOVE) != 0;

        for (int y = 0; y < height; y++) {
            if (y + ty < 0 || y + ty >= bgHeight) {
                continue;
            }

            final int yLineStencil = (y + ty) * bgWidth;
            int yLine;

            if (upsideDown) {
                yLine = (height - y - 1) * width;
            } else {
                yLine = y * width;
            }

            for (int x = 0; x < width; x++) {
                if (x + tx < 0 || x + tx >= bgWidth) {
                    continue;
                }

                final int col = source[yLine + x];

                // ignore transparent pixels
                if ((col & Constants.MAX_ALPHA) == 0) {
                    continue;
                }

                boolean paint = false;

                if (!overwrite) {
                    // don't overwrite -> only paint if background is
                    // transparent
                    if (stencil
                            .get(yLineStencil + tx + x) == Stencil.MSK_EMPTY) {
                        paint = true;
                    }
                } else if (remove) {
                    bgImage.setRGB(x + tx, y + ty, 0 /* bgCol */);
                    stencil.set(yLineStencil + tx + x, Stencil.MSK_EMPTY);
                } else {
                    paint = true;
                }

                if (paint) {
                    bgImage.setRGB(x + tx, y + ty, col);
                    stencil.set(yLineStencil + tx + x, Stencil.MSK_BRICK);
                }
            }
        }
    }
}
