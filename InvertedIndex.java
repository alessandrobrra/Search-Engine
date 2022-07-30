package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * Inverted Index Data Structure Class
 *
 */
public class InvertedIndex {

    /**
     * @author Alessandro Barrera
     *
     */
    public class SingleSearchResult implements Comparable<SingleSearchResult> {
        /**
         * Location of the file
         */
        private final String location;
        /**
         * Query count of the file
         */
        private int queryCount;
        /**
         * Score of the word
         */
        private double score;

        /**
         * Single search method constructor
         *
         * @param location file location
         *
         */

        public SingleSearchResult(String location) {
            this.location = location;
            this.queryCount = 0;
            this.score = 0.0;
        }

        @Override
        public int compareTo(SingleSearchResult o) {
            if (this.score != o.score) {
                return Double.compare(o.score, this.score);
            } else if (this.queryCount != o.queryCount) {
                return Integer.compare(o.queryCount, this.queryCount);
            } else {
                return this.location.compareToIgnoreCase(o.location);
            }
        }

        /**
         * Gets location
         *
         * @return String
         */
        public String getLocation() {
            return location;
        }

        /**
         * Gets Query count
         *
         * @return Integer
         */
        public int getQueryCount() {
            return queryCount;
        }

        /**
         * Gets score
         *
         * @return Double
         */
        public Double getScore() {
            return score;
        }

        @Override
        public String toString() {
            return String.format(this.location + ", " + this.score + ", " + this.queryCount);
        }

        /**
         * Update method for the query count and the score
         *
         * @param word passed in to look up the query count
         *
         */
        private void update(String word) {
            this.queryCount += wordMap.get(word).get(location).size();
            this.score = this.queryCount / (double) fileCount.get(location);
        }

    }

    /**
     * Map containing the file location as key and the word count of the file as
     * value
     */
    private final Map<String, Integer> fileCount;

    /**
     * Nested inverted index data structure
     */
    private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> wordMap;

    /**
     * Constructor to initialize inverted index data structure
     */
    public InvertedIndex() {
        wordMap = new TreeMap<>();
        fileCount = new TreeMap<>();
    }

    /**
     * Add method to add the word, location, and position to the Inverted Index
     *
     * @param word     to get the stemmed word we are looking for
     * @param location of the word we are adding the file
     * @param position to add the number
     */
    public void add(String word, String location, int position) {
        wordMap.putIfAbsent(word, new TreeMap<>());
        wordMap.get(word).putIfAbsent(location, new TreeSet<>());
        wordMap.get(word).get(location).add(position);

        if (fileCount.getOrDefault(location, 0) < position) {
            fileCount.put(location, position);
        }

    }

    /**
     * Adds all the words of a list with it's location and position to the Inverted
     * Index
     *
     * @param words    the list of words to add
     * @param location of the words to add
     */
    public void addAll(List<String> words, String location) {
        for (int i = 0; i < words.size(); i++) {
            add(words.get(i), location, i + 1);
        }
    }

    /**
     * Adds all of the passed in Inverted Index values into the class Inverted Index
     * object
     *
     * @param local Inverted Index used temporarily
     */
    public void addAll(InvertedIndex local) {

        for (String word : local.wordMap.keySet()) {
            if (!wordMap.containsKey(word)) {
                wordMap.put(word, local.wordMap.get(word));
            } else {
                for (String location : local.wordMap.get(word).keySet()) {
                    if (!wordMap.get(word).containsKey(location)) {
                        wordMap.get(word).put(location, local.wordMap.get(word).get(location));
                    } else {
                        wordMap.get(word).get(location).addAll(local.wordMap.get(word).get(location));
                    }
                }
            }
        }

        for (String location : local.fileCount.keySet()) {
            if (!fileCount.containsKey(location)) {
                fileCount.put(location, local.fileCount.get(location));
            } else {
                int total = fileCount.get(location) + local.fileCount.get(location);
                if (fileCount.getOrDefault(location, 0) < total) {
                    fileCount.put(location, total);
                }
            }
        }

    }

    /**
     * Returns boolean value depending if the outer map contains the key word
     *
     * @param word to check if the map contains that word
     * @return size of outer map
     */
    public boolean contains(String word) {
        return wordMap.containsKey(word);
    }

    /**
     * Returns boolean value depending if the inner map contains the key location
     *
     * @param word     to get the map
     * @param location to check if the inner map contains that word
     * @return inner map
     */
    public boolean contains(String word, String location) {
        return contains(word) && wordMap.get(word).containsKey(location);
    }

    /**
     * Returns boolean value depending if the inner set contains the index
     *
     * @param word     to get the map
     * @param location of the word
     * @param index    number to check if the inner set contains it
     * @return size of the inner set
     */
    public boolean contains(String word, String location, Integer index) {
        return contains(word, location) && wordMap.get(word).get(location).contains(index);
    }

