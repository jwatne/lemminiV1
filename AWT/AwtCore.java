package AWT;
import java.awt.Component;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import Extract.Extract;
import Extract.ExtractException;
import GUI.LegalDialog;
import Game.Core;
import Game.GameController;
import Game.LemmException;
import Game.Player;
import Game.ResourceException;
import Graphics.Image;
import Tools.Props;
import Tools.ToolBox;

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
public class AwtCore implements Core {
	
	public static final Instance INSTANCE = new Instance();
	/** The revision string for resource compatibility - not necessarily the version number */
	private final static String REVISION = "0.80";
	/** name of the ini file */
	private final static String INI_NAME = "lemmings.ini";
	/** extensions accepted for level files in file dialog */
	public final static String[] LEVEL_EXTENSIONS = {"ini", "lvl"};
	/** extensions accepted for replay files in file dialog */
	public final static String[] REPLAY_EXTENSIONS = {"rpl"};

	/** program properties */
	private Props programProps;
	/** path of (extracted) resources */
	private String resourcePath;
	/** current player */
	public Player player;

	/** parent component (main frame) */
	private JFrame cmp;
	/** name of program properties file */
	private String programPropsFileStr;
	/** name of player properties file */
	private String playerPropsFileStr;
	/** player properties */
	private Props playerProps;
	/** list of all players */
	private ArrayList<String> players;

	/**
	 * Initialize some core elements.
	 * @param frame parent frame
	 * @param isWebstartApp true if this was started via Webstart, false otherwise
	 * @throws LemmException
	 */
	public void init(final JFrame frame, final boolean isWebstartApp) throws LemmException  {
		// get ini path
		if (isWebstartApp) {
			programPropsFileStr = ToolBox.INSTANCE.get().exchangeSeparators(System.getProperty("user.home"));
			programPropsFileStr = ToolBox.INSTANCE.get().addSeparator(programPropsFileStr);
		} else {
			String s = frame.getClass().getName().replace('.','/') + ".class";
			URL url = frame.getClass().getClassLoader().getResource(s);
			int pos;
			try {
				programPropsFileStr = URLDecoder.decode(url.getPath(),"UTF-8");
			} catch (UnsupportedEncodingException ex) {};
			// special handling for JAR
			if (( (pos=programPropsFileStr.toLowerCase().indexOf("file:")) != -1))
				programPropsFileStr = programPropsFileStr.substring(pos+5);
			if ( (pos=programPropsFileStr.toLowerCase().indexOf(s.toLowerCase())) != -1)
				programPropsFileStr = programPropsFileStr.substring(0,pos);

			/** @todo doesn't work if JAR is renamed...
			 *  Maybe it would be a better idea to search only for ".JAR" and then
			 *  for the first path separator...
			 */

			s = (frame.getClass().getName().replace('.','/') + ".jar").toLowerCase();
			if ( (pos=programPropsFileStr.toLowerCase().indexOf(s)) != -1)
				programPropsFileStr = programPropsFileStr.substring(0,pos);
		}
		programPropsFileStr += INI_NAME;
		// read main ini file
		programProps = new Props();

		if (!programProps.load(programPropsFileStr)) {// might exist or not - if not, it's created
			LegalDialog ld = new LegalDialog(null,true);
			ld.setVisible(true);
			if (!ld.isOk())
				throw new LemmException("User abort");
		}

		resourcePath = programProps.get("resourcePath", "");
		String sourcePath = programProps.get("sourcePath", "");
		String rev = programProps.get("revision", "");
		GameController.setMusicOn(programProps.get("music", false));
		GameController.setSoundOn(programProps.get("sound", true));
		double gain;
		gain = programProps.get("musicGain", 1.0);
		GameController.setMusicGain(gain);
		gain = programProps.get("soundGain", 1.0);
		GameController.setSoundGain(gain);
		GameController.setAdvancedSelect(programProps.get("advancedSelect", true));
		if (resourcePath.length()==0 || !REVISION.equalsIgnoreCase(rev)) {
			// extract resources
			try {
				Extract.extract(null, sourcePath, resourcePath, null, "patch");
				resourcePath = Extract.getResourcePath();
				programProps.set("resourcePath", ToolBox.INSTANCE.get().addSeparator(Extract.getResourcePath()));
				programProps.set("sourcePath", ToolBox.INSTANCE.get().addSeparator(Extract.getSourcePath()));
				programProps.set("revision", REVISION);
				programProps.save(programPropsFileStr);
			} catch (ExtractException ex) {
				programProps.set("resourcePath", ToolBox.INSTANCE.get().addSeparator(Extract.getResourcePath()));
				programProps.set("sourcePath", ToolBox.INSTANCE.get().addSeparator(Extract.getSourcePath()));
				programProps.save(programPropsFileStr);
				throw new LemmException("Ressource extraction failed\n"+ex.getMessage());
			}
		}
		System.gc(); // force garbage collection here before the game starts

		// read player names
		playerPropsFileStr = resourcePath+"players.ini";
		playerProps = new Props();
		playerProps.load(playerPropsFileStr);
		String defaultPlayer = playerProps.get("defaultPlayer", "default");
		players = new ArrayList<String>();
		for (int idx=0; true; idx++) {
			String p = playerProps.get("player_"+Integer.toString(idx), "");
			if (p.length() == 0)
				break;
			players.add(p);
		}
		if (players.size() == 0) {
			// no players yet, establish default player
			players.add("default");
			playerProps.set("player_0", "default");
		}
		player = new Player(defaultPlayer);


		cmp = frame;
	}

