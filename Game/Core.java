package game;

import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import GUI.LegalDialog;
import Tools.Props;
import Tools.ToolBox;
import extract.Extract;
import extract.ExtractException;

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
 * Well, this started as some kind of core class to collect all global stuff Now
 * lots of the functionality moved to GameController. Would need some cleaning
 * up, maybe remove the whole thing?
 *
 * @author Volker Oth
 */
public final class Core {
    /**
     * Vertical padding for draw height, in pixels.
     */
    private static final int DRAW_HEIGHT_PADDING = 90;
    /**
     * The internal draw width, in pixels.
     */
    private static final int INTERNAL_DRAW_WIDTH = 800;
    /**
     * Length of &quot;file:&quot; prefix text.
     */
    private static final int FILE_LENGTH = 5;
    /**
     * The revision string for resource compatibility - not necessarily the
     * version number.
     */
    private static final String REVISION = "0.80";
    /** name of the ini file. */
    private static final String INI_NAME = "lemmings.ini";
    /** extensions accepted for level files in file dialog. */
    public static final List<String> LEVEL_EXTENSIONS = Collections
            .unmodifiableList(Arrays.asList("ini", "lvl"));
    /** extensions accepted for replay files in file dialog. */
    public static final List<String> REPLAY_EXTENSIONS = Collections
            .unmodifiableList(Arrays.asList("rpl"));
    /** height of menu and icon bar in pixels. */
    private static final int WIN_OFS = 120;

    /** program properties. */
    private static Props programProps;

    /**
     * Returns program properties.
     *
     * @return program properties.
     */
    public static Props getProgramProps() {
        return programProps;
    }

    /**
     * Sets program properties.
     *
     * @param properties program properties.
     */
    public static void setProgramProps(final Props properties) {
        Core.programProps = properties;
    }

    /** path of (extracted) resources. */
    private static String resourcePath;

    /**
     * Returns path of (extracted) resources.
     *
     * @return path of (extracted) resources.
     */
    public static String getResourcePath() {
        return resourcePath;
    }

    /**
     * Sets path of (extracted) resources.
     *
     * @param path path of (extracted) resources.
     */
    public static void setResourcePath(final String path) {
        Core.resourcePath = path;
    }

    /** current player. */
    private static Player player;

    /**
     * Returns current player.
     *
     * @return current player.
     */
    public static Player getPlayer() {
        return player;
    }

    /**
     * Sets current player.
     *
     * @param currentPlayer current player.
     */
    public static void setPlayer(final Player currentPlayer) {
        Core.player = currentPlayer;
    }

    /** name of program properties file. */
    private static String programPropsFileStr;
    /** name of player properties file. */
    private static String playerPropsFileStr;
    /** player properties. */
    private static Props playerProps;
    /** list of all players. */
    private static ArrayList<String> players;

    /** Zoom scale. */
    private static double scale;

    /**
     * Private constructor for utility class.
     */
    private Core() {

    }

    /**
     * Initialize some core elements.
     *
     * @param frame parent frame
     * @throws LemmException
     * @throws IOException
     */
    public static void init(final JFrame frame)
            throws LemmException, IOException {
        // get ini path
        String s = frame.getClass().getName().replace('.', '/') + ".class";
        System.out.println("*** s: " + s);
        final URL url = frame.getClass().getClassLoader().getResource(s);
        int pos;

        try {
            programPropsFileStr = URLDecoder.decode(url.getPath(), "UTF-8");
            System.out.println(
                    "*** Initial programPropsFileStr: " + programPropsFileStr);
        } catch (final UnsupportedEncodingException ex) {
        }

        // special handling for JAR
        pos = programPropsFileStr.toLowerCase().indexOf("file:");
        final boolean runFromJarFile = (pos != -1);

        if (runFromJarFile) {
            programPropsFileStr = programPropsFileStr
                    .substring(pos + FILE_LENGTH);
        }

        pos = programPropsFileStr.toLowerCase().indexOf(s.toLowerCase());

        if (pos != -1) {
            programPropsFileStr = programPropsFileStr.substring(0, pos);
        }

        if (runFromJarFile) {
            System.out.println(
                    "*** updated programPropsFileStr:" + programPropsFileStr);
            final String currentDirectory = Paths.get("").toAbsolutePath()
                    .toString();
            System.out.println("*** current directory: " + currentDirectory);
            Path currentFolderPath = Paths.get(currentDirectory, "config");
            final String currentIniFolder = currentFolderPath.toAbsolutePath()
                    .toString();
            System.out.println("*** Current ini folder: " + currentIniFolder);
            programPropsFileStr = Paths.get(currentIniFolder, INI_NAME)
                    .toAbsolutePath().toString();
            System.out.println(
                    "*** New method ini file path: " + programPropsFileStr);
            Files.createDirectories(currentFolderPath);
        } else {
            /*
             * @todo doesn't work if JAR is renamed... Maybe it would be a
             * better idea to search only for ".JAR" and then for the first path
             * separator...
             */
            s = (frame.getClass().getName().replace('.', '/') + ".jar")
                    .toLowerCase();
            System.out.println("*** updated s: " + s);
            pos = programPropsFileStr.toLowerCase().indexOf(s);

            if (pos != -1) {
                programPropsFileStr = programPropsFileStr.substring(0, pos);
            }

            programPropsFileStr += INI_NAME;
        }

        System.out.println("*** ini file path: " + programPropsFileStr);
        readMainIniFile();
        scale = Core.programProps.get("scale", 1.0);
        initializeResources();
        final String defaultPlayer = getDefaultPlayer();
        setPlayer(defaultPlayer);

    }

