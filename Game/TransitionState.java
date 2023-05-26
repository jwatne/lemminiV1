package game;

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
