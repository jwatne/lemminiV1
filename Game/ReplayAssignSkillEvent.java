package game;

/**
 * Storage class for ASSIGN_SKILL event.
 *
 * @author Volker Oth
 */
public class ReplayAssignSkillEvent extends ReplayEvent {
    /** skill. */
    private Type skill;
    /** Lemming. */
    private int lemming;

    /**
     * Skill assigned.
     *
     * @param ctr Frame counter
     * @param s   skill selected
     * @param lem lemming no. that the skill was assigned
     */
    ReplayAssignSkillEvent(final int ctr, final Type s, final int lem) {
        super(ctr, ReplayStream.ASSIGN_SKILL);
        skill = s;
        lemming = lem;
    }

    @Override
    public final String toString() {
        return super.toString() + ", " + skill.ordinal() + ", " + lemming;
    }

    /**
     * Returns skill.
     *
     * @return skill.
     */
    public final Type getSkill() {
        return skill;
    }

    /**
     * Sets skill.
     *
     * @param lemmingSkill skill.
     */
    public final void setSkill(final Type lemmingSkill) {
        this.skill = lemmingSkill;
    }

    /**
     * Returns Lemming.
     *
     * @return Lemming.
     */
    public final int getLemming() {
        return lemming;
    }

    /**
     * Sets Lemming.
     *
     * @param aLemming Lemming.
     */
    public final void setLemming(final int aLemming) {
        this.lemming = aLemming;
    }
}