	/**
	 * Get parent component (main frame).
	 * @return parent component
	 */
	public Component getCmp() {
		return cmp;
	}
	
	public Props getProgramProps() {
		return programProps;
	}

	public int getWidth() {
		return cmp.getWidth();
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
	
	public void setTitle(String title) {
		cmp.setTitle(title);
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
	 * Output error message box in case of a missing resource.
	 * @param rsrc name missing of resource.
	 */
	public void resourceError(final String rsrc) {
		String out = "The resource "+rsrc+" is missing\n" +
		"Please restart to extract all resources.";
		JOptionPane.showMessageDialog(null,out,"Error",JOptionPane.ERROR_MESSAGE);
		// invalidate resources
		programProps.set("revision", "invalid");
		programProps.save(programPropsFileStr);
		System.exit(1);
	}

	/**
	 * Load an image from the resource path.
	 * @param tracker media tracker
	 * @param fName file name
	 * @return Image
	 * @throws ResourceException
	 */
	private java.awt.Image loadImage(final MediaTracker tracker, final String fName) throws ResourceException {
		String fileLoc = findResource(fName);
		if (fileLoc == null)
			return null;
		return loadImage(tracker, fileLoc, false);
	}

	/**
	 * Load an image from either the resource path or from inside the JAR (or the directory of the main class).
	 * @param tracker media tracker
	 * @param fName file name
	 * @param jar true: load from the jar/class path, false: load from resource path
	 * @return Image
	 * @throws ResourceException
	 */
	private static java.awt.Image loadImage(final MediaTracker tracker, final String fName, final boolean jar) throws ResourceException {
		java.awt.Image image;
		if (jar)
			image = Toolkit.getDefaultToolkit().createImage(ToolBox.INSTANCE.get().findFile(fName));
		else
			image = Toolkit.getDefaultToolkit().createImage(fName);
		if (image != null) {
			tracker.addImage(image, 0);
			try {
				tracker.waitForID(0);
				if (tracker.isErrorAny()) {
					image = null;
				}
			} catch (Exception ex) {
				image = null;
			}
		}
		if (image == null)
			throw new ResourceException(fName);
		return image;
	}

	/**
	 * Load an image from the resource path.
	 * @param fname file name
	 * @return Image
	 * @throws ResourceException
	 */
	public java.awt.Image loadImage(final String fname) throws ResourceException {
		MediaTracker tracker = new MediaTracker(getCmp());
		java.awt.Image img = loadImage(tracker, fname);
		if (img == null)
			throw new ResourceException(fname);
		return img;
	}
	
	public Image loadBitmaskImage(final String fname) throws ResourceException {
		return AwtToolBox.INSTANCE.get().ImageToBuffered(loadImage(fname), Transparency.BITMASK);
	}

	public Image loadTranslucentImage(final String fname) throws ResourceException {
		return AwtToolBox.INSTANCE.get().ImageToBuffered(loadImage(fname), Transparency.TRANSLUCENT);
	}

	public Image loadOpaqueImage(final String fname) throws ResourceException {
		return AwtToolBox.INSTANCE.get().ImageToBuffered(loadImage(fname), Transparency.OPAQUE);
	}

	/**
	 * Load an image from inside the JAR or the directory of the main class.
	 * @param fname
	 * @return Image
	 * @throws ResourceException
	 */
	public java.awt.Image loadImageJar(final String fname) throws ResourceException {
		MediaTracker tracker = new MediaTracker(getCmp());
		java.awt.Image img = loadImage(tracker, fname, true);
		if (img == null)
			throw new ResourceException(fname);
		return img;
	}

	public Image loadOpaqueImageJar(final String fname) throws ResourceException {
		return AwtToolBox.INSTANCE.get().ImageToBuffered(loadImageJar(fname), Transparency.OPAQUE);
	}
	
	public Image loadTranslucentImageJar(final String fname) throws ResourceException {
		return AwtToolBox.INSTANCE.get().ImageToBuffered(loadImageJar(fname), Transparency.TRANSLUCENT);
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

	public static class Instance {
		
		private AwtCore core;
		
		private Instance() {
			//prevent instantiation
		}
		
		public AwtCore get() {
			return core;
		}
		
		public void set(AwtCore core) {
			this.core = core;
			Core.INSTANCE.set(core);
		}
	}
}
