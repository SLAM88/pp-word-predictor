package org.ss12.wordprediction.newcore;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A {@link CustomLexicon} implementation that stores all {@link AnnotatedWord}
 * instances in memory, offering fast read and write access.
 * 
 * @author Michael Parker
 */
public class TreeMapCustomLexicon<T extends AnnotatedWord> implements
    CustomLexicon<T> {
  private final AnnotationFactory<T> annotationFactory;

  private final SortedMap<WordSequence, T> unigrams;
  private final SortedMap<WordSequence, T> bigrams;
  private final SortedMap<WordSequence, T> trigrams;

  public TreeMapCustomLexicon(AnnotationFactory<T> annotationFactory) {
    this.annotationFactory = annotationFactory;

    unigrams = new TreeMap<WordSequence, T>();
    bigrams = new TreeMap<WordSequence, T>();
    trigrams = new TreeMap<WordSequence, T>();
  }

  private void add(SortedMap<WordSequence, T> sequenceMap,
      WordSequence wordSequence) {
    T annotation = sequenceMap.get(wordSequence);
    if (annotation != null) {
      annotation.update();
    } else {
      String lastWord = wordSequence.getLastWord();
      sequenceMap.put(wordSequence, annotationFactory.newAnnotation(lastWord));
    }
  }

  public void addUnigram(String word) {
    add(unigrams, new WordSequence(word));
  }

  public void addBigram(String firstWord, String secondWord) {
    add(bigrams, new WordSequence(firstWord, secondWord));
  }

  public void addTrigram(String firstWord, String secondWord, String thirdWord) {
    add(trigrams, new WordSequence(firstWord, secondWord, thirdWord));
  }

  private Iterable<T> get(SortedMap<WordSequence, T> sequenceMap,
      WordSequence wordSequence) {
    WordSequence upperBound = WordSequence.getNextSequence(wordSequence);
    if (upperBound == null) {
      return Collections.unmodifiableCollection(sequenceMap
          .tailMap(wordSequence).values());
    }
    return Collections.unmodifiableCollection(sequenceMap.subMap(
        wordSequence, upperBound).values());
  }

  public Iterable<T> getUnigrams(String incompleteWord) {
    return get(unigrams, new WordSequence(incompleteWord));
  }

  public Iterable<T> getBigrams(String incompleteWord, String prevWord) {
    return get(bigrams, new WordSequence(prevWord, incompleteWord));
  }

  public Iterable<T> getTrigrams(String incompleteWord, String prevWord,
      String prevPrevWord) {
    return get(trigrams, new WordSequence(prevPrevWord, prevWord,
        incompleteWord));
  }
}
