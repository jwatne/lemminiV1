package game.level;

import java.util.ArrayList;
import java.util.List;
import java.awt.Color;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import gameutil.Sprite;
import lemmini.Constants;
import tools.ToolBox;

/**
 * Class for processing a Level's objects when painting the Level. Code moved
 * from Level by John Watne 08/2023.
 */
public final class ObjectProcessor {
    /**
     * Maximum number of foreground objects.
     */
    private static final int MAX_FOREGROUND_OBJECTS = 4;
    /**
     * Combined foreground and background sprite objects.
     */
    private List<SpriteObject> oCombined;
    /**
     * Background sprite objects.
     */
    private List<SpriteObject> oBehind;
    /**
     * Foreground objects.
     */
    private List<SpriteObject> oFront;
    /**
     * A List of processed Entry objects.
     */
    private List<Entry> entry;

    /**
     * Returns Combined foreground and background sprite objects.
     *
     * @return Combined foreground and background sprite objects.
     */
    public List<SpriteObject> getoCombined() {
        return oCombined;
    }

    /**
     * Sets Combined foreground and background sprite objects.
     *
     * @param combined Combined foreground and background sprite objects.
     */
    public void setoCombined(final List<SpriteObject> combined) {
        this.oCombined = combined;
    }

    /**
     * Returns Background sprite objects.
     *
     * @return Background sprite objects.
     */
    public List<SpriteObject> getoBehind() {
        return oBehind;
    }

    /**
     * Sets Background sprite objects.
     *
     * @param background Background sprite objects.
     */
    public void setoBehind(final List<SpriteObject> background) {
        this.oBehind = background;
    }

    /**
     * Returns Foreground objects.
     *
     * @return Foreground objects.
     */
    public List<SpriteObject> getoFront() {
        return oFront;
    }

    /**
     * Sets Foreground objects.
     *
     * @param foreground Foreground objects.
     */
    public void setoFront(final List<SpriteObject> foreground) {
        this.oFront = foreground;
    }

    /**
     * The Level whose objects are to be processed.
     */
    private Level level;

    /**
     * Constructs the ObjectProcessor for the specified Level.
     *
     * @param processedLevel the Level whose objects are to be processed.
     */
    public ObjectProcessor(final Level processedLevel) {
        this.level = processedLevel;
    }

    /**
     * Process the objects for the Level being painted.
     *
     * @param bgImage background image to draw into
     * @param stencil Stencil for painting Level.
     * @return a List of processed Entry objects.
     */
    public List<Entry> processObjects(final BufferedImage bgImage,
            final Stencil stencil) {
        oCombined = new ArrayList<SpriteObject>(
                LevelLoader.MAX_NUM_SPRITE_OBJECTS);
        oBehind = new ArrayList<SpriteObject>(
                LevelLoader.MAX_NUM_SPRITE_OBJECTS);
        oFront = new ArrayList<SpriteObject>(MAX_FOREGROUND_OBJECTS);
        entry = new ArrayList<Entry>(MAX_FOREGROUND_OBJECTS);

        for (int n = 0; n < level.getObjects().size(); n++) {
            try {
                processNthLvlObject(bgImage, stencil, n);
            } catch (final ArrayIndexOutOfBoundsException ex) {
                // System.out.println("Array out of bounds");
            }
        }

        return entry;
    }

    /**
     * Process nth LevelObject.
     *
     * @param bgImage background image to draw into
     * @param stencil Stencil for painting Level.
     * @param n       LvlObject number.
     */
    private void processNthLvlObject(final BufferedImage bgImage,
            final Stencil stencil, final int n) {
        final LvlObject o = level.getObjects().get(n);
        final SpriteObject spr = getSpriteObjectForLevelObject(o);

        // animated
        determineForegroundBackgroundAndCombined(o, spr);

        // draw stencil (only for objects that are not upside down)
        if (!o.isUpsideDown()) {
            drawStencil(stencil, n, bgImage, spr);
        }

        // remove invisible pixels from all object frames that are "in
        // front"
        // for upside down objects, just create the upside down copy
        removeInvisiblePixelsFromFrontObjectFrames(bgImage, stencil, n, spr);
    }

    /**
     * Returns the SpriteObject for the LevelObject.
     *
     * @param o an Object like a door.
     * @return the SpriteObject for the LevelObject.
     */
    private SpriteObject getSpriteObjectForLevelObject(final LvlObject o) {
        final SpriteObject spr = new SpriteObject(
                level.getSprObjAvailable()[o.getId()]);
        spr.setX(o.getxPos());
        spr.setY(o.getyPos());

        // check for entries (ignore upside down entries)
        if (spr.getType() == SpriteObject.Type.ENTRY && !o.isUpsideDown()) {
            final Entry e = new Entry(o.getxPos() + spr.getWidth() / 2,
                    o.getyPos());
            e.setId(oCombined.size());
            entry.add(e);
            spr.setAnimMode(Sprite.Animation.NONE);
        }

        return spr;
    }

