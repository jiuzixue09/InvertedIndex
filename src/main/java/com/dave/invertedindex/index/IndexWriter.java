package com.dave.invertedindex.index;


import com.dave.invertedindex.document.Document;
import com.dave.invertedindex.document.Field;
import com.dave.invertedindex.store.DbFileDirectory;
import com.dave.invertedindex.store.Directory;
import com.dave.invertedindex.store.TxtFileDirectory;
import com.dave.invertedindex.util.Benchmark;

import java.io.IOException;

/**
 * This class is used to create and maintain the Index. It's not doing real job, but delegating:
 * - FieldIndexer adds the terms in a field to the index
 * - Directory saves index files to disk
 *
 */
public class IndexWriter {

    private Directory directory;

    private Index index;

    private FieldIndexer indexer;
    public IndexWriter(Directory directory) {
        this.directory = directory;
        this.index = Index.getInstance();
        this.indexer = new FieldIndexer(index);

    }

    /**
     * Add a document to the in-memory index.
     * Iterate over all fields and add content of each field, one by one
     */
    public void addDocument(Document doc) {

        for(String fieldName: doc.fields().keySet()) {
            Field field = doc.fields().get(fieldName);
            //for some fields (like title) we might want store them but not index them
            //skip to index its contents, since it's not in the requirements, even though it would be possible
            if (field.isStored()) {
                index.getStoredDocuments(fieldName).put(doc.getDocumentId(), field.data());
            }
            if (field.isIndexed()) {
                indexer.addField(doc.getDocumentId(), field);
            }
        }
    }

    public void removeDocument(Document doc) throws IOException, ClassNotFoundException {
        for(String fieldName: doc.fields().keySet()) {
            Field field = doc.fields().get(fieldName);
            if(directory instanceof TxtFileDirectory){
                indexer.removeField(doc.getDocumentId(), field);
            }else{
                ((DbFileDirectory)directory).removeField(doc.getDocumentId(),field);
            }

        }
    }


    public long getNumDocs() {
        return index.numDocs;
    }

    public long getNumTerms() {
        return index.numTerms;
    }

    /**
     * flush all indexed documents to disk
     */
    public void flush() throws IOException, CorruptIndexException {
        Benchmark.getInstance().start("IndexWriter.flush");
        directory.write(index);
        Benchmark.getInstance().end("IndexWriter.flush");
    }

    /**
     * reset the index to its initial state, with no data in memory, neither on files on disk
     * @throws IOException
     * @throws CorruptIndexException
     */
    public void reset() throws IOException, CorruptIndexException {
        directory.reset();
        index.reset();
    }

    public void clear(){
        index.clear();
    }

    /**
     * close all open resources and files which have been used by the index
     */
    public void close() {
        directory.close(index);
    }

    /**
     * open the index for reading, ie, load data from directory
     * this method only work for DbFileDirectory, not ExtFileDirectory
     */
    public void open() throws IOException, CorruptIndexException {
        if(directory instanceof DbFileDirectory) directory.read(index);
    }
}