    /**
     * Exact search method that puts the word and it's list of search results in the
     * map
     *
     * @param parsedWords list of parsed words
     * @return List of type SingleSearchResult
     */
    public List<SingleSearchResult> exactSearch(Set<String> parsedWords) {
        HashMap<String, SingleSearchResult> queryMap = new HashMap<>();
        List<SingleSearchResult> searchResults = new ArrayList<>();
        for (String word : parsedWords) {
            if (wordMap.containsKey(word)) {
                var iterator = wordMap.get(word).entrySet().iterator();
                searchHelper(iterator, queryMap, searchResults, word);
            }
        }

        Collections.sort(searchResults);
        return searchResults;
    }

    /**
     * Partial search method that puts the word and it's list of search results in
     * the map
     *
     * @param parsedWords list of parsed words
     * @return List of type SingleSearchResult
     */
    public List<SingleSearchResult> partialSearch(Set<String> parsedWords) {
        HashMap<String, SingleSearchResult> queryMap = new HashMap<>();

        List<SingleSearchResult> searchResults = new ArrayList<>();
        for (String word : parsedWords) {
            var entry = wordMap.tailMap(word).entrySet().iterator();
            while (entry.hasNext()) {
                var key = entry.next();
                if (key.getKey().startsWith(word)) {
                    searchHelper(key.getValue().entrySet().iterator(), queryMap, searchResults, key.getKey());
                } else {
                    break;
                }
            }
        }

        Collections.sort(searchResults);
        return searchResults;
    }

    /**
     * Search Helper method
     *
     * @param iterator      used to iterate over map
     * @param queryMap      used to put search results
     * @param searchResults list to sort
     * @param word          to update
     */
    private void searchHelper(Iterator<Entry<String, TreeSet<Integer>>> iterator,
            HashMap<String, SingleSearchResult> queryMap, List<SingleSearchResult> searchResults, String word) {
        while (iterator.hasNext()) {
            var name = iterator.next();
            String location = name.getKey();
            if (!queryMap.containsKey(location)) {
                queryMap.put(location, new SingleSearchResult(location));
                searchResults.add(queryMap.get(location));
            }
            queryMap.get(location).update(word);
        }
    }

    /**
     * Search function to perform an exact or partial search depending of the exact
     * boolean
     *
     * @param parsedWords Set of parsed words
     * @param exact       boolean to perform exact or partial search
     * @return List of type SingleSearchResult
     */
    public List<SingleSearchResult> search(Set<String> parsedWords, boolean exact) {
        if (exact) {
            return exactSearch(parsedWords);
        }
        return partialSearch(parsedWords);
    }

    /**
     * Getter method to get outer map key set
     *
     * @return unmodifiable map key-sets
     */
    public Collection<String> get() {
        return Collections.unmodifiableCollection(wordMap.keySet());
    }

    /**
     * Getter method to get inner map key set
     *
     * @param word to get the map
     * @return unmodifiable map
     */
    public Collection<String> get(String word) {
        return contains(word) ? Collections.unmodifiableSet(wordMap.get(word).keySet()) : Collections.emptySet();
    }

    /**
     * Getter method to get inner set
     *
     * @param word     to get the map
     * @param location of the word
     * @return unmodifiable map
     */
    public Collection<Integer> get(String word, String location) {
        return contains(word) ? Collections.unmodifiableCollection(wordMap.get(word).get(location)) : null;
    }

    /**
     * Returns unmodifiable fileCount map
     *
     * @return unmodifiable file count map
     */
    public Map<String, Integer> getFileCount() {
        return Collections.unmodifiableMap(fileCount);
    }

    /**
     * Returns size of the outer map
     *
     * @return size of outer map
     */
    public int size() {
        return wordMap.keySet().size();
    }

    /**
     * Returns size of the inner map
     *
     * @param word to get the map
     * @return inner map
     */
    public int size(String word) {
        if (contains(word)) {
            return wordMap.get(word).keySet().size();
        }
        return -1;
    }

    /**
     * Returns size of the inner set
     *
     * @param word     to get the map
     * @param location of the word
     * @return size of the inner set
     */
    public int size(String word, String location) {
        if (contains(word, location)) {
            return wordMap.get(word).get(location).size();
        }
        return -1;
    }

    /**
     * toJSON method writer
     *
     * @param writer to write
     * @throws IOException if an IO error occurs
     */
    public void toJSON(Path writer) throws IOException {
        SimpleJsonWriter.writeDoubleNestedArray(wordMap, writer);
    }

    @Override
    public String toString() {
        return wordMap.toString();
    }

}