package com.dave.invertedindex.parse;


public class WithoutFilter extends DataStream{
    public WithoutFilter(DataStream input) {
        super(input);
    }

    @Override
    public boolean hasMoreTokens() {
        if (!input.hasMoreTokens()) {
            out = null;
            return false;
        }
        out = input.out();
        return true;
    }
}