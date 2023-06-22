package lemmini;

import java.awt.Component;
import java.io.File;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;

import game.Core;
import game.Player;
import gui.PlayerDialog;
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
 * ActionListener for manage player menu item.
 */
public final class ManagePlayerMenuItemActionListener
        implements java.awt.event.ActionListener {
    /**
     * The calling {@link MenuCreator}.
     */
    private final MenuCreator menuCreator;
    /** Frame. */
    private Component frame;

    /**
     * Constructor.
     *
     * @param creator the menu creator.
     * @param parent  the parent frame.
     */
    ManagePlayerMenuItemActionListener(final MenuCreator creator,
            final Component parent) {
        this.menuCreator = creator;
        this.frame = parent;
    }

    /**
     * Private default no-args constructor: not used.
     */
    @SuppressWarnings("unused")
    private ManagePlayerMenuItemActionListener() {
        this.menuCreator = null;
    }

    @Override
    public void actionPerformed(final java.awt.event.ActionEvent e) {
        final Player corePlayer = Core.getPlayer();
        corePlayer.store(); // save player in case it is changed
        final PlayerDialog d = new PlayerDialog((JFrame) frame, true);
        d.setVisible(true);
        // blocked until dialog returns
        final List<String> players = d.getPlayers();

        if (players != null) {
            String player = corePlayer.getName(); // old player
            final int playerIdx = d.getSelection();

            if (playerIdx != -1) {
                player = players.get(playerIdx); // remember selected player
            }

            player = checkForPlayersToDelete(players, player);

            // rebuild players list
            Core.clearPlayers();

            // add default player if missing
            if (!players.contains("default")) {
                players.add("default");
            }

            // now copy all player and create properties
            for (int i = 0; i < players.size(); i++) {
                Core.addPlayer(players.get(i));
            }

            // select new default player
            final Player newDefaultPlayer = new Player(player);
            Core.setPlayer(newDefaultPlayer);

            // rebuild players menu
            this.menuCreator.setPlayerGroup(new ButtonGroup());
            this.menuCreator.getjMenuSelectPlayer().removeAll();

            for (int idx = 0; idx < Core.getPlayerNum(); idx++) {
                final JCheckBoxMenuItem item = this.menuCreator
                        .addPlayerItem(Core.getPlayer(idx));

                if (newDefaultPlayer.getName().equals(Core.getPlayer(idx))) {
                    item.setSelected(true);
                }
            }

            this.menuCreator.updateLevelMenus();
        }
    }

    /**
     * Checks for players to delete and deletes them. Returns the currentPlayer
     * or &quot;default&quot; if currentPlayer is not in the List of players.
     *
     * @param players       the List of players currently in the application.
     * @param currentPlayer the name of the current player.
     * @return currentPlayer, or &quot;default&quot; if currentPlayer is not in
     *         the List of players.
     */
    private String checkForPlayersToDelete(final List<String> players,
            final String currentPlayer) {
        String player = currentPlayer;

        // check for players to delete
        for (int i = 0; i < Core.getPlayerNum(); i++) {
            final String p = Core.getPlayer(i);

            if (!players.contains(p)) {
                final String pathname = Core.getResourcePath() + "players/" + p
                        + ".ini";
                final File f = new File(pathname);
                final boolean deleted = f.delete();

                if (!deleted) {
                    System.out.println("ERROR: " + pathname + " NOT DELETED!");
                }

                if (p.equals(player)) {
                    player = "default";
                }
            }
        }

        return player;
    }
}
