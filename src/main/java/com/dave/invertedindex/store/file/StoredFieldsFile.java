package com.dave.invertedindex.store.file;

import com.dave.invertedindex.index.CorruptIndexException;
import com.dave.invertedindex.store.codec.Codec;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Write/Read stored fields to/from disk
 */
public class StoredFieldsFile extends TxtFile {

    public StoredFieldsFile(String path, Codec codec) {
        this.path = path;
        this.codec = codec;
    }

    @Override
    protected  HashMap<?, ?> parseData() throws IOException, CorruptIndexException {
        HashMap<Long, String> storedFields = new HashMap<>();
        String rawData = null;
        //traverse the file and parse one by one the postings of every term using codec
        while ((rawData = this.reader.readLine()) != null) {
            Map.Entry<Long, String> entry = this.codec.readEntry(rawData);
            storedFields.put(entry.getKey(), entry.getValue());
        }
        return storedFields;
    }

}