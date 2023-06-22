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

/** game state. */
public enum GameState {
    /** init state. */
    INIT,
    /** display intro screen. */
    INTRO,
    /** display level briefing screen. */
    BRIEFING,
    /** display level. */
    LEVEL,
    /** display debriefing screen. */
    DEBRIEFING,
    /** fade out after level was finished. */
    LEVEL_END
}
