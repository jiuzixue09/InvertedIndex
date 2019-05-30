package com.dave.invertedindex.parse;

/**
 * A Tokenizer gets the input from a Reader and splits data in tokens. The StreamChain always has to begin with
 * a tokenizer that splits the text and streams the token to the next elements in the chain
 */
public abstract class Tokenizer extends DataStream {
    /**
     * keep an empty string to return it whenever it's needed, rather than creating a new one every time
     */
    public static final String EMPTY_STRING = "";

    /**
     * Tokenizer object can be reused, just set new data, and then call start()
     * @param data data to be processed
     */
    public abstract void setData(String data);

    /**
     * split the text and fill the buffer with the tokens
     */
    public abstract void start() ;

    /**
     * extract next token and set it in the output, so next element in the chain can process it
     * @return
     */
    public abstract boolean nextToken();

    /**
     * get next element of the string pass it to next element in the chain
     * @return true if there's some token to be processed, false in other case
     */
    public abstract boolean hasMoreTokens();

}