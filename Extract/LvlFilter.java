package extract;

import java.io.File;
import java.io.FilenameFilter;

/**
 * File name filter for level files.
 *
 * @author Volker Oth
 */
public final class LvlFilter implements FilenameFilter {
    @Override
    public boolean accept(final File dir, final String name) {
        return (name.toLowerCase().indexOf(".lvl") != -1);
    }
}