    /**
     * Reads main INI file.
     *
     * @throws LemmException if user aborts the initialization; that is, if they
     *                       do not specify source and destination paths for
     *                       resources from original Lemmings game disk.
     */
    private static void readMainIniFile() throws LemmException {
        // read main ini file
        programProps = new Props();

        if (!programProps.load(programPropsFileStr)) {
            // might exist or not - if not, it's created
            final LegalDialog ld = new LegalDialog(null, true);
            ld.setVisible(true);

            if (!ld.isOk()) {
                throw new LemmException("User abort");
            }
        }
    }

    /**
     * Initializes resources.
     *
     * @throws LemmException in unable to load resources from files.
     */
    private static void initializeResources() throws LemmException {
        resourcePath = programProps.get("resourcePath", "");
        final String sourcePath = programProps.get("sourcePath", "");
        final String rev = programProps.get("revision", "");
        GameController.setMusicOn(programProps.get("music", false));
        GameController.setSoundOn(programProps.get("sound", true));
        double gain;
        gain = programProps.get("musicGain", 1.0);
        GameController.setMusicGain(gain);
        gain = programProps.get("soundGain", 1.0);
        GameController.setSoundGain(gain);
        GameController
                .setAdvancedSelect(programProps.get("advancedSelect", true));
        GameController
                .setClassicalCursor(programProps.get("classicalCursor", false));

        if (resourcePath.length() == 0 || !REVISION.equalsIgnoreCase(rev)) {
            // extract resources
            try {
                Extract.extract(null, sourcePath, resourcePath, null);
                resourcePath = Extract.getResourcePath();
                programProps.set("resourcePath",
                        ToolBox.addSeparator(Extract.getResourcePath()));
                programProps.set("sourcePath",
                        ToolBox.addSeparator(Extract.getSourcePath()));
                programProps.set("revision", REVISION);
                programProps.save(programPropsFileStr);
            } catch (final ExtractException ex) {
                programProps.set("resourcePath",
                        ToolBox.addSeparator(Extract.getResourcePath()));
                programProps.set("sourcePath",
                        ToolBox.addSeparator(Extract.getSourcePath()));
                programProps.save(programPropsFileStr);
                throw new LemmException(
                        "Ressource extraction failed\n" + ex.getMessage());
            }
        }
    }

    /**
     * Sets the player for the game to use.
     *
     * @param defaultPlayer the default player.
     */
    private static void setPlayer(final String defaultPlayer) {
        if (players.size() == 0) {
            // no players yet, establish default player
            players.add("default");
            Core.playerProps.set("player_0", "default");
        }

        player = new Player(defaultPlayer);
    }

    /**
     * Reads player name from INI file and returns name of default player.
     *
     * @return the name of the default player.
     */
    private static String getDefaultPlayer() {
        // read player names
        playerPropsFileStr = Core.resourcePath + "players.ini";
        playerProps = new Props();
        playerProps.load(playerPropsFileStr);
        final String defaultPlayer = playerProps.get("defaultPlayer",
                "default");
        players = new ArrayList<String>();

        for (int idx = 0; true; idx++) {
            final String p = playerProps.get("player_" + Integer.toString(idx),
                    "");

            if (p.length() == 0) {
                break;
            }

            players.add(p);
        }

        return defaultPlayer;
    }

    /**
     * Get String to resource in resource path.
     *
     * @param fname file name (without path)
     * @return absolute path to resource
     */
    public static String findResource(final String fname) {
        return resourcePath + fname;
    }

