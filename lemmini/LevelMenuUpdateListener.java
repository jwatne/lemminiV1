package lemmini;

import game.Core;
import game.GroupBitfield;
import game.LevelPack;
import game.UpdateListener;
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
import gameutil.FaderHandler;

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
     * @param app the Lemmini application.
     */
    LevelMenuUpdateListener(final Lemmini app) {
        this.lemmini = app;
    }

    @Override
    public final void update() {
        if (FaderHandler.getCurLevelPackIdx() != 0) { // 0 is the dummy pack
            LevelPack lvlPack = FaderHandler
                    .getLevelPack(FaderHandler.getCurLevelPackIdx());
            String pack = lvlPack.getName();
            String diff = lvlPack.getDiffLevels()[FaderHandler
                    .getCurDiffLevel()];
            // get next level
            int num = FaderHandler.getCurLevelNumber() + 1;

            if (num >= lvlPack
                    .getLevels(FaderHandler.getCurDiffLevel()).length) {
                num = FaderHandler.getCurLevelNumber();
            }

            // set next level as available
            GroupBitfield bf = Core.getPlayer().setAvailable(pack, diff, num);
            // update the menu
            this.lemmini.updateLevelMenu(pack, diff, bf);
        }
    }
}
