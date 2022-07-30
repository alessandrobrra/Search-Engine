package edu.usfca.cs272;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Web crawler class
 *
 * @author alessandrobarrera
 *
 */
public class Crawler {
    /**
     * Inverted index to work on
     */
    private final ThreadSafeInvertedIndex index;
    /**
     * Work Queue to manage multithreading
     */
    private final WorkQueue workQueue;
    /**
     * Number of urls to crawl
     */
    private final int numOfUrlsToCrawl;
    /**
     * Initial url to crawl
     */
    private final String initialCrawl;

    /**
     * Set of visited Urls
     */
    private final Set<URL> visitedUrls;

    /**
     * Initial Constructor
     *
     * @param invertedIndex    to work on
     * @param workQueue        to manage multithreading
     * @param numOfUrlsToCrawl number of urls to crawl
     * @param initialCrawl     initial url to crawl
     */
    public Crawler(InvertedIndex invertedIndex, WorkQueue workQueue, int numOfUrlsToCrawl, String initialCrawl) {
        this.index = (ThreadSafeInvertedIndex) invertedIndex;
        this.workQueue = workQueue;
        this.numOfUrlsToCrawl = numOfUrlsToCrawl;
        this.initialCrawl = initialCrawl;
        this.visitedUrls = new HashSet<>();
    }

    /**
     * Crawl method that adds the initial seed and then calls the workqueue for the
     * next ones
     *
     * @throws MalformedURLException if an malformed url exists
     * @throws URISyntaxException    if an url syntax error occurs
     */
    public void crawl() throws MalformedURLException, URISyntaxException {
        URL seed = new URL(initialCrawl);
        visitedUrls.add(seed);
        workQueue.execute(new Task(seed));
        workQueue.finish();
    }

    /**
     * Recursive crawler method
     *
     * @param base base url
     * @throws MalformedURLException if an malformed url exists
     * @throws URISyntaxException    if an url syntax error occurs
     */
    public void crawl(URL base) throws MalformedURLException, URISyntaxException {
        String html = HtmlFetcher.fetch(base, 3);
        if (html == null) {
            return;
        }
        html = HtmlCleaner.stripBlockElements(html);
        List<URL> listOfUrls = LinkParser.getValidLinks(base, html);
        synchronized (visitedUrls) {
            for (URL url : listOfUrls) {
                if (!visitedUrls.contains(url) && visitedUrls.size() < numOfUrlsToCrawl) {
                    visitedUrls.add(url);
                    workQueue.execute(new Task(url));
                }
            }
        }
        html = HtmlCleaner.stripHtml(html);
        int counter = 1;
        ThreadSafeInvertedIndex temp = new ThreadSafeInvertedIndex();
        for (String stem : TextFileStemmer.listStems(html)) {
            temp.add(stem, base.toString(), counter);
            counter++;
        }
        index.addAll(temp);
    }

    /**
     * Inner class called Task implementing Runnable
     *
     * @author alessandrobarrera
     *
     */
    public class Task implements Runnable {
        /**
         * Link url
         */
        private final URL link;

        /**
         * Constructor that takes in a link
         *
         * @param link to assign
         */
        public Task(URL link) {
            this.link = link;
        }

        @Override
        public void run() {
            try {
                crawl(link);
            } catch (MalformedURLException | URISyntaxException e) {
                e.getCause();
            }
        }
    }
}
