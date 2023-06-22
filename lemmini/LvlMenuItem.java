package lemmini;

import javax.swing.JMenuItem;
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
 * Specialized menu item for level selection menus.
 *
 * @author Volker Oth
 */
public class LvlMenuItem extends JMenuItem {
    private static final long serialVersionUID = 0x01;

    /** index of level pack. */
    private int levelPack;
    /** index of difficulty level. */
    private int diffLevel;
    /** level number. */
    private int level;

    /**
     * Constructor.
     *
     * @param text level name
     * @param pack index level pack
     * @param diff index of difficulty level
     * @param lvl  level number
     */
    LvlMenuItem(final String text, final int pack, final int diff,
            final int lvl) {
        super(text);
        levelPack = pack;
        diffLevel = diff;
        level = lvl;
    }

    /**
     * Returns index of level pack.
     *
     * @return index of level pack.
     */
    public final int getLevelPack() {
        return levelPack;
    }

    /**
     * Sets index of level pack.
     *
     * @param index index of level pack.
     */
    public final void setLevelPack(final int index) {
        this.levelPack = index;
    }

    /**
     * Returns index of difficulty level.
     *
     * @return index of difficulty level.
     */
    public final int getDiffLevel() {
        return diffLevel;
    }

    /**
     * Sets index of difficulty level.
     *
     * @param index index of difficulty level.
     */
    public final void setDiffLevel(final int index) {
        this.diffLevel = index;
    }

    /**
     * Returns level number.
     *
     * @return level number.
     */
    public final int getLevel() {
        return level;
    }

    /**
     * Sets level number.
     *
     * @param levelNumber level number.
     */
    public final void setLevel(final int levelNumber) {
        this.level = levelNumber;
    }
}
