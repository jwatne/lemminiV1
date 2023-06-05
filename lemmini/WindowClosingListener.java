package lemmini;

/**
 * A customized {@link java.awt.event.WindowAdapter} that handles closing a
 * {@link Lemmini} window.
 */
public final class WindowClosingListener extends java.awt.event.WindowAdapter {
    /**
     *
     */
    private final Lemmini lemmini;

    /**
     * @param app the Lemmini application.
     */
    WindowClosingListener(final Lemmini app) {
        this.lemmini = app;
    }

    @Override
    public void windowClosing(final java.awt.event.WindowEvent e) {
        this.lemmini.exit();
    }

    @Override
    public void windowClosed(final java.awt.event.WindowEvent e) {
        this.lemmini.exit();
    }
}
