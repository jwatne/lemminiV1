package game.replay;

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
 * Storage class for replay level info.
 *
 * @author Volker Oth
 */
public class ReplayLevelInfo {
    /** name of level pack. */
    private String levelPack;
    /** difficulty level. */
    private int diffLevel;
    /** level number. */
    private int lvlNumber;

    /**
     * Set name of level pack.
     *
     * @param levelPackName name of level pack
     */
    public void setLevelPack(final String levelPackName) {
        this.levelPack = levelPackName;
    }

    /**
     * Get name of level pack.
     *
     * @return name of level pack
     */
    public String getLevelPack() {
        return levelPack;
    }

    /**
     * Set difficulty level.
     *
     * @param level difficulty level
     */
    public void setDiffLevel(final int level) {
        this.diffLevel = level;
    }

    /**
     * Get difficulty level.
     *
     * @return difficulty level
     */
    public int getDiffLevel() {
        return diffLevel;
    }

    /**
     * Set level number.
     *
     * @param level level number
     */
    public void setLvlNumber(final int level) {
        this.lvlNumber = level;
    }

    /**
     * Get level number.
     *
     * @return level number
     */
    public int getLvlNumber() {
        return lvlNumber;
    }
}
