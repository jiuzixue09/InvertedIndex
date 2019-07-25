package com.dave.invertedindex.document;

import java.util.HashMap;
import java.util.UUID;

/**
 * Represents a single document contained in the index
 */
public class Document {
    private String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }
    /**
     * unique identifier for the document in the index
     */
    private String documentId;

    /**
     * A document is composed of Fields. Every field is identified by its name (String)
     */
    private final HashMap<String, Field> fields = new HashMap<>();


    public Document(String documentId) {
        this.setDocumentId(documentId);
    }

    /**
     * it will automatic set the document id as uuid if you haven't set the value
     */
    public Document() {
        this.setDocumentId(generateUUID());
    }

    public HashMap<String, Field> fields() {
        return this.fields;
    }

    public void addField(final Field field) {
        this.fields.put(field.name(), field);
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}