    /**
     * Add the SpriteObject to the List of foreground or background objects, and
     * the list of foreground and background objects combined.
     *
     * @param o   an Object like a door.
     * @param spr the SpriteObject added to the Lists.
     */
    private void determineForegroundBackgroundAndCombined(final LvlObject o,
            final SpriteObject spr) {
        final boolean inFront = isObjectInFront(o);

        if (inFront) {
            oFront.add(spr);
        } else {
            oBehind.add(spr);
        }

        oCombined.add(spr);
    }

    /**
     * Draw stencil (only for objects that are not upside down).
     *
     * @param stencil Stencil for painting Level.
     * @param n       LvlObject number.
     * @param bgImage background image to draw into
     * @param spr     the SpriteObject to draw.
     */
    private void drawStencil(final Stencil stencil, final int n,
            final BufferedImage bgImage, final SpriteObject spr) {
        final int bgWidth = bgImage.getWidth();
        final int bgHeight = bgImage.getHeight();

        for (int y = 0; y < spr.getHeight(); y++) {
            if (y + spr.getY() < 0 || y + spr.getY() >= bgHeight) {
                continue;
            }

            final int yLineStencil = (y + spr.getY()) * bgWidth;

            for (int x = 0; x < spr.getWidth(); x++) {
                // boolean pixOverdraw = false;
                if (x + spr.getX() < 0 || x + spr.getX() >= bgWidth) {
                    continue;
                }

                // manage collision mask
                manageCollisionMask(stencil, n, spr, y, yLineStencil, x);
            }
        }
    }

    /**
     * Manage the collision mask.
     *
     * @param stencil      Stencil for painting Level.
     * @param n            LvlObject number.
     * @param spr          the SpriteObject to draw.
     * @param y            y position within sprite.
     * @param yLineStencil y position for line within image.
     * @param x            x position within sprite.
     */
    private void manageCollisionMask(final Stencil stencil, final int n,
            final SpriteObject spr, final int y, final int yLineStencil,
            final int x) {
        // now read stencil
        int stencilVal = stencil.get(yLineStencil + spr.getX() + x);

        // store object type in mask and idx in higher byte
        if (spr.getType() != SpriteObject.Type.ENTRY
                && spr.getType() != SpriteObject.Type.PASSIVE) {
            if ((spr.getMask(x, y) & Constants.MAX_ALPHA) != 0) {
                // not transparent
                // avoid two objects on the same stencil
                // position
                // overlap makes it impossible to delete
                // pixels in objects (mask operations)
                if (Stencil.getObjectID(stencilVal) == 0) {
                    stencil.or(yLineStencil + spr.getX() + x,
                            spr.getMaskType() | Stencil.createObjectID(n));
                } // else: overlap - erased later in object
                  // instance
            }
        }
    }

    /**
     * Remove invisible pixels from all object frames that are "in front". For
     * upside down objects, just create the upside down copy.
     *
     * @param bgImage background image to draw into
     * @param stencil Stencil for painting Level.
     * @param n       LvlObject number.
     * @param spr     the SpriteObject to draw.
     */
    private void removeInvisiblePixelsFromFrontObjectFrames(
            final BufferedImage bgImage, final Stencil stencil, final int n,
            final SpriteObject spr) {
        final LvlObject o = level.getObjects().get(n);
        final boolean inFront = isObjectInFront(o);
        BufferedImage imgSpr;

        if (o.isUpsideDown() || inFront) {
            for (int frame = 0; frame < spr.getNumFrames(); frame++) {
                imgSpr = ToolBox.createImage(spr.getWidth(), spr.getHeight(),
                        Transparency.BITMASK);

                // get flipped or normal version
                imgSpr = getFlippedOrNormalVersionOfImageSprite(o, spr, imgSpr,
                        frame);
                // for "in front" objects the really drawn pixels have
                // to be determined
                determinePixelsToDraw(stencil, n, bgImage, spr, imgSpr);
                spr.setImage(frame, imgSpr);
            }
        }
    }

