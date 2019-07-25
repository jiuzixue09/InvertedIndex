package com.dave.invertedindex.index;

import com.dave.invertedindex.document.Document;
import com.dave.invertedindex.document.Field;
import com.dave.invertedindex.document.FieldInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Inverted Index, modeled using a postings lists,  which  maps terms occurrences to documents
 */
public class Index {
    /**
     * keep a list of the field names  of the fields that are indexed and another for the stored
     */
    protected HashMap<String, HashSet<String>> fieldNamesByOption = new HashMap<>();

    /**
     * For every indexed field,  keep a postings dictionary
     * A postings dictionary keep list of terms-postings list
     * In a terms-postings list,for every indexed Term we keep a list of Postings
     */
    protected Map<String, PostingsDictionary> postingsDictionary = new HashMap<>();

    /**
     * norms of every docId-fieldName
     */
    protected Map<String, Map<String, Integer>> normsByDocument = new HashMap<>();

    /**
     * For every stored field, we have a HashMap with documentId as key and the stored field as value
     */
    protected Map<String, Map<String, String>> storedByDocument = new HashMap<>();


    /**
     * number of documents indexed. used to assign unique Ids to the documents
     */
    protected long numDocs = 0;

    /**
     * number of terms indexed
     */
    protected long numTerms = 0;


    private static Index instance = null;

    /**
     * use singleton to have only one instance of the Index, just in case both IndexWriter and IndexReader
     * are used simultaneously, they should access the same data
     * @return
     */
    public static Index getInstance() {
        if (instance == null) {
            instance = new Index();
        }
        return instance;
    }

    private Index() {
        //to protect from direct instantiation
    }


    public long getNumDocs() {
        return numDocs;
    }

    public long getNumTerms() {
        return numTerms;
    }

    /**
     * when a new doc is created, increase numDocs and return the value as Id
     * @return new value for the count, which will be used as Id for next document
     */
    public long nextDocumentId() {
        return ++this.numDocs;
    }

    public void setNumDocs(long numDocs) {
        this.numDocs = numDocs;
    }

    /**
     * if a new term is added to the dictionary, increase the counter
     * @return the new value for the count
     */
    public long newTerm() {
        return ++this.numTerms;
    }

    /**
     * Get postings dictionary for the given field
     * If there's any defined, set up a new one
     * @param fieldName
     * @return
     */
    public PostingsDictionary getPostingsDictionary(final String fieldName) {
        PostingsDictionary dictionary = this.postingsDictionary.computeIfAbsent(fieldName, k -> new PostingsDictionary());
        return dictionary;
    }


    /**
     * Get document norms for fieldName
     * If there's any defined, set up a new one
     * @param fieldName
     * @return
     */
    public Map<String, Integer> getDocumentNorms(final String fieldName) {
        Map<String, Integer> norms = this.normsByDocument.computeIfAbsent(fieldName, k -> new HashMap<>());
        return norms;
    }

    /**
     * Get list of documents stored fields for  fieldName
     * If there's any defined, set up a new one
     * @param fieldName
     * @return
     */
    public Map<String, String> getStoredDocuments(final String fieldName) {
        Map<String, String> stored = this.storedByDocument.computeIfAbsent(fieldName, k -> new HashMap<>());
        return stored;
    }

    /**
     * get fields which have configured the  option
     * @return a set containing fields which have configured the specified option
     */
    public HashSet<String> getFieldNamesByOption(final String fieldOption) {
        HashSet<String> fields = this.fieldNamesByOption.get(fieldOption);
        if (fields == null) {
            if (fieldOption.equals(FieldInfo.INDEXED)) {
                if (!this.postingsDictionary.isEmpty()) {
                    Set<String> keySet = this.postingsDictionary.keySet();
                    fields = new HashSet<>(keySet);
                } else {
                    fields = new HashSet<>();
                }
            } else if (fieldOption.equals(FieldInfo.STORED)) {
                if (!this.storedByDocument.isEmpty()) {
                    Set<String> keySet = this.storedByDocument.keySet();
                    fields = new HashSet<>(keySet);
                } else {
                    fields = new HashSet<>();
                }
            }
            if(fields.size() > 0)
             this.fieldNamesByOption.put(fieldOption, fields);
        }
        return fields;
    }


    public void setPostingsDictionary(HashMap<String, PostingsDictionary> postingsDictionary) {
        this.postingsDictionary = postingsDictionary;
    }

    public HashMap<String, HashSet<String>> getFieldNamesByOption() {
        return this.fieldNamesByOption;
    }

    public void setNormsByDocument(Map<String, Map<String, Integer>> normsByDocument) {
        this.normsByDocument = normsByDocument;
    }

    public void setStoredByDocument(Map<String, Map<String, String>> storedByDocument) {
        this.storedByDocument = storedByDocument;
    }

    public void setFieldNamesByOption(HashMap<String, HashSet<String>> fieldNamesByOption) {
        this.fieldNamesByOption = fieldNamesByOption;
    }

    /**
     * get a list of the field names from which we have content stored
     * @return
     */
    public HashSet<String> getStoredFields() {
        return this.fieldNamesByOption.get(FieldInfo.STORED);
    }

    /**
     * get a list of the field names from which we have text indexed
     * @return
     */
    public HashSet<String> getIndexedFields() {
        return this.fieldNamesByOption.get(FieldInfo.INDEXED);
    }

    /**
     * retrieves all stored fields for the given documentId
     * @param documentId
     * @return Document containing retrieved data
     */
    public Document document(final String documentId) {
        Document doc = new Document(documentId);

        for(String fieldName: storedByDocument.keySet()) {
            Map<String, String> storedData = this.storedByDocument.get(fieldName);
            if (storedData.containsKey(documentId)) {
                Field f = new Field(fieldName, storedData.get(documentId));
                doc.addField(f);
            }
        }
        return doc;
    }

    /**
     * deletes all data in the index, reset the object to its initial state
     */
    public void reset() {
        this.numDocs = 0;
        this.numTerms = 0;
        this.fieldNamesByOption.clear();
        this.postingsDictionary.clear();
        this.normsByDocument.clear();
        this.storedByDocument.clear();
    }

    public void clear(){
        this.fieldNamesByOption.clear();
        this.postingsDictionary.clear();
        this.normsByDocument.clear();
        this.storedByDocument.clear();
    }
}
