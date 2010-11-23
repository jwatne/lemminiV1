package Game;


import java.util.ArrayList;

import Tools.Props;

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

public abstract class AbstractCore implements Core {

	/** program properties */
	protected Props programProps;
	/** path of (extracted) resources */
	protected String resourcePath;
	/** current player */
	public Player player;
	/** name of program properties file */
	protected String programPropsFileStr;
	/** name of player properties file */
	protected String playerPropsFileStr;
	/** player properties */
	protected Props playerProps;
	/** list of all players */
	protected ArrayList<String> players;

	public AbstractCore() {
		super();
	}

	public Props getProgramProps() {
		return programProps;
	}

	public String getResourcePath() {
		return resourcePath;
	}

	/**
	 * Get String to resource in resource path.
	 * @param fname file name (without path)
	 * @return absolute path to resource
	 */
	public String findResource(final String fname) {
		return resourcePath+fname;
	}

	/**
	 * Store program properties.
	 */
	public void saveProgramProps() {
		programProps.save(programPropsFileStr);
		playerProps.set("defaultPlayer", player.getName());
		playerProps.save(playerPropsFileStr);
		player.store();
	}

	/**
	 * Get player name via index.
	 * @param idx player index
	 * @return player name
	 */
	public String getPlayer(final int idx) {
		return players.get(idx);
	}

	/**
	 * Get number of players.
	 * @return number of player.
	 */
	public int getPlayerNum() {
		if (players == null)
			return 0;
		return players.size();
	}

	/**
	 * Reset list of players.
	 */
	public void clearPlayers() {
		players.clear();
		playerProps.clear();
	}

	/**
	 * Add player.
	 * @param name player name
	 */
	public void addPlayer(final String name) {
		players.add(name);
		playerProps.set("player_"+(players.size()-1), name);
	}

}