package com.dave.invertedindex.document;

import com.dave.invertedindex.parse.DataStream;
import com.dave.invertedindex.parse.Parser;
import com.dave.invertedindex.parse.TextParser;
import com.dave.invertedindex.parse.Tokenizer;
import com.dave.invertedindex.util.Logger;

/**
 * A Field belongs to a Document, and is composed by a sequence of Terms. Even though, when the document is created
 * the Terms are indeed still in the form of String
 */
public class Field {

    /**
     * name of the field
     */
    protected String name;

    /**
     * data contained by the field, contains the terms to be indexed, and/or stored
     */
    protected String data;


    protected DataStream stream;


    /**
     * configuration options for this field
     */
    private FieldInfo options;


    private static TextParser parser = null;

    public Field(final String name, String data, final FieldInfo options) {
        this.name = name;
        this.data = data;
        this.options = options;
    }

    public Field(String name, String data) {
        this(name, data, new FieldInfo());
    }

    public String name() {
        return name;
    }

    public String data() {
        return data;
    }

    public boolean isIndexed() {
        return options.isIndexed();
    }

    public boolean isStored() {
        return options.isStored();
    }

    public boolean isTokenized() {
        return options.isTokenized();
    }

    public void setData(String data) {
        this.data = data;
    }

    /**
     * Obtain by reflection an instance of the Parser
     * With this approach, parsers can be defined per field, dynamically. The client only needs
     * to pass the  class within FieldInfo when the Field is declared.
     * @return A DataParser
     */
    public Parser getParser() {
        Class c = options.getTokenizer();
        if(null == c) return null;
        try {
            Tokenizer tokenizer = (Tokenizer)c.getDeclaredConstructor().newInstance();
            return new TextParser(tokenizer);
        } catch (Exception e) {
            Logger.getInstance().error("couldn't create tokenizer object", e);
        }
        return null;
    }


    public DataStream getDataStream(Parser parser) {
        if (!options.isIndexed()) {
            return null;
        }
        if (stream != null) {
            return stream;
        }
        return parser.dataStream(name, data);
    }

}