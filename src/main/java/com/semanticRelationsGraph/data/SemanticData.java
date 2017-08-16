package com.semanticRelationsGraph.data;

/**
 * Created by Oliver on 4/11/2017.
 */
public class SemanticData {

    private String atomicSubject = "";

    private String extendedSubject = "";

    private String atomicVerbPredicate = "";

    private String extendedVerbPredicate = "";

    private String atomicNounPredicate = "";

    private String extendedNounPredicate = "";

    private String sentence = "";

    private String wikiTopic = "";

    public SemanticData(String atomicSubject, String extendedSubject, String atomicVerbPredicate,
                        String extendedVerbPredicate, String atomicNounPredicate, String extendedNounPredicate,
                        String sentence, String wikiTopic) {
        this.atomicSubject = atomicSubject;
        this.extendedSubject = extendedSubject;
        this.atomicVerbPredicate = atomicVerbPredicate;
        this.extendedVerbPredicate = extendedVerbPredicate;
        this.atomicNounPredicate = atomicNounPredicate;
        this.extendedNounPredicate = extendedNounPredicate;
        this.sentence = sentence;
        this.wikiTopic = wikiTopic;
    }

    public String getAtomicSubject() {
        return atomicSubject;
    }

    public String getExtendedSubject() {
        return extendedSubject;
    }

    public String getAtomicVerbPredicate() {
        return atomicVerbPredicate;
    }

    public String getExtendedVerbPredicate() {
        return extendedVerbPredicate;
    }

    public String getAtomicNounPredicate() {
        return atomicNounPredicate;
    }

    public String getExtendedNounPredicate() {
        return extendedNounPredicate;
    }

    public String getSentence() {
        return sentence;
    }

    public String getWikiTopic() {
        return wikiTopic;
    }
}
