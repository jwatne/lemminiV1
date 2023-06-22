package game;
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

/** Transition states. */
public enum TransitionState {
    /** no fading. */
    NONE,
    /** restart level: fade out, fade in briefing. */
    RESTART_LEVEL,
    /** replay level: fade out, fade in briefing. */
    REPLAY_LEVEL,
    /** load level: fade out, fade in briefing. */
    LOAD_LEVEL,
    /** load replay: fade out, fade in briefing. */
    LOAD_REPLAY,
    /** level finished: fade out. */
    END_LEVEL,
    /** go to intro: fade in intro. */
    TO_INTRO,
    /** go to briefing: fade in briefing. */
    TO_BRIEFING,
    /** go to debriefing: fade in debriefing. */
    TO_DEBRIEFING,
    /** go to level: fade in level. */
    TO_LEVEL
}
