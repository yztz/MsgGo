/*
 * Copyright (C) 2026 yztz
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package top.yztz.msggo.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import top.yztz.msggo.R;
import top.yztz.msggo.data.Settings;
import top.yztz.msggo.exception.DataLoadFailed;

public class SpreadsheetReader {

    // To add a new format: implement SpreadsheetParser and add its constructor here.
    private static final List<Supplier<SpreadsheetParser>> REGISTRY = Arrays.asList(
            PoiSpreadsheetParser::new,
            OdsSpreadsheetParser::new
    );

    private SpreadsheetParser active;

    public void read(String path) throws DataLoadFailed {
        File file = new File(path);
        if (file.exists() && file.length() > Settings.EXCEL_FILE_SIZE_MAX)
            throw new DataLoadFailed(R.string.file_too_large);

        String ext = path.substring(path.lastIndexOf(".")).toLowerCase(Locale.ROOT);
        active = null;
        for (Supplier<SpreadsheetParser> factory : REGISTRY) {
            SpreadsheetParser parser = factory.get();
            if (parser.supports(ext)) {
                active = parser;
                break;
            }
        }
        if (active == null) throw new DataLoadFailed(R.string.error_unsupported_format);
        active.parse(path);
    }

    public String[] getTitles() {
        return active.getTitles();
    }

    public ArrayList<HashMap<String, String>> readContent() {
        return active.getContent();
    }
}
