package extract;

import java.io.File;
import java.io.FilenameFilter;
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
