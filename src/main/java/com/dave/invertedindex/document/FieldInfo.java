package com.dave.invertedindex.document;

import com.dave.invertedindex.parse.CommonTokenizer;
import com.dave.invertedindex.parse.TextParser;
import com.dave.invertedindex.parse.Tokenizer;

/**
 * Represents configuration for a concrete type of field
 */
public class FieldInfo {

    /**
     * string representation for the  field types INDEXED and STORED
     */
    public static final String INDEXED = "indexed";
    public static final String STORED = "stored";


    /**
     * default value for this.indexed
     */
    protected static final boolean INDEXED_DEFAULT = true;

    /**
     * default value for this.stored
     */
    protected static final boolean STORED_DEFAULT = false;

    /**
     * Text has to be tokenized, ie, splitted in tokens using a tokenizer
     */
    protected static final boolean TOKENIZED_DEFAULT = true;


    /**
     * is the field being indexed?
     */
    protected boolean indexed;

    /**
     * is the field being stored?
     */
    protected boolean stored;

    /**
     * does this field need to be tokenized?
     */
    protected boolean tokenized;

    /**
     * tokenizer used to analyze/tokenize/normalize the data
     */
    private Class tokenizer = null;


    public FieldInfo() {
        this.indexed = FieldInfo.INDEXED_DEFAULT;
        this.stored = FieldInfo.STORED_DEFAULT;
        this.tokenized = FieldInfo.TOKENIZED_DEFAULT;
    }

    /**
     * Obtain a default FieldInfo instance
     * @return new FieldInfo(true,false,TextParser.class)
     */
    public static FieldInfo getDefaultInstance(){
        return new FieldInfo(true,false, CommonTokenizer.class);
    }

    /**
     * For the field to be tokenized, a tokenizer has to be passed
     * @param indexed is the field being indexex?
     * @param stored is the field being stored?
     * @param tokenizer in case the field is indexed, a tokenizer needs to be defined
     */
    public FieldInfo(boolean indexed, boolean stored, Class tokenizer)  {
        this.indexed = indexed;
        this.stored = stored;
        if (tokenizer.getSuperclass().isAssignableFrom(Tokenizer.class)) {
            this.tokenizer = tokenizer;
            this.tokenized = true;
        } else {
            //TODO what should we do if tokenizer is not as expected an object of Tokenizer or subclass?
            //at least user should be notified that he's doing something wrong...
            this.tokenized = false;
        }
    }

    public FieldInfo(boolean indexed, boolean stored) {
        this.indexed = indexed;
        this.stored = stored;
        this.tokenized = false;
    }


    public boolean isIndexed() {
        return this.indexed;
    }

    public boolean isStored() {
        return this.stored;
    }

    public boolean isTokenized() {
        return this.tokenized;
    }

    public Class getTokenizer() {
        return tokenizer;
    }

}