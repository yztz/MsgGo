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

package top.yztz.msggo.exception;

import top.yztz.msggo.R;

public class DataLoadFailed extends Exception {
//    public final String msg;
    public final int res_id;
    public Exception e = null;
//    public DataLoadFailed(String msg) {
//        this.msg = msg;
//    }

    public DataLoadFailed(int res_id) {
        this.res_id = res_id;
    }
    public DataLoadFailed(Exception e) {
        this.res_id = R.string.unknown_error;
        this.e = e;
    }
}
