package com.dave.invertedindex.parse;

import java.util.StringTokenizer;

/**
 * A Tokenizer gets the input from a Reader and splits data in tokens. The StreamChain always has to begin with
 * a tokenizer that splits the text and streams the token to the next elements in the chain
 */
public class CommonTokenizer extends Tokenizer{

    /**
     * text that is going to be processed
     */
    protected String input = null;

    protected StringTokenizer tokenizer;

    /**
     * Tokenizer object can be reused, just set new data, and then call start()
     * @param data data to be processed
     */
    public void setData(String data) {
        this.input = data;
    }

    /**
     * split the text and fill the buffer with the tokens
     */
    public void start() {
        tokenizer = new StringTokenizer(input);
    }


    /**
     * extract next token and set it in the output, so next element in the chain can process it
     * @return
     */
    public boolean nextToken() {
        if (!tokenizer.hasMoreTokens()) {
            out = EMPTY_STRING;
            return false;
        }
        out = tokenizer.nextToken();

        return true;
    }

    /**
     * get next element of the string pass it to next element in the chain
     * @return true if there's some token to be processed, false in other case
     */
    public boolean hasMoreTokens() {
        if (null == tokenizer || tokenizer.countTokens() == 0) {
            out = EMPTY_STRING;
            return  false;
        }
        return nextToken();
    }
}