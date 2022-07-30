package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;

/**
 *
 * Inverted Index Data Structure Builder Class
 */
public class ThreadSafeInvertedIndexBuilder extends InvertedIndexBuilder {
    /**
     * Work Queue
     */
    private final WorkQueue workQueue;

    /**
     * Constructor passed in with the number of threads to multithread
     *
     * @param index     to use
     * @param workQueue used to manage the threads
     */
    public ThreadSafeInvertedIndexBuilder(ThreadSafeInvertedIndex index, WorkQueue workQueue) {
        super(index);
        this.workQueue = workQueue;
    }

    /**
     * Builder class to initialize the inverted index
     *
     * @param path of the text files
     * @throws IOException if an IO error occurs
     */
    @Override
    public void build(Path path) throws IOException {
//        if (Files.isDirectory(path)) {
//            traverseDirectory(path);
//        } else {
//            workQueue.execute(new Task(path));
//        }

        super.build(path);
        workQueue.finish();

    }

    @Override
    public void indexWriter(Path file) throws IOException {
        workQueue.execute(new Task(file));
    }

    /**
     * Inner class called Task implementing Runnable
     *
     * @author alessandrobarrera
     *
     */
    public class Task implements Runnable {
        /**
         * File to parse
         */
        private final Path file;

        /**
         * Task Constructor
         *
         * @param file to Parse
         */
        public Task(Path file) {
            this.file = file;
        }

        @Override
        public void run() {
            InvertedIndex local = new InvertedIndex();
            try {
                InvertedIndexBuilder.indexWriter(file, local);
            } catch (IOException e) {
                e.getCause();
            }
            index.addAll(local);
        }
    }
}