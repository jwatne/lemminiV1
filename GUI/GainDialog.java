package gui;

import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import game.Music;
import game.SoundController;
import lemmini.Constants;

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
 * Dialog for volume/gain control.
 *
 * @author Volker Oth
 */
public class GainDialog extends JDialog {

    /**
     * Height of Cancel button.
     */
    private static final int CANCEL_BUTTON_HEIGHT = 23;

    /**
     * Width of Cancel button.
     */
    private static final int CANCEL_BUTTON_WIDTH = 77;

    /**
     * Y coordinate of Cancel button.
     */
    private static final int CANCEL_BUTTON_Y = 136;

    /**
     * X coordinate of cancel button.
     */
    private static final int CANCEL_BUTTON_X = 14;

    /**
     * Divisor for converting percentages to decimal values.
     */
    private static final double PERCENT_DIVISOR = 100.0;

    /**
     * Width of OK button.
     */
    private static final int OK_BUTTON_WIDTH = 66;

    /**
     * Y coordinate of OK button.
     */
    private static final int OK_BUTTON_Y = 135;

    /**
     * X coordinate of OK button.
     */
    private static final int OK_BUTTON_X = 210;

    /**
     * Y coordinate of sound slider.
     */
    private static final int SOUND_SLIDER_Y = 90;

    /**
     * Major tick spacing, in pixels.
     */
    private static final int MAJOR_TICK_SPACING = 10;

    /**
     * Height of controls.
     */
    private static final int CONTROL_HEIGHT = 25;

    /**
     * Width of music slider.
     */
    private static final int MUSIC_SLIDER_WIDTH = 256;

    /**
     * Y coordinate of music slider.
     */
    private static final int MUSIC_SLIDER_Y = 30;

    /**
     * Width of music volume label.
     */
    private static final int MUSIC_VOLUME_WIDTH = 106;

    /**
     * Y coordinate of music volume label.
     */
    private static final int MUSIC_VOLUME_Y = 15;

    /**
     * Shared height of volume labels.
     */
    private static final int VOLUME_HEIGHT = 14;

    /**
     * Width of sound volume label.
     */
    private static final int SOUND_VOLUME_WIDTH = 101;

    /**
     * Y coordinate of sound volume label.
     */
    private static final int SOUND_VOLUME_Y = 75;

    /**
     * Shared x value for volume labels.
     */
    private static final int VOLUME_LABELS_X = 15;

    /**
     * Initial height of dialog.
     */
    private static final int INITIAL_HEIGHT = 200;

    /**
     * Initial width of dialog.
     */
    private static final int INITIAL_WIDTH = 300;

    private static final long serialVersionUID = 1L;

    /**
     * Content pane.
     */
    private JPanel jContentPane = null;

    /**
     * Music slider.
     */
    private JSlider jSliderMusic = null;

    /**
     * Music gain label.
     */
    private JLabel jLabelMusicGain = null;

    /**
     * Sound gain label.
     */
    private JLabel jLabelSoundGain = null;

    /**
     * Sound slider.
     */
    private JSlider jSliderSound = null;

    /**
     * Ok button.
     */
    private JButton jButtonOK = null;

    /**
     * Cancel button.
     */
    private JButton jButtonCancel = null;

    /**
     * Constructor for modal dialog in parent frame.
     *
     * @param frame parent frame
     * @param modal create modal dialog?
     */
    public GainDialog(final JFrame frame, final boolean modal) {
        super(frame, modal);
        initialize();
        final Point p = frame.getLocation();
        this.setLocation(p.x + frame.getWidth() / 2 - getWidth() / 2,
                p.y + frame.getHeight() / 2 - getHeight() / 2);
        jSliderSound.setValue((int) (Constants.ONE_HUNDRED_PERCENT
                * SoundController.getSound().getGain()));
        jSliderMusic.setValue(
                (int) (Constants.ONE_HUNDRED_PERCENT * Music.getGain()));
    }

    /**
     * Automatically generated init.
     */
    private void initialize() {
        this.setSize(INITIAL_WIDTH, INITIAL_HEIGHT);
        this.setTitle("Volume Controls");
        this.setContentPane(getJContentPane());
    }

