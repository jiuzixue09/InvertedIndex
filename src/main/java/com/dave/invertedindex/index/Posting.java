package com.dave.invertedindex.index;

import java.io.Serializable;

/**
 * A Posting represents an occurrence of a Term inside a document:
 */
public class Posting implements Serializable {


    /**
     * document where the term occurs
     */
    private String documentId;

    /**
     * number of times that the term occurs in the document
     */
    private short termFrequency;

    /**
     * @param documentId documentId
     * @param termFrequency term frequency
     */
    public Posting(final String documentId, short termFrequency) {
        this.documentId = documentId;
        this.termFrequency = termFrequency;
    }

    public Posting(final String documentId) {
        this(documentId, (short)0);
    }

    public String getDocumentId() {
        return documentId;
    }

    public int getTermFrequency() {
        return termFrequency;
    }

    public void addOccurrence() {
        this.termFrequency++;
    }

    public String toString() {
        return String.format("%d,%d", this.documentId, this.termFrequency);
    }
}