package Game;

import Graphics.Image;
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

/**
 * Well, this started as some kind of core class to collect all global stuff
 * Now lots of the functionality moved to GameController.
 * Would need some cleaning up, maybe remove the whole thing?
 * @author Volker Oth
 */
public interface Core {
	public static Instance INSTANCE = new Instance();
	/** extensions accepted for level files in file dialog */
	public final static String[] LEVEL_EXTENSIONS = {"ini", "lvl"};
	/** extensions accepted for replay files in file dialog */
	public final static String[] REPLAY_EXTENSIONS = {"rpl"};

	/**
	 * Returns the program properties.
	 * @return the program properties
	 */
	public Props getProgramProps();

	/**
	 * Returns the width of the game
	 */
	public int getWidth();
	
	/**
	 * Returns the resource path.
	 * @return the resource path
	 */
	public String getResourcePath();
	
	/**
	 * Get String to resource in resource path.
	 * @param fname file name (without path)
	 * @return absolute path to resource
	 */
	public String findResource(final String fname);

	/**
	 * Set the title
	 */
	public void setTitle(String title);
	
	/**
	 * Store program properties.
	 */
	public void saveProgramProps();

	/**
	 * Output error message box in case of a missing resource.
	 * @param rsrc name missing of resource.
	 */
	public void resourceError(final String rsrc);

	/**
	 * Load an image from the resource path.
	 * @param fname file name
	 * @return Image
	 * @throws ResourceException
	 */
	public Image loadBitmaskImage(final String fname) throws ResourceException;

	/**
	 * Load an image from the resource path.
	 * @param fname file name
	 * @return Image
	 * @throws ResourceException
	 */
	public Image loadTranslucentImage(final String fname) throws ResourceException;

	/**
	 * Load an image from the resource path.
	 * @param fname file name
	 * @return Image
	 * @throws ResourceException
	 */
	public Image loadOpaqueImage(final String fname) throws ResourceException;

	/**
	 * Load an image from inside the JAR or the directory of the main class.
	 * @param fname
	 * @return Image
	 * @throws ResourceException
	 */
	public Image loadOpaqueImageJar(final String fname) throws ResourceException;

	/**
	 * Load an image from inside the JAR or the directory of the main class.
	 * @param fname
	 * @return Image
	 * @throws ResourceException
	 */
	public Image loadTranslucentImageJar(final String fname) throws ResourceException;

	/**
	 * Get player name via index.
	 * @param idx player index
	 * @return player name
	 */
	public String getPlayer(final int idx);

	/**
	 * Get number of players.
	 * @return number of player.
	 */
	public int getPlayerNum();

	/**
	 * Reset list of players.
	 */
	public void clearPlayers();

	/**
	 * Add player.
	 * @param name player name
	 */
	public void addPlayer(final String name);

	public static class Instance {
		
		private Core core;
		
		private Instance() {
			//prevent instantiation
		}
		
		public Core get() {
			return core;
		}
		
		public void set(Core core) {
			this.core = core;
		}
	}
}
