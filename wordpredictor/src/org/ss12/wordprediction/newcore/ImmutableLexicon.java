package org.ss12.wordprediction.newcore;

/**
 * An immutable collection of {@link WordSignificance} instances, sorted
 * lexicographically by word.
 * 
 * @author Michael Parker
 */
public interface ImmutableLexicon {
  /**
   * Returns the significance of the given word.
   * 
   * @param word the word to get the significance of
   * @return its significance, or {@code null} if the word was not found
   */
  public WordSignificance getSignificance(String word);

  /**
   * Returns the significance of all words lexicographically between the given
   * bounds.
   * 
   * @param lowBound the inclusive lower bound, or {@code null} for no lower
   *          bound
   * @param highBound the exclusive higher bound, or {@code null} for no higher
   *          bound
   * @return an iterable over the significance of all words between the bounds,
   *         possibly empty
   */
  public Iterable<WordSignificance> getSignificance(String lowBound,
      String highBound);
}
