package edu.usfca.cs272;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 *
 * @author Alessandro Daniel Barrera
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2022
 */
public class Driver {

    /**
     * Initializes the classes necessary based on the provided command-line
     * arguments. This includes (but is not limited to) how to build or search an
     * inverted index.
     *
     * @param args flag/value pairs used to start this program
     */
    public static void main(String[] args) {
        ArgumentParser argumentParser = new ArgumentParser(args);
        InvertedIndex invertedIndex = null;
        InvertedIndexBuilder indexBuilder = null;
        QueryFileParserInterface queryFileParser = null;
        Path path = null;
        String initialCrawl = null;
        boolean multithreading = argumentParser.hasFlag("-threads");
        boolean webcrawling = argumentParser.hasFlag("-html");
        int numOfUrlToCrawl = argumentParser.getInteger("-max", 1);
        int threads = argumentParser.getInteger("-threads", 5);
        WorkQueue workQueue = null;
        Crawler crawler = null;
        int PORT = argumentParser.getInteger("-server", 8080);
        Logger log = LogManager.getLogger();
        Server server = null;

        if (threads < 1) {
            threads = 5;
        }

        if (numOfUrlToCrawl < 1) {
            numOfUrlToCrawl = 1;
        }

        if (multithreading || webcrawling) {
            ThreadSafeInvertedIndex threadSafeInvertedIndex = new ThreadSafeInvertedIndex();
            invertedIndex = threadSafeInvertedIndex;
            workQueue = new WorkQueue(threads);
            queryFileParser = new ThreadSafeQueryFileParser(threadSafeInvertedIndex, workQueue);
            indexBuilder = new ThreadSafeInvertedIndexBuilder(threadSafeInvertedIndex, workQueue);

            numOfUrlToCrawl = argumentParser.getInteger("-max", 1);
            initialCrawl = argumentParser.getString("-html");

        } else {
            invertedIndex = new InvertedIndex();
            queryFileParser = new QueryFileParser(invertedIndex);
            indexBuilder = new InvertedIndexBuilder(invertedIndex);
        }

        if (argumentParser.hasFlag("-text")) {
            if (argumentParser.hasValue("-text")) {
                path = argumentParser.getPath("-text");
                try {
                    indexBuilder.build(path);
                } catch (IOException e) {
                    System.out.println(
                            "Unable to build the inverted index with multithreading for path: " + path.toString());
                    System.out.println(e.getCause());
                }

            } else {
                System.out.println("You are missing a value for the text flag");
            }
        } else {
            System.out.println("You are missing the text flag");
        }

        if (webcrawling) {
            try {
                crawler = new Crawler(invertedIndex, workQueue, numOfUrlToCrawl, initialCrawl);
                crawler.crawl();
            } catch (MalformedURLException | URISyntaxException e) {
                System.out.println("Unable to crawl to: " + initialCrawl);
                e.getCause();
            }
        }

        if (argumentParser.hasFlag("-server")) {
            server = new Server(PORT);
            ServletHandler handler = new ServletHandler();
            try {
                handler.addServletWithMapping(new ServletHolder(new SearchServlet(invertedIndex)), "/search");
                server.setHandler(handler);
                server.start();
                System.out.println("Server: Server starting on PORT:" + PORT);
                log.info("Server: {} with {} threads", server.getState(), server.getThreadPool().getThreads());
                server.join();
            } catch (Exception e) {
                System.out.println("Cannot start server on PORT: " + PORT);
                System.out.println(e.getCause());
            }
        }

        if (argumentParser.hasFlag("-query")) {
            if (argumentParser.hasValue("-query")) {
                path = argumentParser.getPath("-query");
                Boolean exact = argumentParser.hasFlag("-exact");
                try {
                    queryFileParser.parseFile(path, exact);
                } catch (Exception e) {
                    System.out.println("Unable  to read query file: " + path.toString());
                    System.out.println(e.getCause());
                }
            }
        }

        if (argumentParser.hasFlag("-results")) {
            Path writerPath = argumentParser.getPath("-results", Path.of("results.json"));
            try {
                queryFileParser.writeJSON(writerPath);
            } catch (Exception e) {
                System.out.println("Unable  to read query file: " + path.toString());
                System.out.println(e.getCause());
            }
        }

        if (argumentParser.hasFlag("-counts")) {
            Path writerPath = argumentParser.getPath("-counts", Path.of("counts.json"));
            try {
                SimpleJsonWriter.writeJSON(invertedIndex.getFileCount(), writerPath);
            } catch (IOException e) {
                System.out.println("Unable to write to : " + path.toString());
                System.out.println(e.getCause());
            }
        }

        if (argumentParser.hasFlag("-index")) {
            Path writerPath = argumentParser.getPath("-index", Path.of("index.json"));
            try {
                invertedIndex.toJSON(writerPath);
            } catch (Exception e) {
                System.out.println("Something went wrong writing to: " + writerPath.toString());
                System.out.println(e.getCause());
            }
        } else {
            System.out.println("You are missing the index flag");
        }

        if (multithreading) {
            workQueue.shutdown();
        }
    }
}