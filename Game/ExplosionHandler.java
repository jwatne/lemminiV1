package game;
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

import java.awt.Graphics2D;
import java.util.Iterator;
import java.util.LinkedList;

import game.level.Explosion;

/**
 * Utility that handles explosions for a Level. Code extracted from
 * GameController by John Watne 07/2023.
 */
public final class ExplosionHandler {
    /** list of all active explosions. */
    private static LinkedList<Explosion> explosions;

    /**
     * Private default constructor for utility class.
     */
    private ExplosionHandler() {

    }

    /**
     * Returnslist of all active explosions.
     *
     * @return list of all active explosions.
     */
    public static LinkedList<Explosion> getExplosions() {
        return explosions;
    }

    /**
     * Draw the explosions.
     *
     * @param g      graphics object
     * @param width  width of screen in pixels
     * @param height height of screen in pixels
     * @param xOfs   horizontal level offset in pixels
     */
    public static void drawExplosions(final Graphics2D g, final int width,
            final int height, final int xOfs) {
        synchronized (explosions) {
            for (final Explosion e : explosions) {
                e.draw(g, width, height, xOfs);
            }
        }
    }

    /**
     * Add a new explosion.
     *
     * @param x x coordinate in pixels.
     * @param y y coordinate in pixels.
     */
    public static void addExplosion(final int x, final int y) {
        // create particle explosion
        synchronized (explosions) {
            explosions.add(new Explosion(x, y));
        }
    }

    /**
     * Loop through ad animate any explosions.
     */
    public static void handleExplosions() {
        synchronized (explosions) {
            final Iterator<Explosion> it = explosions.iterator();

            while (it.hasNext()) {
                final Explosion e = it.next();

                if (e.isFinished()) {
                    it.remove();
                } else {
                    e.update();
                }
            }
        }
    }

    /**
     * Clear the list of explosions when initializing a Level.
     */
    public static void initLevel() {
        explosions.clear();
    }

    /**
     * Initialization.
     */
    public static void init() {
        explosions = new LinkedList<Explosion>();
    }

}
