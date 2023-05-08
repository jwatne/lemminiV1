package lemmini;

import Game.Core;
import Game.GameController;
import Game.GroupBitfield;
import Game.LevelPack;
import Game.UpdateListener;

/**
 * Listener to inform the GUI of the player's progress.
 * 
 * @author Volker Oth
 */
public class LevelMenuUpdateListener implements UpdateListener {
    /**
     *
     */
    private final Lemmini lemmini;

    /**
     * @param lemmini
     */
    LevelMenuUpdateListener(Lemmini lemmini) {
        this.lemmini = lemmini;
    }

    @Override
    public void update() {
        if (GameController.getCurLevelPackIdx() != 0) { // 0 is the dummy pack
            LevelPack lvlPack = GameController.getLevelPack(GameController.getCurLevelPackIdx());
            String pack = lvlPack.getName();
            String diff = lvlPack.getDiffLevels()[GameController.getCurDiffLevel()];
            // get next level
            int num = GameController.getCurLevelNumber() + 1;

            if (num >= lvlPack.getLevels(GameController.getCurDiffLevel()).length) {
                num = GameController.getCurLevelNumber();
            }

            // set next level as available
            GroupBitfield bf = Core.player.setAvailable(pack, diff, num);
            // update the menu
            this.lemmini.updateLevelMenu(pack, diff, bf);
        }
    }
}