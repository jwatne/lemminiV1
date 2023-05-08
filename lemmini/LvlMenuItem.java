package lemmini;
import javax.swing.JMenuItem;

/**
 * Specialized menu item for level selection menus.
 * 
 * @author Volker Oth
 */
public class LvlMenuItem extends JMenuItem {
    private final static long serialVersionUID = 0x01;

    /** index of level pack */
    int levelPack;
    /** index of difficulty level */
    int diffLevel;
    /** level number */
    int level;

    /**
     * Constructor
     * 
     * @param text level name
     * @param pack index level pack
     * @param diff index of difficulty level
     * @param lvl  level number
     */
    LvlMenuItem(final String text, final int pack, final int diff, final int lvl) {
        super(text);
        levelPack = pack;
        diffLevel = diff;
        level = lvl;
    }
}