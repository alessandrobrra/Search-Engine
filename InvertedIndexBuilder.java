package edu.usfca.cs272;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 *
 * Inverted Index Data Structure Builder Class
 */
public class InvertedIndexBuilder {

    /**
     * An Inverted Index
     *
     */
    protected final InvertedIndex index;

    /**
     * Constructor that passes in an inverted index
     *
     * @param index to build
     */
    public InvertedIndexBuilder(InvertedIndex index) {
        this.index = index;
    }

    /**
     * Builder class to initialize the inverted index
     *
     *
     * @param path of the text files
     * @throws IOException if an IO error occurs
     */
    public void build(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            traverseDirectory(path);
        } else {
            indexWriter(path);
        }
    }

    /**
     * Inverted Index functions that calls the overloaded method
     *
     * @param file the path of the file
     * @throws IOException if an IO error occurs
     */
    public void indexWriter(Path file) throws IOException {
        indexWriter(file, index);
    }

    /**
     * Inverted Index Writer
     *
     * @param file  the path of the file
     * @param index the InvertedIndex object containing the map
     * @throws IOException if an IO error occurs
     *
     */
    public static void indexWriter(Path file, InvertedIndex index) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);) {
            Stemmer stemmer = new SnowballStemmer(ENGLISH);
            String line = null;
            String location = file.toString();

            int count = 0;
            while ((line = reader.readLine()) != null) {
                for (String word : TextParser.parse(line)) {
                    index.add(stemmer.stem(word).toString(), location, count + 1);
                    count++;
                }
            }
        }
    }

    /**
     * Function to check if it is a text file
     *
     * @param path of the file
     * @return boolean whether it is a text file or not
     */
    public static boolean isTextFile(Path path) {
        String lower = path.toString().toLowerCase();
        return lower.endsWith(".txt") || lower.endsWith(".text");
    }

    /**
     * Recursive function to traverse through directories until it is a text file
     *
     * @param directory of the files where we traverse through
     * @throws IOException if an IO error occurs
     */
    public void traverseDirectory(Path directory) throws IOException {
        try (DirectoryStream<Path> listing = Files.newDirectoryStream(directory)) {
            for (Path path : listing) {
                if (Files.isDirectory(path)) {
                    traverseDirectory(path);
                } else if (isTextFile(path)) {
                    indexWriter(path);
                }
            }
        }
    }

}