    /**
     * Returns affine transform for flipping SpriteObject.
     *
     * @param spr the SpriteObject to draw.
     * @return affine transform for flipping SpriteObject.
     */
    private AffineTransformOp getAffineTransformToFlipSprite(
            final SpriteObject spr) {
        AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
        tx.translate(0, -spr.getHeight());
        final AffineTransformOp op = new AffineTransformOp(tx,
                AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op;
    }

    /**
     * Returns flipped or normal version of image Sprite.
     *
     * @param o                  an Object like a door.
     * @param spr                the SpriteObject to draw.
     * @param initialSpriteImage initial (normal) version of image Sprite.
     * @param frame              the frame number.
     * @return flipped or normal version of image Sprite.
     */
    private BufferedImage getFlippedOrNormalVersionOfImageSprite(
            final LvlObject o, final SpriteObject spr,
            final BufferedImage initialSpriteImage, final int frame) {
        BufferedImage imgSpr = initialSpriteImage;
        // affine transform for flipping
        final AffineTransformOp op = getAffineTransformToFlipSprite(spr);

        if (o.isUpsideDown()) {
            // flip the image vertically
            imgSpr = op.filter(spr.getImage(frame), imgSpr);
        } else {
            final WritableRaster rImgSpr = imgSpr.getRaster();
            rImgSpr.setRect(spr.getImage(frame).getRaster());
            // just copy
        }

        return imgSpr;
    }

    /**
     * Indicates whether the object is in front.
     *
     * @param o an Object like a door.
     * @return <code>true</code> if the object is in front.
     */
    private boolean isObjectInFront(final LvlObject o) {
        final boolean drawOnVis = o
                .getPaintMode() == LvlObject.MODE_VIS_ON_TERRAIN;
        final boolean noOverwrite = o
                .getPaintMode() == LvlObject.MODE_VIS_ON_TERRAIN;
        final boolean inFront = (drawOnVis || !noOverwrite);
        return inFront;
    }

    /**
     * Determine which pixels to draw for &quot;in front&quot; objects.
     *
     * @param stencil Stencil for painting Level.
     * @param n       LvlObject number.
     * @param bgImage background image to draw into
     * @param spr     the SpriteObject to draw.
     * @param imgSpr  flipped or normal version of sprite image.
     */
    private void determinePixelsToDraw(final Stencil stencil, final int n,
            final BufferedImage bgImage, final SpriteObject spr,
            final BufferedImage imgSpr) {
        final int bgWidth = bgImage.getWidth();
        final int bgHeight = bgImage.getHeight();
        final LvlObject o = level.getObjects().get(n);
        final boolean inFront = isObjectInFront(o);

        if (inFront) {
            for (int y = 0; y < spr.getHeight(); y++) {
                if (y + spr.getY() < 0 || y + spr.getY() >= bgHeight) {
                    continue;
                }

                processSpriteObjectRow(bgWidth, stencil, n, spr, imgSpr, y);
            }
        }
    }

    /**
     * Process a row of a SpriteObject.
     *
     * @param bgWidth width of the background image to draw into.
     * @param stencil Stencil for painting Level.
     * @param n       LvlObject number.
     * @param spr     the SpriteObject to draw.
     * @param imgSpr  flipped or normal version of sprite image.
     * @param y       the row number within the SpriteObject image.
     */
    private void processSpriteObjectRow(final int bgWidth,
            final Stencil stencil, final int n, final SpriteObject spr,
            final BufferedImage imgSpr, final int y) {
        final int yLineStencil = (y + spr.getY()) * bgWidth;

        for (int x = 0; x < spr.getWidth(); x++) {
            if (x + spr.getX() < 0 || x + spr.getX() >= bgWidth) {
                continue;
            }

            // now read stencil
            final int stencilVal = stencil.get(yLineStencil + spr.getX() + x);
            // hack for overlap:
            final int id = Stencil.getObjectID(stencilVal);
            boolean paint = shouldPaint(n, spr, id);
            // sprite screenBuffer pixel
            final int imgCol = imgSpr.getRGB(x, y);

            if ((imgCol & Constants.MAX_ALPHA) == 0) {
                continue;
            }

            if (!paint) {
                imgSpr.setRGB(x, y, imgCol & Color.WHITE.getRGB());
                // set transparent
            }
        }
    }

    /**
     * Determines whether to paint the sprite.
     *
     * @param n          LvlObject number.
     * @param spr        the SpriteObject to draw.
     * @param stencilVal stencil value at position being examined.
     * @return <code>true</code> if painting the sprite.
     */
    private boolean shouldPaint(final int n, final SpriteObject spr,
            final int stencilVal) {
        final LvlObject o = level.getObjects().get(n);
        final boolean drawOnVis = o
                .getPaintMode() == LvlObject.MODE_VIS_ON_TERRAIN;
        final boolean noOverwrite = o
                .getPaintMode() == LvlObject.MODE_NO_OVERWRITE;
        final boolean drawFull = o.getPaintMode() == LvlObject.MODE_FULL;
        final int stencilValMasked = stencilVal & Stencil.MSK_WALK_ON;
        boolean paint = drawFull || (stencilValMasked != 0 && drawOnVis)
                || (stencilValMasked == 0 && noOverwrite);
        final int id = Stencil.getObjectID(stencilVal);

        // check if a different interactive object
        // was already entered at this pixel
        // position
        // however: exits must always be painted
        // also: passive objects will always be
        // painted
        if (spr.getType() != SpriteObject.Type.PASSIVE
                && spr.getType() != SpriteObject.Type.EXIT && id != 0
                && id != n) {
            paint = false;
        }

        return paint;
    }
}
