package com.dave.invertedindex.parse;

/**
 * DataStream
 */
public abstract class DataStream {

    protected DataStream input;

    protected String out;

    public DataStream() {}

    public DataStream(DataStream input) {
        this.input = input;
        this.out = null;
    }

    public DataStream(DataStream input, String out) {
        this.input = input;
        this.out = out;
    }

    public String out() {
        return out;
    }

    public abstract boolean hasMoreTokens();

    public void start() {
        input.start();
    }

}