package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.usfca.cs272.InvertedIndex.SingleSearchResult;

/**
 * Outputs several simple data structures in "pretty" JSON format where newlines
 * are used to separate elements and nested elements are indented using tabs
 *
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2022
 */
public class SimpleJsonWriter {
    /**
     * Writes multiple search result
     *
     * @param searchIterator iterator to go over List of Single Search Result
     * @param writer         to write
     * @throws IOException if an IO error occurs
     */
    private static void multipleSearchResults(ListIterator<SingleSearchResult> searchIterator, Writer writer)
            throws IOException {
        var entry = searchIterator;
        if (entry.hasNext()) {
            multipleSearchResultsHelper(searchIterator, writer);
            while (entry.hasNext()) {
                writer.write(",");
                multipleSearchResultsHelper(searchIterator, writer);
            }
        }
    }

    /**
     * Helper method for multuipleSearchResults
     *
     * @param searchIterator iterator to go over List of Single Search Result
     * @param writer         to write
     * @throws IOException if an IO error occurs
     */
    private static void multipleSearchResultsHelper(ListIterator<SingleSearchResult> searchIterator, Writer writer)
            throws IOException {
        var temp = searchIterator.next();
        writer.write("\n");
        writeIndent("{", writer, 2);
        writer.write("\n");
        writeQuote("count", writer, 3);
        writer.write(": ");
        int query = temp.getQueryCount();
        String queryToString = "" + query;
        writer.write(queryToString);
        writer.write(",");
        writer.write("\n");
        writeQuote("score", writer, 3);
        double score = temp.getScore();
        writer.write(": " + String.format("%.8f", score));
        writer.write(",");
        writer.write("\n");
        writeQuote("where", writer, 3);
        writer.write(": \"");
        writer.write(temp.getLocation());
        writer.write("\"");
        writer.write("\n");
        writeIndent("}", writer, 2);
    }

    /**
     * Returns the elements as a pretty JSON array.
     *
     * @param elements the elements to use
     * @return a {@link String} containing the elements in pretty JSON format
     *
     * @see #writeArray(Collection, Writer, int)
     */
    public static String writeArray(Collection<Integer> elements) {
        try {
            StringWriter writer = new StringWriter();
            writeArray(elements, writer, 0);
            return writer.toString();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Writes the elements as a pretty JSON array to file.
     *
     * @param elements the elements to write
     * @param path     the file path to use
     * @throws IOException if an IO error occurs
     *
     * @see #writeArray(Collection, Writer, int)
     */
    public static void writeArray(Collection<Integer> elements, Path path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
            writeArray(elements, writer, 0);
        }
    }

    /**
     * Writes the elements as a pretty JSON array.
     *
     * @param elements the elements to write
     * @param writer   the writer to use
     * @param indent   the initial indent level; the first bracket is not indented,
     *                 inner elements are indented by one, and the last bracket is
     *                 indented at the initial indentation level
     * @throws IOException if an IO error occurs
     */
    public static void writeArray(Collection<Integer> elements, Writer writer, int indent) throws IOException {
        writer.write("[");
        Iterator<Integer> iterator = elements.iterator();
        if (iterator.hasNext()) {
            writer.write("\n");
            writeIndent(iterator.next().toString(), writer, indent + 1);
        }
        while (iterator.hasNext()) {
            writer.write(",\n");
            writeIndent(iterator.next().toString(), writer, indent + 1);
        }
        writer.write("\n");
        writeIndent("]", writer, indent);
    }

    /**
     * Returns the elements as a pretty JSON object with double-nested arrays.
     *
     * @param elements the elements to use
     * @return a {@link String} containing the elements in pretty JSON format
     *
     */
    public static String writeDoubleNestedArray(TreeMap<String, TreeMap<String, TreeSet<Integer>>> elements) {
        try {
            StringWriter writer = new StringWriter();
            writeDoubleNestedArray(elements, writer, 0);
            return writer.toString();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Writes the elements as a pretty JSON object with double-nested arrays to
     * file.
     *
     * @param wordMap the elements to write
     * @param path    the file path to use
     * @throws IOException if an IO error occurs
     *
     */

    public static void writeDoubleNestedArray(TreeMap<String, TreeMap<String, TreeSet<Integer>>> wordMap, Path path)
            throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
            writeDoubleNestedArray(wordMap, writer, 0);
        }
    }

    /**
     * Writes the elements as a pretty JSON object with double-nested arrays. The
     * generic notation used allows this method to be used for any type of map with
     * any type of nested collection of integer objects.
     *
     * @param wordMap the elements to write
     * @param writer  the writer to use
     * @param indent  the initial indent level; the first bracket is not indented,
     *                inner elements are indented by one, and the last bracket is
     *                indented at the initial indentation level
     * @throws IOException if an IO error occurs
     */
    public static void writeDoubleNestedArray(TreeMap<String, TreeMap<String, TreeSet<Integer>>> wordMap, Writer writer,
            int indent) throws IOException {
        writer.write("{");
        var iterator = wordMap.entrySet().iterator();
        if (iterator.hasNext()) {
            writeDoubleNestedArrayHelper(iterator, writer, indent);
        }
        while (iterator.hasNext()) {
            writer.write(",");
            writeDoubleNestedArrayHelper(iterator, writer, indent);
        }
        writeIndent("\n}", writer, indent);
    }

    /**
     * Helper method for the writeDoubleNestedArray method
     *
     * @param iterator use to iterate
     * @param writer   to write
     * @param indent   the initial indent level; the first bracket is not indented,
     *                 inner elements are indented by one, and the last bracket is
     *                 indented at the initial indentation level
     * @throws IOException if an IO error occurs
     *
     */
    public static void writeDoubleNestedArrayHelper(Iterator<Entry<String, TreeMap<String, TreeSet<Integer>>>> iterator,
            Writer writer, int indent) throws IOException {
        var entry = iterator.next();
        writer.write("\n");
        writeQuote(entry.getKey(), writer, indent + 1);
        writer.write(": ");
        writeNestedArray(entry.getValue(), writer, indent + 1);
    }

    /**
     * Writes the elements as an Entry format
     *
     * @param iterator used to get the entry set
     * @param writer   to write the results
     * @param indent   to get indentation
     * @throws IOException if an IO error occurs
     */
    public static void writeEntry(Iterator<Entry<String, Integer>> iterator, Writer writer, int indent)
            throws IOException {
        var entry = iterator.next();
        if (entry.getValue() > 0) {
            writer.write("\n");
            writeQuote(entry.getKey(), writer, indent + 1);
            writer.write(": " + entry.getValue());
            if (iterator.hasNext()) {
                writer.write(",");
            }
        }
    }

    /**
     * Indents and then writes the String element.
     *
     * @param element the element to write
     * @param writer  the writer to use
     * @param indent  the number of times to indent
     * @throws IOException if an IO error occurs
     */
    public static void writeIndent(String element, Writer writer, int indent) throws IOException {
        writeIndent(writer, indent);
        writer.write(element);
    }

    /**
     * Indents the writer by the specified number of times. Does nothing if the
     * indentation level is 0 or less.
     *
     * @param writer the writer to use
     * @param indent the number of times to indent
     * @throws IOException if an IO error occurs
     */
    public static void writeIndent(Writer writer, int indent) throws IOException {
        while (indent-- > 0) {
            writer.write('\t');
        }
    }

    /**
     * Writes the elements as a pretty JSON array to file.
     *
     * @param map  the elements to write
     * @param path the file path to use
     * @throws IOException if an IO error occurs
     *
     * @see #writeArray(Collection, Writer, int)
     */
    public static void writeJSON(Map<String, Integer> map, Path path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
            writeJSON(map, writer, 0);
        }
    }

