/* Copyright (C) 2017, 2019  Martin Šícho <martin.sicho@vscht.cz>
   Copyright (C) 2013  Johannes Kirchmair <johannes.kirchmair@univie.ac.at>
 
    This file is part of GLORYx.

    GLORYx is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    All we ask is that proper credit is given for our work, which includes 
    - but is not limited to - adding the above copyright notice to the beginning 
    of your source code files, and to any copyright notice that you may distribute 
    with programs based on this work.

    GLORYx is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GLORYx.  If not, see <https://www.gnu.org/licenses/>.
*/

package org.zbh.fame.fame3.utils.data.parsers;

public class FAMEFileParserException extends Throwable {

    private String input;
    private String inputIdentifier;

    public FAMEFileParserException(String message) {
        super(message);

        this.input = null;
        this.inputIdentifier = null;
    }

    public FAMEFileParserException(String message, Throwable throwable) {
        super(message, throwable);

        this.input = null;
        this.inputIdentifier = null;
    }

    public FAMEFileParserException(String message, Throwable throwable, String input, String inputIdentifier) {
        super(message, throwable);

        this.input = input;
        this.inputIdentifier = inputIdentifier;
    }

    public FAMEFileParserException(Throwable throwable, String input, String inputIdentifier) {
        super(throwable);

        this.input = input;
        this.inputIdentifier = inputIdentifier;
    }

    public FAMEFileParserException(String message, Throwable throwable, boolean enableSuppression, boolean writableStackTrace, String input, String inputIdentifier) {
        super(message, throwable, enableSuppression, writableStackTrace);

        this.input = input;
        this.inputIdentifier = inputIdentifier;
    }

    public String getInput() {
        return input;
    }

    public String getInputIdentifier() {
        return inputIdentifier;
    }
}
