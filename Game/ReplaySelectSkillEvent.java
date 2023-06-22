package game;

/**
 * Storage class for SELECT_SKILL event.
 *
 * @author Volker Oth
 */
public class ReplaySelectSkillEvent extends ReplayEvent {
    /**
     * A lemming's assigned skill.
     */
    private Type skill;

    /**
     * Skill selected.
     *
     * @param ctr Frame counter
     * @param s   skill selected
     */
    ReplaySelectSkillEvent(final int ctr, final Type s) {
        super(ctr, ReplayStream.SELECT_SKILL);
        skill = s;
    }

    @Override
    public final String toString() {
        return super.toString() + ", " + skill.ordinal();
    }

    /**
     * Returns a lemming's assigned skill.
     *
     * @return a lemming's assigned skill.
     */
    public final Type getSkill() {
        return skill;
    }

    /**
     * Sets a lemming's assigned skill.
     *
     * @param skillAssigned a lemming's assigned skill.
     */
    public final void setSkill(final Type skillAssigned) {
        this.skill = skillAssigned;
    }
}
