import java.io.File;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;

import GUI.PlayerDialog;
import Game.Core;
import Game.Player;

public final class ManagePlayerMenuItemActionListener implements java.awt.event.ActionListener {
    /**
     * The calling {@link MenuCreator}.
     */
    private final MenuCreator menuCreator;

    /**
     * @param menuCreator
     */
    ManagePlayerMenuItemActionListener(final MenuCreator menuCreator) {
        this.menuCreator = menuCreator;
    }

    @Override
    public void actionPerformed(final java.awt.event.ActionEvent e) {
        Core.player.store(); // save player in case it is changed
        final PlayerDialog d = new PlayerDialog((JFrame) Core.getCmp(), true);
        d.setVisible(true);
        // blocked until dialog returns
        final List<String> players = d.getPlayers();

        if (players != null) {
            String player = Core.player.getName(); // old player
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
            Core.player = new Player(player);

            // rebuild players menu
            this.menuCreator.playerGroup = new ButtonGroup();
            this.menuCreator.jMenuSelectPlayer.removeAll();

            for (int idx = 0; idx < Core.getPlayerNum(); idx++) {
                final JCheckBoxMenuItem item = this.menuCreator.addPlayerItem(Core.getPlayer(idx));

                if (Core.player.getName().equals(Core.getPlayer(idx))) {
                    item.setSelected(true);
                }
            }

            this.menuCreator.updateLevelMenus();
        }
    }

    /**
     * Checks for players to delete and deletes them. Returns the currentPlayer or
     * &quot;default&quot; if currentPlayer is not in the List of players.
     * 
     * @param players       the List of players currently in the application.
     * @param currentPlayer the name of the current player.
     * @return currentPlayer, or &quot;default&quot; if currentPlayer is not in the
     *         List of players.
     */
    private String checkForPlayersToDelete(final List<String> players, final String currentPlayer) {
        String player = currentPlayer;

        // check for players to delete
        for (int i = 0; i < Core.getPlayerNum(); i++) {
            final String p = Core.getPlayer(i);

            if (!players.contains(p)) {
                final File f = new File(Core.resourcePath + "players/" + p + ".ini");
                f.delete();

                if (p.equals(player)) {
                    player = "default";
                }
            }
        }

        return player;
    }
}