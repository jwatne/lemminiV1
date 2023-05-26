package game;

/** game state. */
public enum State {
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
