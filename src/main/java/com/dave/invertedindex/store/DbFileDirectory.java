package com.dave.invertedindex.store;

import com.dave.invertedindex.document.Field;
import com.dave.invertedindex.document.FieldInfo;
import com.dave.invertedindex.index.*;
import com.dave.invertedindex.parse.DataStream;
import com.dave.invertedindex.parse.Parser;
import com.dave.invertedindex.store.codec.*;
import com.dave.invertedindex.store.file.FieldConfigFile;
import com.dave.invertedindex.store.file.NormsFile;
import com.dave.invertedindex.store.file.PostingsFile;
import com.dave.util.SerializeUtil;
import org.mapdb.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

/**
 * Implements a Directory storing data in text files
 */
public class DbFileDirectory implements Directory {

    /**
     * names used to name the files stored in disk
     */
    protected static final String FIELDS_CONFIG_FILE = "fields";
    protected static final String NORMS_FILE = "norms.db";
    protected static final String POSTINGS_FILE = "postings.db";
    protected static final String STORED_CONTENT_FILE = "stored.db";

    private static DB NORMS_DB;
    private static DB POSTINGS_DB;
    private static DB STORED_DB;

    /**
     * path where the directory files are
     */
    protected String directoryPath;

    public DbFileDirectory(String path) {
        this.directoryPath = path;
        initDB();
    }

    private synchronized void initDB(){
        if(null == NORMS_DB || NORMS_DB.isClosed()){
            NORMS_DB = DBMaker.fileDB(directoryPath.concat(DbFileDirectory.NORMS_FILE))
                    .fileMmapEnable()
                    .fileLockDisable()
                    .checksumHeaderBypass()
                    .closeOnJvmShutdown()
                    .make();
        }

        if(null == POSTINGS_DB || POSTINGS_DB.isClosed()) {
            POSTINGS_DB = DBMaker
                    .fileDB(directoryPath.concat(DbFileDirectory.POSTINGS_FILE))
                    .fileMmapEnable()
                    .fileLockDisable()
                    .checksumHeaderBypass()
                    .closeOnJvmShutdown()
                    .make();
        }
        if(null == STORED_DB || STORED_DB.isClosed()) {
            STORED_DB = DBMaker
                    .fileDB(directoryPath.concat(DbFileDirectory.STORED_CONTENT_FILE))
                    .fileMmapEnable()
                    .fileLockDisable()
                    .checksumHeaderBypass()
                    .closeOnJvmShutdown()
                    .make();
        }

    }

    Map<String, ConcurrentMap> dbMap = new HashMap<>();

    private ConcurrentMap<String, Integer> getDocumentNormsDB(String fieldName){
        String key = NORMS_FILE.concat(".").concat(fieldName);
        if(!dbMap.containsKey(key)){
            ConcurrentMap<String, Integer> map = NORMS_DB
                    .hashMap(fieldName, Serializer.STRING, Serializer.INTEGER)
                    .createOrOpen();

            dbMap.put(key,map);
        }

        return dbMap.get(key);
    }

    private ConcurrentMap<String, byte[]> getPostingsDB(String fieldName){
        String key = POSTINGS_FILE.concat(".").concat(fieldName);
        if(!dbMap.containsKey(key)) {
            ConcurrentMap<String, byte[]> map = POSTINGS_DB
                    .hashMap(fieldName, Serializer.STRING, Serializer.BYTE_ARRAY)
                    .createOrOpen();
            dbMap.put(key,map);
        }
        return dbMap.get(key);
    }

    private ConcurrentMap<String, String> getStoreDB(String fieldName){
        String key = NORMS_FILE.concat(".").concat(fieldName);
        if(!dbMap.containsKey(key)) {
            ConcurrentMap<String, String> map = STORED_DB
                    .hashMap(fieldName, Serializer.STRING, Serializer.STRING)
                    .createOrOpen();
            dbMap.put(key,map);
        }
        return dbMap.get(key);
    }



    public void removeField(final String documentId, final Field field) throws IOException, ClassNotFoundException {
        ConcurrentMap<String, byte[]> dictionary = getPostingsDB(field.name());
        if (field.isStored()) {
            getStoreDB(field.name()).remove(documentId);
        }else if(field.isIndexed()){
            //identifiers or keywords can be indexed without being tokenized
            if (!field.isTokenized()) {
                removePosting(dictionary, field.data(), documentId);
            } else {
                //get the stream that  provides the terms
                Parser parser = field.getParser();
                DataStream stream = parser.dataStream(field.name(), field.data());
                stream.start();
                //get the tokens and add to the index
                while (stream.hasMoreTokens()) {
                    String token = stream.out();
                    if (token.length() > 0) {
                        removePosting(dictionary, token, documentId);
                    }
                }

            }

        }

        getDocumentNormsDB(field.name()).remove(documentId);
    }

    private boolean removePosting(ConcurrentMap<String, byte[]> dictionary, String term, String documentId) throws IOException, ClassNotFoundException {
        byte[] bytes = dictionary.get(term);
        if(null == bytes) return false;
        List<Posting> postings = SerializeUtil.toListObject(bytes);
        boolean b = postings.removeIf(posting -> posting.getDocumentId().equals(documentId));
        if(b){
            if(postings.isEmpty()){
                dictionary.remove(term);
            }else{
                dictionary.put(term,SerializeUtil.toByteArrayList(postings));
            }
        }
        return b;
    }


