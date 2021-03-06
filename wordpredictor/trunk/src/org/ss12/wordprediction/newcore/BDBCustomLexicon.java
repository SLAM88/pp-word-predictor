package org.ss12.wordprediction.newcore;

import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;

import org.ss12.wordprediction.newcore.annotations.FrequencyAnnotation;
import org.ss12.wordprediction.newcore.annotations.FrequencyAnnotationFactory;

import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.collections.StoredSortedMap;
import com.sleepycat.collections.TransactionRunner;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

/**
 * @author Brad Fol
 *
 */
public class BDBCustomLexicon<T extends AnnotatedWord & Serializable> 
		implements CustomLexicon<T> {
	static final String UNIGRAM_DB_NAME = "UnigramCustomLexicon";
	static final String BIGRAM_DB_NAME = "BigramCustomLexicon";
	static final String TRIGRAM_DB_NAME = "TrigramCustomLexicon";
	static final String CLASS_DB_NAME = "classDb";
	
	private Environment env;
	String envName = "CustomLexicon";
	private StoredClassCatalog catalog;
	private Database uniDB;
	private Database biDB;
	private Database triDB;
	private StoredSortedMap unigrams;
	private StoredSortedMap bigrams;
	private StoredSortedMap trigrams;
	TransactionRunner runner;
	
	private final Class<T> dataClass;
	private final AnnotationFactory<T> annotationFactory;
	final static String dir = "./resources/dictionaries/bdb/custom";
	
	
	private void add(SortedMap<WordSequence, T> sequenceMap,
			WordSequence wordSequence){
		T annotation = sequenceMap.get(wordSequence);
		if (annotation != null){
			annotation.update();
			sequenceMap.put(wordSequence, annotation);
		} else {
			String lastWord = wordSequence.getLastWord();
			sequenceMap.put(wordSequence, 
					annotationFactory.newAnnotation(lastWord, wordSequence.size() - 1));
		}
	}

	public void addUnigram(String word){
		try {
			add(unigrams, new WordSequence(word));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addBigram(String firstWord, String secondWord)
			throws IllegalStateException {
		try {
			add(bigrams, new WordSequence(firstWord, secondWord));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addTrigram(String firstWord, String secondWord, String thirdWord)
			throws IllegalStateException {
		try {
			add(trigrams, new WordSequence(firstWord, secondWord, thirdWord));
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	public Iterable<T> getUnigrams(String incompleteWord)
			throws IllegalStateException {
		return get(unigrams, new WordSequence(incompleteWord));
	}
	
	public Iterable<T> getBigrams(String prevWord, String incompleteWord)
			throws IllegalStateException {
		return get(bigrams, new WordSequence(prevWord, incompleteWord));
	}

	public Iterable<T> getTrigrams(String prevPrevWord, String prevWord,
			String incompleteWord) throws IllegalStateException {
		return get(trigrams, new WordSequence(prevPrevWord, prevWord,
		        incompleteWord));
	}

	public static void main(String[] args) throws Exception{
		EnvironmentConfig envConfig = new EnvironmentConfig();
		envConfig.setTransactional(true);
		envConfig.setAllowCreate(true);
		Environment myEnv = new Environment(new File(dir), envConfig);
		
		// will use class AnnotatedWord for the data type 
		// in this test code
		BDBCustomLexicon<FrequencyAnnotation> wp = 
			new BDBCustomLexicon<FrequencyAnnotation>(myEnv, 
					new FrequencyAnnotationFactory(), 
					FrequencyAnnotation.class);
		
//		wp.tester();
		wp.check();
		
		wp.close();
	}
	
	public void tester(){
//		addUnigram("cat");
		
//		addBigram("the", "cat");
//		addBigram("the", "bat");
		
		addTrigram("the", "fat", "rat");
	}
	
	public void check(){
//		Iterable<T> i = getUnigrams("a");
//		System.out.println(i);
		
//		Iterable<T> b = getBigrams("the","c");
//		System.out.println(b);
		
//		Iterable<T> b = getTrigrams("the","fat","");
//		System.out.println(b);
		
		System.out.println("unigrams= "+ unigrams);
		System.out.println("bigrams= "+ bigrams);
		System.out.println("trigrams= "+ trigrams);
	}
	
	public BDBCustomLexicon(Environment e, AnnotationFactory<T> a, Class<T> c)
			throws Exception{
		env = e;
		
		annotationFactory = a;
		dataClass = c;
		
		open();
	}

	@SuppressWarnings("unchecked")
	private void open() throws Exception{
		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setTransactional(true);
		dbConfig.setAllowCreate(true);
		
//		db = env.openDatabase(null, envName, dbConfig);
		uniDB = env.openDatabase(null, UNIGRAM_DB_NAME, dbConfig);
		biDB = env.openDatabase(null, BIGRAM_DB_NAME, dbConfig);
		triDB = env.openDatabase(null, TRIGRAM_DB_NAME, dbConfig);
		
		dbConfig = new DatabaseConfig();
		dbConfig.setSortedDuplicates(false);
		dbConfig.setAllowCreate(true);
		Database catalogDb = env.openDatabase(null, CLASS_DB_NAME, dbConfig);
		catalog = new StoredClassCatalog(catalogDb);
		
		// set the keys to be type WordSequence, and set the data to be the class 
		// set by the constructor
//		EntryBinding uniKeyBinding = new UnigramTupleBinding();
//		TupleBinding uniKeyBinding = TupleBinding.getPrimitiveBinding(UnigramTupleBinding.class);
//		TupleBinding biKeyBinding = TupleBinding.getPrimitiveBinding(BigramTupleBinding.class);
//		TupleBinding triKeyBinding = TupleBinding.getPrimitiveBinding(TrigramTupleBinding.class);
		SerialBinding dataBinding = new SerialBinding(catalog, dataClass);
		
//		map = new StoredSortedMap(db, keyBinding, dataBinding, true);
		unigrams = new StoredSortedMap(uniDB, new UnigramTupleBinding(), dataBinding, true);
		bigrams = new StoredSortedMap(biDB, new BigramTupleBinding(), dataBinding, true);
		trigrams = new StoredSortedMap(triDB, new TrigramTupleBinding(), dataBinding, true);
		
	}
	
	
	private static class UnigramTupleBinding extends TupleBinding {

		public void objectToEntry(Object o, TupleOutput out) {
			WordSequence sequence = (WordSequence) o;
			List<String> words = sequence.getWords();
			
			if(words.size() < 1){out.writeString("");} else {
				out.writeString(words.get(0));
			}
		}

		public Object entryToObject(TupleInput in) {
			WordSequence sequence = new WordSequence(in.readString());
			
			return sequence;
		}
		
	}
	
	
	private static class BigramTupleBinding extends TupleBinding {

		public void objectToEntry(Object o, TupleOutput out) {
			WordSequence sequence = (WordSequence) o;
			List<String> words = sequence.getWords();
			
			if(words.size() < 1){out.writeString("");} else {
				out.writeString(words.get(0));
			}
			if(words.size() < 2){out.writeString("");} else {
				out.writeString(words.get(1));
			}
		}

		public Object entryToObject(TupleInput in) {
			WordSequence sequence = new WordSequence(in.readString(), in.readString());
			
			return sequence;
		}
		
	}
	
	
	private static class TrigramTupleBinding extends TupleBinding {

		public void objectToEntry(Object o, TupleOutput out) {
			WordSequence sequence = (WordSequence) o;
			List<String> words = sequence.getWords();

			if(words.size() < 1){out.writeString("");} else {
				out.writeString(words.get(0));
			}
			if(words.size() < 2){out.writeString("");} else {
				out.writeString(words.get(1));
			}
			if(words.size() < 3){out.writeString("");} else {
				out.writeString(words.get(2));
			}
		}

		public Object entryToObject(TupleInput in) {
			WordSequence sequence = new WordSequence(in.readString(), in.readString(), in.readString());
			
			return sequence;
		}
		
	}
	
	public void close() {
		try {
			if (catalog != null) {
				catalog.close();
				catalog = null;
			}
			if (uniDB != null) {
				uniDB.close();
				uniDB = null;
			}
			if (biDB != null) {
				biDB.close();
				biDB = null;
			}
			if (triDB != null) {
				triDB.close();
				triDB = null;
			}
			if (env != null) {
				env.close();
				env = null;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
