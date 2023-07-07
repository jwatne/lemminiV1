package game.lemmings;
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

import game.ExplosionHandler;
import game.LemmingExplosion;
import game.SoundController;
import game.Type;
import game.level.Level;
import game.level.Mask;
import game.level.Stencil;
import lemmini.Constants;

/**
 * Class for handling bomber Lemmings. Logic moved from Lemming class by John
 * Watne 07/2023.
 */
public class Bomber {
    /**
     * The Lemming owning this instance of the Bomber class.
     */
    private Lemming lemming;

    /**
     * Constructs a Bomber for the specified Lemming.
     *
     * @param owner the Lemming owning this instance of the Bomber class.
     */
    public Bomber(final Lemming owner) {
        this.lemming = owner;
    }

    /**
     * Let the Lemming explode.
     *
     * @param type Lemming's skill/type.
     */
    public void explode(final Type type) {
        SoundController.getSound().play(SoundController.SND_EXPLODE);
        // create particle explosion
        ExplosionHandler.addExplosion(lemming.midX(), lemming.midY());
        lemming.setHasDied(true);
        lemming.changeType(type, Type.BOMBER);
        // consider height difference between lemming and mask
        final Mask m = lemming.getLemRes().getMask(Direction.RIGHT);
        // check if lemming is standing on steel
        final int sy = lemming.getY() + 1;
        final int x = lemming.getX();

        if (x > 0 && x < Level.WIDTH && sy > 0 && sy < Level.HEIGHT) {
            m.eraseMask(x - m.getWidth() / 2,
                    lemming.midY() - m.getHeight() / 2 + Constants.THREE, 0,
                    Stencil.MSK_STEEL);
        }
    }

    /**
     * Animates bomber.
     *
     * @return the updated y value for the owning Lemming.
     */
    public int animateBomber() {
        int y = lemming.getY();
        int free;
        free = lemming.freeBelow(Floater.FLOATER_STEP);

        if (free == Faller.FALL_DISTANCE_FORCE_FALL) {
            y += Faller.FALLER_STEP;
        } else {
            y += free;
        }

        lemming.getFaller().crossedLowerBorder();
        return y;
    }

    /**
     * Indicates whether the owning Lemming can be changed to a bomber.
     *
     * @return <code>true</code> if the owning Lemming can be changed to a
     *         bomber, <code>false</code> if the Lemming has already been
     *         changed to a bomber.
     */
    public boolean canChangeToBomber() {
        final LemmingExplosion exploder = lemming.getExploder();

        if (exploder.getExplodeNumCtr() == 0) {
            exploder.setExplodeNumCtr(Constants.FIVE);
            exploder.setExplodeCtr(0);
            return true;
        } else {
            return false;
        }
    }

}
