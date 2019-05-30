package com.dave.invertedindex.parse;

import java.util.regex.Pattern;

/**
 * AlphaNumericFilter filters out characters that are not letters (occidental languages) or numbers
 */
public class SymbolFilter extends DataStream {

    protected static final Pattern FILTER = Pattern.compile("[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]");

    public SymbolFilter(DataStream input) {
        this.input = input;
    }


    @Override
    public boolean hasMoreTokens() {
        if (!input.hasMoreTokens()) {
            out = null;
            return false;
        }
        out = FILTER.matcher(input.out()).replaceAll("");

        return true;
    }
}