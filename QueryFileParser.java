package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.usfca.cs272.InvertedIndex.SingleSearchResult;

/**
 * Query File Parser Class Responsible For Reading The Queries And Writing Them
 * In JSON
 *
 * @author Alessandro Barrera
 *
 */
public class QueryFileParser implements QueryFileParserInterface {

    /**
     * Inverted Index
     */
    private final InvertedIndex index;

    /**
     * Map containing the stemmed word as key, the value is a list of all the search
     * results
     */
    private final Map<String, List<SingleSearchResult>> storeSearchData;

    /**
     * Constructor of QueryFileParser
     *
     * @param index Inverted Index
     */
    public QueryFileParser(InvertedIndex index) {
        storeSearchData = new TreeMap<>();
        this.index = index;
    }

    /**
     * Parse line method to parse one line
     *
     * @param line  to parse
     * @param exact keyword to do an exact search or not
     * @throws IOException if an IO error occurs
     */
    @Override
    public void parseLine(String line, boolean exact) throws IOException {
        var uniqueSet = TextFileStemmer.uniqueStems(line);

        String stemmedLine = String.join(" ", uniqueSet);
        if (!stemmedLine.isEmpty() && !storeSearchData.containsKey(stemmedLine)) {

            storeSearchData.put(stemmedLine, index.search(uniqueSet, exact));
        }
    }

    /**
     * Write to JSON format
     *
     * @param output address to write
     * @throws IOException if an IO error occurs
     */
    @Override
    public void writeJSON(Path output) throws IOException {
        SimpleJsonWriter.writeSearch(storeSearchData, output);
    }

    @Override
    public String toString() {
        return storeSearchData.toString();
    }
}