    /**
     * This method initializes jContentPane.
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jLabelSoundGain = new JLabel();
            jLabelSoundGain.setBounds(new Rectangle(VOLUME_LABELS_X,
                    SOUND_VOLUME_Y, SOUND_VOLUME_WIDTH, VOLUME_HEIGHT));
            jLabelSoundGain.setText("Sound Volume");
            jLabelMusicGain = new JLabel();
            jLabelMusicGain.setText("Music Volume");
            jLabelMusicGain.setBounds(new Rectangle(VOLUME_LABELS_X,
                    MUSIC_VOLUME_Y, MUSIC_VOLUME_WIDTH, VOLUME_HEIGHT));
            jContentPane = new JPanel();
            jContentPane.setLayout(null);
            jContentPane.add(getJSliderMusic(), null);
            jContentPane.add(jLabelMusicGain, null);
            jContentPane.add(jLabelSoundGain, null);
            jContentPane.add(getJSliderSound(), null);
            jContentPane.add(getJButtonOK(), null);
            jContentPane.add(getJButtonCancel(), null);
        }
        return jContentPane;
    }

    /**
     * This method initializes jSliderMusic.
     *
     * @return javax.swing.JSlider
     */
    private JSlider getJSliderMusic() {
        if (jSliderMusic == null) {
            jSliderMusic = new JSlider();
            jSliderMusic.setBounds(new Rectangle(VOLUME_LABELS_X,
                    MUSIC_SLIDER_Y, MUSIC_SLIDER_WIDTH, CONTROL_HEIGHT));
            jSliderMusic.setMaximum(Constants.ONE_HUNDRED_PERCENT);
            jSliderMusic.setMinimum(0);
            jSliderMusic.setMajorTickSpacing(MAJOR_TICK_SPACING);
            jSliderMusic.setPaintTicks(true);
            jSliderMusic.setValue(Constants.ONE_HUNDRED_PERCENT);
        }
        return jSliderMusic;
    }

    /**
     * This method initializes jSliderSound.
     *
     * @return javax.swing.JSlider
     */
    private JSlider getJSliderSound() {
        if (jSliderSound == null) {
            jSliderSound = new JSlider();
            jSliderSound.setBounds(new Rectangle(VOLUME_LABELS_X,
                    SOUND_SLIDER_Y, MUSIC_SLIDER_WIDTH, CONTROL_HEIGHT));
            jSliderSound.setMaximum(Constants.ONE_HUNDRED_PERCENT);
            jSliderSound.setMinimum(0);
            jSliderSound.setPaintTicks(true);
            jSliderSound.setValue(Constants.ONE_HUNDRED_PERCENT);
            jSliderSound.setMajorTickSpacing(MAJOR_TICK_SPACING);
        }

        return jSliderSound;
    }

    /**
     * This method initializes jButtonOK.
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonOK() {
        if (jButtonOK == null) {
            jButtonOK = new JButton();
            jButtonOK.setBounds(new Rectangle(OK_BUTTON_X, OK_BUTTON_Y,
                    OK_BUTTON_WIDTH, CONTROL_HEIGHT));
            jButtonOK.setText(" Ok ");
            jButtonOK.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(
                        final java.awt.event.ActionEvent e) {
                    Music.setGain(jSliderMusic.getValue() / PERCENT_DIVISOR);
                    SoundController.getSound()
                            .setGain(jSliderSound.getValue() / PERCENT_DIVISOR);
                    dispose();
                }
            });
        }

        return jButtonOK;
    }

    /**
     * This method initializes jButtonCancel.
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonCancel() {
        if (jButtonCancel == null) {
            jButtonCancel = new JButton();
            jButtonCancel
                    .setBounds(new Rectangle(CANCEL_BUTTON_X, CANCEL_BUTTON_Y,
                            CANCEL_BUTTON_WIDTH, CANCEL_BUTTON_HEIGHT));
            jButtonCancel.setText("Cancel");
            jButtonCancel
                    .addActionListener(new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(
                                final java.awt.event.ActionEvent e) {
                            dispose();
                        }
                    });
        }
        return jButtonCancel;
    }
}
