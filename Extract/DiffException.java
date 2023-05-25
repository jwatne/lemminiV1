package extract;

/**
 * Generic Exception for Diff.
 *
 * @author Volker Oth
 */
public class DiffException extends Exception {
    private static final long serialVersionUID = 0x000000001;

    /**
     * Constructor.
     */
    public DiffException() {
        super();
    }

    /**
     * Constructor.
     *
     * @param s Exception string
     */
    public DiffException(final String s) {
        super(s);
    }
}