    /**
     * save the data stored in the Index to disk
     * @param index the index containing the data that it's going to be written to disk
     */
    public void write(Index index) throws IOException, CorruptIndexException {
        HashSet<String> indexedFields = index.getFieldNamesByOption(FieldInfo.INDEXED);

        //for every field store a file
        for(String fieldName: indexedFields) {
            Map<String, Integer> documentNorms = index.getDocumentNorms(fieldName);

            getDocumentNormsDB(fieldName).putAll(documentNorms);

            writePostings(index, fieldName);
            indexedFields.add(fieldName);
        }

        Set<String> storedFields = index.getFieldNamesByOption(FieldInfo.STORED);
        //for every stored field
        for(String fieldName: storedFields) {
            getStoreDB(fieldName).putAll(index.getStoredDocuments(fieldName));
        }
        //to reload index from disk, it's necessary to keep a file with names of the fields that are indexed and stored
        FieldConfigFile fiFile = new FieldConfigFile(this.directoryPath.concat(DbFileDirectory.FIELDS_CONFIG_FILE), new FieldConfigCodec());
        fiFile.delete();
        fiFile.write(index.getFieldNamesByOption());

    }


    /**
     * write the files containing the postings data. since there's too much info to save it in one single file, every
     * block is saved to a file, and identified by the first 2 letters of the terms the block is keeping
     * @param index the index
     */
    protected void writePostings(Index index, String fieldName) throws IOException {
        PostingsDictionary dictionary = index.getPostingsDictionary(fieldName);
        ConcurrentMap<String, byte[]> postingsDB = getPostingsDB(fieldName);

        for (HashMap<String, List<Posting>> b : dictionary.getPostingsBlocksDictionary().values()) {

            for (Map.Entry<String, List<Posting>> entry : b.entrySet()) {
                postingsDB.put(entry.getKey(), SerializeUtil.toByteArrayList(entry.getValue()));
            }

        }

    }

    /**
     * Reconstruct the index from the data stored in disk
     * @return the index with the necessary data to start a search
     */
    public Index read(Index index) throws IOException, CorruptIndexException {
        //init HashMaps that will keep the index
        Map<String, Map<String, Integer>> norms = new HashMap<>();

        HashMap<String, PostingsDictionary> dictionary = new HashMap<>();

        Map<String, Map<String, String>> stored = new HashMap<>();

        //now load fields config info
        FieldConfigFile fiFile = new FieldConfigFile(this.directoryPath.concat(DbFileDirectory.FIELDS_CONFIG_FILE), new FieldConfigCodec());
        HashMap<String, HashSet<String>> fields = (HashMap<String, HashSet<String>>)fiFile.read();

        if(fields == null || fields.isEmpty()) {
            //index can't be reloaded without this data
            return null;
        }

        Set<String> indexedFields = fields.get(FieldInfo.INDEXED);
        for(String fieldName: indexedFields) {
            norms.put(fieldName, getDocumentNormsDB(fieldName));

            //leave dictionary empty, data will be loaded dynamically when needed
            PostingsDictionary fieldDictionary = new PostingsDictionary();
            dictionary.put(fieldName, fieldDictionary);
        }

        Set<String> storedFields = fields.get(FieldInfo.STORED);
        if(null != storedFields){
            //for every  stored field, load stored field file
            for(String fieldName: storedFields) {
                stored.put(fieldName, getStoreDB(fieldName));
            }
        }

        //at this point, we have already all what we need to start, set data in the index and return it
        index.setNormsByDocument(norms);
        index.setPostingsDictionary(dictionary);
        index.setStoredByDocument(stored);
        index.setFieldNamesByOption(fields);
        return index;
    }

    /**
     * read a file corresponding to a postings block, parse the data and add it to the dictionary
     * @param dictionary PostingsDictionary where the data will be loaded
     * @param fieldName name of the field
     * @param term which block is going to be loaded
     * @return the dictionary containing the data extracted from the file
     * @throws IOException
     * @throws CorruptIndexException
     */
    @Override
    public PostingsDictionary readPostingsBlock(PostingsDictionary dictionary, String fieldName, String term) throws IOException {
        ConcurrentMap<String, byte[]> postingsDB = getPostingsDB(fieldName);

        String key = dictionary.getKeyForTerm(term);

        byte[] postings =  postingsDB.get(term);

        HashMap<String, List<Posting>> hashMap = new HashMap<>();
        try {
            if(null != postings && postings.length > 0){
                hashMap.put(term, new LinkedList<>(SerializeUtil.toListObject(postings)));
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        dictionary.getPostingsBlocksDictionary().put(key,hashMap );

        return dictionary;
    }

    /**
     * delete index files
     */
    public void reset() throws IOException, CorruptIndexException {
        File folder = new File(this.directoryPath);
        //if the directory does not exist, create it
        if (!folder.exists()) {
            folder.mkdir();
            return;
        }
        //if it exists, delete all the files that there are
        File[] files = folder.listFiles();
        for (File f: files) {
            if (f != null && f.exists()) {
                f.delete();
            }
        }
    }

    /**
     * close open files and resources
     */
    @Override
    public void close(Index index) {
        HashSet<String> indexedFields = index.getFieldNamesByOption(FieldInfo.INDEXED);
        //for every field store a file
        for(String fieldName: indexedFields) {
            NormsFile fNorms = new NormsFile(this.directoryPath.concat(DbFileDirectory.NORMS_FILE).concat(fieldName), new NormsCodec());
            fNorms.close();
            PostingsFile pFile = new PostingsFile(this.directoryPath.concat(DbFileDirectory.POSTINGS_FILE).concat(fieldName), new PostingsCodec());
            pFile.close();
        }
        Set<String> storedFields = index.getFieldNamesByOption(FieldInfo.STORED);
        //for every  stored field, load stored field file
        for(String fieldName: storedFields) {
            PostingsFile fStored = new PostingsFile(this.directoryPath.concat(DbFileDirectory.STORED_CONTENT_FILE).concat(fieldName), new StoredFieldsCodec());
            fStored.close();
        }
    }

}