    /**
     * Writes the elements as a pretty JSON array.
     *
     * @param map    the elements to write
     * @param writer to write to results
     *
     * @param indent the initial indent level; the first bracket is not indented,
     *               inner elements are indented by one, and the last bracket is
     *               indented at the initial indentation level
     * @throws IOException if an IO error occurs
     */
    public static void writeJSON(Map<String, Integer> map, Writer writer, int indent) throws IOException {
        writer.write("{");
        var iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            writeEntry(iterator, writer, indent);
        }

        writer.write("\n}");
    }

    /**
     * Returns the elements as a pretty JSON object with nested arrays.
     *
     * @param elements the elements to use
     * @return a {@link String} containing the elements in pretty JSON format
     *
     * @see #writeNestedArray(Map, Writer, int)
     */
    public static String writeNestedArray(Map<String, ? extends Collection<Integer>> elements) {
        try {
            StringWriter writer = new StringWriter();
            writeNestedArray(elements, writer, 0);
            return writer.toString();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Writes the elements as a pretty JSON object with nested arrays to file.
     *
     * @param elements the elements to write
     * @param path     the file path to use
     * @throws IOException if an IO error occurs
     *
     * @see #writeNestedArray(Map, Writer, int)
     */
    public static void writeNestedArray(Map<String, ? extends Collection<Integer>> elements, Path path)
            throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
            writeNestedArray(elements, writer, 0);
        }
    }

    /**
     * Writes the elements as a pretty JSON object with nested arrays. The generic
     * notation used allows this method to be used for any type of map with any type
     * of nested collection of integer objects.
     *
     * @param elements the elements to write
     * @param writer   the writer to use
     * @param indent   the initial indent level; the first bracket is not indented,
     *                 inner elements are indented by one, and the last bracket is
     *                 indented at the initial indentation level
     * @throws IOException if an IO error occurs
     */
    public static void writeNestedArray(Map<String, ? extends Collection<Integer>> elements, Writer writer, int indent)
            throws IOException {
        writer.write("{");
        var iterator = elements.entrySet().iterator();
        if (iterator.hasNext()) {
            writeNestedArrayHelper(iterator, writer, indent);
        }
        while (iterator.hasNext()) {
            writer.write(",");
            writeNestedArrayHelper(iterator, writer, indent);
        }
        writer.write("\n");
        writeIndent("}", writer, indent);
    }

    /**
     * Helper method for the writeNestedArray method
     *
     * @param iterator use to iterate
     * @param writer   to write
     * @param indent   indentation levels
     * @throws IOException if an IO error occurs
     */
    public static void writeNestedArrayHelper(Iterator<? extends Entry<String, ? extends Collection<Integer>>> iterator,
            Writer writer, int indent) throws IOException {
        var entry = iterator.next();
        writer.write("\n");
        writeQuote(entry.getKey(), writer, indent + 1);
        writer.write(": ");
        writeArray(entry.getValue(), writer, indent + 1);
    }

    /**
     * Returns the elements as a pretty JSON object.
     *
     * @param elements the elements to use
     * @return a {@link String} containing the elements in pretty JSON format
     *
     * @see #writeObject(Map, Writer, int)
     */
    public static String writeObject(Map<String, Integer> elements) {
        try {
            StringWriter writer = new StringWriter();
            writeObject(elements, writer, 0);
            return writer.toString();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Writes the elements as a pretty JSON object to file.
     *
     * @param elements the elements to write
     * @param path     the file path to use
     * @throws IOException if an IO error occurs
     *
     * @see #writeObject(Map, Writer, int)
     */
    public static void writeObject(Map<String, Integer> elements, Path path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
            writeObject(elements, writer, 0);
        }
    }

    /**
     * Writes the elements as a pretty JSON object.
     *
     * @param elements the elements to write
     * @param writer   the writer to use
     * @param indent   the initial indent level; the first bracket is not indented,
     *                 inner elements are indented by one, and the last bracket is
     *                 indented at the initial indentation level
     * @throws IOException if an IO error occurs
     */
    public static void writeObject(Map<String, Integer> elements, Writer writer, int indent) throws IOException {
        writer.write("{");
        var iterator = elements.entrySet().iterator();
        if (iterator.hasNext()) {
            writeObjectHelper(iterator, writer, indent);

            while (iterator.hasNext()) {
                writer.write(",");
                writeObjectHelper(iterator, writer, indent);
            }
        }
        writer.write("\n");
        writeIndent(writer, indent);
        writer.write("}");
    }

    /**
     * Helper method for the writeObject method
     *
     * @param iterator use to iterate
     * @param writer   to write
     * @param indent   indentation levels
     * @throws IOException if an IO error occurs
     */
    public static void writeObjectHelper(Iterator<Entry<String, Integer>> iterator, Writer writer, int indent)
            throws IOException {
        var entry = iterator.next();
        writer.write("\n");
        writeQuote(entry.getKey(), writer, indent + 1);
        writer.write(": " + entry.getValue());
    }

    /**
     * Indents and then writes the text element surrounded by {@code " "} quotation
     * marks.
     *
     * @param element the element to write
     * @param writer  the writer to use
     * @param indent  the number of times to indent
     * @throws IOException if an IO error occurs
     */
    public static void writeQuote(String element, Writer writer, int indent) throws IOException {
        writeIndent(writer, indent);
        writer.write('"');
        writer.write(element);
        writer.write('"');
    }

    /**
     * Creates writer and writes search data for available words
     *
     * @param storeSearchData map containing the word and a list of single search
     *                        results
     * @param path            File path to written
     * @throws IOException if an IO error occurs
     */
    public static void writeSearch(Map<String, List<SingleSearchResult>> storeSearchData, Path path)
            throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
            writeSearch(storeSearchData, writer);
        }

    }

    /**
     * Writes search data for available words
     *
     * @param storeSearchData map containing the word and a list of single search
     *                        results
     * @param writer          to write
     * @throws IOException if an IO error occurs
     */
    public static void writeSearch(Map<String, List<SingleSearchResult>> storeSearchData, Writer writer)
            throws IOException {
        var uniqueStems = storeSearchData.keySet().iterator();
        writer.write("{\n");

        if (uniqueStems.hasNext()) {

            writeSearchHelper(storeSearchData, uniqueStems, writer);
        }
        while (uniqueStems.hasNext()) {
            writeSearchHelper(storeSearchData, uniqueStems, writer);
        }

        writer.write("}");

    }

    /**
     * Helper method for writeSearch
     *
     * @param storeSearchData map containing the search results
     * @param iterator        to iterate
     * @param writer          to write
     * @throws IOException if an IO error occurs
     */
    private static void writeSearchHelper(Map<String, List<SingleSearchResult>> storeSearchData,
            Iterator<String> iterator, Writer writer) throws IOException {
        var temp = iterator.next();
        String word = temp.toString();
        writeQuote(word, writer, 1);
        writer.write(": ");
        writer.write("[");
        if (storeSearchData.get(word) != null) {
            multipleSearchResults(storeSearchData.get(word).listIterator(), writer);
        }
        if (iterator.hasNext()) {
            writer.write("\n");
            writeIndent("]", writer, 1);
            writer.write(",");
        } else {
            writer.write("\n");
            writeIndent("]", writer, 1);
        }
        writer.write("\n");

    }
}