    /**
     * Store program properties.
     */
    public static void saveProgramProps() {
        programProps.set("scale", scale);
        programProps.save(programPropsFileStr);
        playerProps.set("defaultPlayer", Core.player.getName());
        playerProps.save(playerPropsFileStr);
        player.store();
    }

    /**
     * Output error message box in case of a missing resource.
     *
     * @param rsrc name missing of resource.
     */
    public static void resourceError(final String rsrc) {
        final String out = "The resource " + rsrc + " is missing\n"
                + "Please restart to extract all resources.";
        JOptionPane.showMessageDialog(null, out, "Error",
                JOptionPane.ERROR_MESSAGE);
        // invalidate resources
        programProps.set("revision", "invalid");
        programProps.save(programPropsFileStr);
        System.exit(1);
    }

    /**
     * Load an image from the resource path.
     *
     * @param tracker media tracker
     * @param fName   file name
     * @return Image
     * @throws ResourceException
     */
    public static Image loadImage(final MediaTracker tracker,
            final String fName) throws ResourceException {
        final String fileLoc = findResource(fName);

        // fileLoc cannot be null, since it equals resourcePath + fName, and
        // resourcePath must contain text in order for the program to run.
        // if (fileLoc == null) {
        // return null;
        // }

        return loadImage(tracker, fileLoc, false);
    }

    /**
     * Load an image from either the resource path or from inside the JAR (or
     * the directory of the main class).
     *
     * @param tracker media tracker
     * @param fName   file name
     * @param jar     true: load from the jar/class path, false: load from
     *                resource path
     * @return Image
     * @throws ResourceException
     */
    private static Image loadImage(final MediaTracker tracker,
            final String fName, final boolean jar) throws ResourceException {
        Image image;

        if (jar) {
            image = Toolkit.getDefaultToolkit()
                    .createImage(ToolBox.findFile(fName));
        } else {
            image = Toolkit.getDefaultToolkit().createImage(fName);
        }

        if (image != null) {
            tracker.addImage(image, 0);

            try {
                tracker.waitForID(0);
                if (tracker.isErrorAny()) {
                    image = null;
                }
            } catch (final Exception ex) {
                image = null;
            }
        }

        if (image == null) {
            throw new ResourceException(fName);
        }

        return image;
    }

    /**
     * Load an image from the resource path.
     *
     * @param fname file name
     * @param frame the parent component (main frame of the application).
     * @return Image
     * @throws ResourceException
     */
    public static Image loadImage(final String fname, final Component frame)
            throws ResourceException {
        final MediaTracker tracker = new MediaTracker(frame);
        final Image img = loadImage(tracker, fname);

        if (img == null) {
            throw new ResourceException(fname);
        }

        return img;
    }

    /**
     * Load an image from inside the JAR or the directory of the main class.
     *
     * @param fname
     * @param frame the parent component (main frame of the application).
     * @return Image
     * @throws ResourceException
     */
    public static Image loadImageJar(final String fname, final Component frame)
            throws ResourceException {
        final MediaTracker tracker = new MediaTracker(frame);
        final Image img = loadImage(tracker, fname, true);

        if (img == null) {
            throw new ResourceException(fname);
        }

        return img;
    }

    /**
     * Get player name via index.
     *
     * @param idx player index
     * @return player name
     */
    public static String getPlayer(final int idx) {
        return players.get(idx);
    }

    /**
     * Get number of players.
     *
     * @return number of player.
     */
    public static int getPlayerNum() {
        if (players == null) {
            return 0;
        }

        return players.size();
    }

    /**
     * Reset list of players.
     */
    public static void clearPlayers() {
        players.clear();
        playerProps.clear();
    }

    /**
     * Add player.
     *
     * @param name player name
     */
    public static void addPlayer(final String name) {
        players.add(name);
        playerProps.set("player_" + (players.size() - 1), name);
    }

    /**
     * Get internal Draw Width.
     *
     * @return internal draw width
     */
    public static int getDrawWidth() {
        return INTERNAL_DRAW_WIDTH;
    }

    /**
     * Get internal Draw Height.
     *
     * @return internal draw width
     */
    public static int getDrawHeight() {
        return Level.HEIGHT + WIN_OFS + DRAW_HEIGHT_PADDING;
    }

    /**
     * Get Zoom scale.
     *
     * @return zoom scale
     */
    public static double getScale() {
        return scale;
    }

    /**
     * Set zoom scale.
     *
     * @param s zoom scale
     */
    public static void setScale(final double s) {
        scale = s;
    }
}
