package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.usfca.cs272.InvertedIndex.SingleSearchResult;

/**
 * Query File Parser Class Responsible For Reading The Queries And Writing them
 *
 * @author Alessandro Barrera
 *
 */
public class ThreadSafeQueryFileParser implements QueryFileParserInterface {

    /**
     * Inverted Index
     */
    private final ThreadSafeInvertedIndex index;

    /**
     * Map containing the stemmed word as key, the value is a list of all the search
     * results
     */
    private final Map<String, List<SingleSearchResult>> storeSearchData;

    /**
     * Work Queue to use for multitheading
     */
    private final WorkQueue workQueue;

    /**
     * Constructor of QueryFileParser
     *
     * @param index Inverted Index
     */
    public ThreadSafeQueryFileParser(ThreadSafeInvertedIndex index) {
        storeSearchData = new TreeMap<>();
        this.index = index;
        workQueue = null;
    }

    /**
     * Constructor of QueryFileParser
     *
     * @param invertedIndex Inverted Index
     * @param workQueue     built workQueue
     */
    public ThreadSafeQueryFileParser(ThreadSafeInvertedIndex invertedIndex, WorkQueue workQueue) {
        storeSearchData = new TreeMap<>();
        this.index = invertedIndex;
        this.workQueue = workQueue;
    }

    /**
     * Parse file method which parses the entire file
     *
     * @param file  to parse
     * @param exact keyword to do an exact search or not
     * @throws IOException if an IO error occurs
     */
    @Override
    public void parseFile(Path file, boolean exact) throws IOException {
        QueryFileParserInterface.super.parseFile(file, exact);
        workQueue.finish();
    }

    /**
     * Write to JSON format
     *
     * @param output address to write
     * @throws IOException if an IO error occurs
     */
    @Override
    public void writeJSON(Path output) throws IOException {
        synchronized (storeSearchData) {
            SimpleJsonWriter.writeSearch(storeSearchData, output);
        }
    }

    /**
     * Inner class called Task implementing Runnable
     *
     * @author alessandrobarrera
     *
     */
    public class Task implements Runnable {
        /**
         * Line to Add
         */
        private final String line;

        /**
         * Exact search or not
         */
        private final boolean exact;

        /**
         * Task used for multithreading assigning each corresponding value.
         *
         * @param line  to add
         * @param exact search or not
         */
        public Task(String line, boolean exact) {
            this.line = line;
            this.exact = exact;
        }

        @Override
        public void run() {

            var uniqueSet = TextFileStemmer.uniqueStems(line);
            String stemmedLine = String.join(" ", uniqueSet);

            synchronized (storeSearchData) {
                if (stemmedLine.isEmpty() || storeSearchData.containsKey(stemmedLine)) {
                    return;
                }
            }

            var local = index.search(uniqueSet, exact);

            synchronized (storeSearchData) {
                storeSearchData.put(stemmedLine, local);
            }
        }
    }

    @Override
    public void parseLine(String line, boolean exact) throws IOException {
        workQueue.execute(new Task(line, exact));
    }

}
