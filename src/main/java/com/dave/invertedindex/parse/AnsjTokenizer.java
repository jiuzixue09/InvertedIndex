package com.dave.invertedindex.parse;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.IndexAnalysis;

import java.util.Iterator;


public class AnsjTokenizer extends Tokenizer {
    Iterator<Term> iterator;
    /**
     * text that is going to be processed
     */
    protected String input = null;

    /**
     * Tokenizer object can be reused, just set new data, and then call start()
     * @param data data to be processed
     */
    public void setData(String data) {
        this.input = data;
    }

    public void start() {
        iterator = IndexAnalysis.parse(input).iterator();

    }

    /**
     * extract next token and set it in the output, so next element in the chain can process it
     *
     * @return
     */
    public boolean nextToken() {
        if (!iterator.hasNext()) {
            out = EMPTY_STRING;
            return false;
        }
        out = iterator.next().getName();

        return true;
    }

    /**
     * get next element of the string pass it to next element in the chain
     *
     * @return true if there's some token to be processed, false in other case
     */
    public boolean hasMoreTokens() {
        if (null == iterator) {
            out = EMPTY_STRING;
            return false;
        }
        return nextToken();
    }


}