package lemmini;
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
