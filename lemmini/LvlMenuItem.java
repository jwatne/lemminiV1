package lemmini;

import javax.swing.JMenuItem;

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
