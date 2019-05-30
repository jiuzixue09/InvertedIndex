package com.dave.invertedindex.index;

import com.dave.invertedindex.document.Document;
import com.dave.invertedindex.document.Field;
import com.dave.invertedindex.document.Term;
import com.dave.invertedindex.parse.DataStream;
import com.dave.invertedindex.parse.TextParser;
import com.dave.invertedindex.store.Directory;
import com.dave.invertedindex.store.TxtFileDirectory;
import com.dave.invertedindex.util.Logger;

import java.io.IOException;
import java.util.*;

/**
 * IndexReader provides access to read the index and perform queries
 */
public class IndexReader {

    /**
     * provides access to the files where the index data is stored
     */
    private Directory directory;

    /**
     * index data is kept inside this data structure
     */
    private Index index;

    public IndexReader(Directory directory) {
        this.directory = directory;
        this.index = Index.getInstance();
    }

    /**
     * open the index for reading, ie, load data from directory
     */
    public void open() throws IOException, CorruptIndexException {
        directory.read(index);
    }

    /**
     * close the index and all opened resources
     */
    public void close() {
        directory.close(index);
    }

    /**
     * search for occurrences of a single word in the specified field
     * @param word the word which is being searched for
     * @param fieldName in which field is the word being searched
     * @return a set of Hits with the documents that have been matched
     */
    public TreeSet<Hit> search(final String fieldName, String word) throws IOException, CorruptIndexException {
        if (fieldName.isEmpty() || word.isEmpty()) {
            throw new IllegalArgumentException("fieldName and word are required: " .concat(word). concat(" "). concat(fieldName));
        }
        Term term = new Term(fieldName, word);
        return this.query(term);
    }

    public TreeSet<Hit> search(Document document) throws IOException, CorruptIndexException {
        if (null == document || null == document.fields() || document.fields().size() < 1) {
            throw new IllegalArgumentException("document shouldn't be null");
        }

        return query(document);
    }

    private TreeSet<Hit> query(Document document) throws IOException, CorruptIndexException {
        Map<Long, Hit> map = new HashMap<>();
        for (Map.Entry<String, Field> entry : document.fields().entrySet()) {
            String fieldName = entry.getKey();
            Field field = entry.getValue();

            DataStream stream = field.getDataStream(field.getParser());

            stream.start();
            Set<String> tokens = new HashSet<>();
            while (stream.hasMoreTokens()){
                String token = stream.out();
                if(token.isBlank()) continue;
                tokens.add(token);
            }
            for (String token : tokens) {
                Term term = new Term(fieldName, token);
                LinkedList<Posting> postingsList = lookupData(term);
                if(null == postingsList || postingsList.isEmpty()) continue;
                postingsList.stream().forEach(p -> {

                    //get the norm to calculate the score of this hit,
                    int normValue = index.getDocumentNorms(term.getFieldName()).get(p.getDocumentId());
                    //score the hit proportionally to the ratio tf/norm
                    //use sqrt to compress the range of scores, log could be also be used...
                    double score = Math.sqrt((double)p.getTermFrequency() / normValue);
                    //read stored content
                    Document doc = index.document(p.getDocumentId());

                    if(map.containsKey(p.getDocumentId())){
                        map.get(p.getDocumentId()).setScore((float) (score + map.get(p.getDocumentId()).score()));
                    }else{
                        map.put(p.getDocumentId(),new Hit(doc, (float) score));
                    }

                });
            }



        }
        TreeSet<Hit> hits = new TreeSet<>();
        hits.addAll(map.values());

        return hits;
    }


    /**
     * Perform a query for a single term. Returns a Set of Hits
     * @param term Term that is being searched
     * @return Documents that match are returned as Hit objects within a Set
     */
    private TreeSet<Hit> query(final Term term) throws IOException, CorruptIndexException {
        LinkedList<Posting> postingsList = lookupData(term);

        TreeSet<Hit> hits = new TreeSet<>();
        if (postingsList == null) {
            //term was not found  an empty result
            return hits;
        }

        //traverse posting list,  get list of results and score the docs
        for (Posting p : postingsList) {
            //get the norm to calculate the score of this hit,
            int normValue = index.getDocumentNorms(term.getFieldName()).get(p.getDocumentId());
            //score the hit proportionally to the ratio tf/norm
            //use sqrt to compress the range of scores, log could be also be used...
            double score = Math.sqrt((double)p.getTermFrequency() / normValue);
            //read stored content
            Document doc = index.document(p.getDocumentId());

            //add hit to the list
            hits.add(new Hit(doc, (float) score));
        }

        return hits;
    }


    /**
     * try to load the postings list for the given term, first from memory, if not in memory
     * load from fisk
     * @param term the term searched
     * @return postings list for the term, if any occurrence is found
     * @throws IOException
     * @throws CorruptIndexException
     */
    private LinkedList<Posting> lookupData(final Term term) throws IOException, CorruptIndexException {
        //get the dictionary for this field
        PostingsDictionary dictionary = index.getPostingsDictionary(term.getFieldName());
        if (dictionary == null) {
            //this field is not indexed, so we can't search
            Logger.getInstance().error(String.format("field %s is not indexed, hence, not searchable" , term.getFieldName()));
            return null;
        }
        //first check if there is any postings list for this term already in memory
        LinkedList<Posting> postingsList = dictionary.getPostingsList(term.getToken());
        if (postingsList == null && dictionary.getPostingsBlock(dictionary.getKeyForTerm(term.token)).size() == 0) {
            //if not, try to load from disk
            dictionary = ((TxtFileDirectory)directory).readPostingsBlock(dictionary, term.getFieldName(), term.getToken());
            postingsList = dictionary.getPostingsList(term.getToken());
        }

        return postingsList;
    }


}