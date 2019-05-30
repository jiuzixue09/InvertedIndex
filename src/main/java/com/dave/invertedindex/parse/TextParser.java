package com.dave.invertedindex.parse;

import com.dave.invertedindex.util.Logger;

/**
 *  TextParser extends Parser and defines in createStreamChain() method a set of rules and filters to split text
 *   in normalized tokens
 */
public class TextParser extends Parser {

    Tokenizer tokenizer;
    public TextParser(Tokenizer tokenizer ) {
        this.tokenizer = tokenizer;
    }

    /**
     * Here, the filters (and the order they are applied) used to parse the text are defined
     * @param fieldName this filter chain will be used for this field, to index all documents
     * @return An StreamChain object which keeps the reference to the first and the last elements in the chain
     */
    public StreamChain createStreamChain(final String fieldName) {
        DataStream out = new WithoutFilter(tokenizer);
        if(getConf().getEnableLowercaseFilter()){
            out = new LowerCaseFilter(out);
        }
        if(getConf().getEnableAlphanumericFilter()){
            out = new AlphaNumericFilter(out);
        }
        if(getConf().getEnableStopwordFilter()){
            out = new StopWordFilter(out);
        }
        if(getConf().getEnableLengthFilter()){
            out = new LengthFilter(out, getConf().getMinLengthFilter(), getConf().getMaxLengthFilter());
        }
        if (getConf().getEnableSymbolFilter()) {
            out = new SymbolFilter(out);
        }

        return new StreamChain(tokenizer, out);
    }



}