package com.dave.invertedindex.store.file;

import com.dave.invertedindex.index.CorruptIndexException;
import com.dave.invertedindex.index.Posting;
import com.dave.invertedindex.store.codec.Codec;

import java.io.IOException;
import java.util.*;

/**
 * Handles Read/Write of postings file
 */
public class PostingsFile extends TxtFile {


    public PostingsFile(String path, Codec codec) {
        this.path = path;
        this.codec = codec;
    }


    protected HashMap<?,?> parseData() throws IOException, CorruptIndexException {
        HashMap<String, List<Posting>> postings = new HashMap<>();
        String rawData = null;
        //traverse the file and parse one by one the postings of every term using codec
        while ((rawData = this.reader.readLine()) != null) {
            Map.Entry<String, List<Posting>> entry = this.codec.readEntry(rawData);
            if (entry != null) {
                postings.put(entry.getKey(), entry.getValue());
            }
        }
        return postings;
    }

}