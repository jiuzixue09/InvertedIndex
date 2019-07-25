package com.dave.invertedindex.store;

import com.dave.invertedindex.index.CorruptIndexException;
import com.dave.invertedindex.index.Index;
import com.dave.invertedindex.index.PostingsDictionary;

import java.io.IOException;

/**
 * A directory provides access to the set of files where the index data is stored
 */
public interface Directory {

    /**
     * save the data stored in the Index to disk
     * @param index
     */
    public void write(Index index) throws IOException, CorruptIndexException;

    /**
     * Reconstruct the index from the data stored in disk
     * @return
     */
    public Index read(Index index) throws IOException, CorruptIndexException;

    public PostingsDictionary readPostingsBlock(PostingsDictionary dictionary, String fieldName, String term) throws IOException, CorruptIndexException;

    /**
     * reset the index, ie, delete all files stored in disk
     */
    public void reset() throws IOException, CorruptIndexException;

    /**
     * close open files and resources
     */
    public void close(Index index);


}