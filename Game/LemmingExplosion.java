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

/**
 * Class for handling exploding Lemmings. Moved from Lemming by John Watne
 * 06/2023.
 */
public class LemmingExplosion {
    /** explosion counter is decreased every second. */
    private static final int MAX_EXPLODE_CTR = 1000000
            / GameController.MICROSEC_PER_FRAME;
    /** explosion counter when nuked. */
    private int explodeNumCtr;
    /** counter used to manage the explosion. */
    private int explodeCtr;

    /**
     * Initialize the explosion handler's attributes on construction.
     */
    public LemmingExplosion() {
        explodeNumCtr = 0;
    }

    /**
     * Indicates whether the Lemming has finished exploding.
     *
     * @return <code>true</code> if the Lemming has finished exploding.
     */
    public boolean checkExplodeState() {
        boolean explode = false;

        // first check explode state
        if (explodeNumCtr != 0) {
            if (++explodeCtr >= MAX_EXPLODE_CTR) {
                explodeCtr -= MAX_EXPLODE_CTR;
                explodeNumCtr--;

                if (explodeNumCtr == 0) {
                    explode = true;
                }
            }
        }

        return explode;
    }

    /**
     * Returns explosion counter when nuked.
     *
     * @return explosion counter when nuked.
     */
    public final int getExplodeNumCtr() {
        return explodeNumCtr;
    }

    /**
     * Sets explosion counter when nuked.
     *
     * @param counter explosion counter when nuked.
     */
    public final void setExplodeNumCtr(final int counter) {
        this.explodeNumCtr = counter;
    }

    /**
     * Returns counter used to manage explosion.
     *
     * @return counter used to manage explosion.
     */
    public final int getExplodeCtr() {
        return explodeCtr;
    }

    /**
     * Sets counter used to manage explosion.
     *
     * @param counter counter used to manage explosion.
     */
    public final void setExplodeCtr(final int counter) {
        this.explodeCtr = counter;
    }

}
