package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Query File Parser Interface
 *
 * @author alessandrobarrera
 *
 */
public interface QueryFileParserInterface {
    /**
     * Parse file method which parses the entire file
     *
     * @param file  to parse
     * @param exact keyword to do an exact search or not
     * @throws IOException if an IO error occurs
     */
    public default void parseFile(Path file, boolean exact) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                parseLine(line, exact);
            }
        }
    }

    /**
     * Parse line method to parse one line
     *
     * @param line  to parse
     * @param exact keyword to do an exact search or not
     * @throws IOException if an IO error occurs
     */
    public void parseLine(String line, boolean exact) throws IOException;

    /**
     * Write to JSON format
     *
     * @param output address to write
     * @throws IOException if an IO error occurs
     */
    public void writeJSON(Path output) throws IOException;

    @Override
    public String toString();

}
