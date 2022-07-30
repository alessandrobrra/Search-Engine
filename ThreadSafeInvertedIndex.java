package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * Inverted Index Data Structure Class
 *
 */
public class ThreadSafeInvertedIndex extends InvertedIndex {
    /**
     * The lock used to protect concurrent access to the underlying set.
     */
    private final SimpleReadWriteLock lock;

    /**
     * Initialized a thread-safe Inverted Index
     */
    public ThreadSafeInvertedIndex() {
        this.lock = new SimpleReadWriteLock();
    }

    @Override
    public void add(String word, String location, int position) {
        lock.writeLock().lock();
        try {
            super.add(word, location, position);
        } finally {
            lock.writeLock().unlock();
        }

    }

    @Override
    public void addAll(List<String> words, String location) {
        lock.writeLock().lock();
        try {
            super.addAll(words, location);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void addAll(InvertedIndex local) {
        lock.writeLock().lock();
        try {
            super.addAll(local);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean contains(String word) {
        lock.readLock().lock();
        try {
            return super.contains(word);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean contains(String word, String location) {
        lock.readLock().lock();
        try {
            return super.contains(word, location);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean contains(String word, String location, Integer index) {
        lock.readLock().lock();
        try {
            return super.contains(word, location, index);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<SingleSearchResult> exactSearch(Set<String> parsedWords) {
        lock.readLock().lock();
        try {
            return super.exactSearch(parsedWords);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<SingleSearchResult> partialSearch(Set<String> parsedWords) {
        lock.readLock().lock();
        try {
            return super.partialSearch(parsedWords);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Collection<String> get() {
        lock.readLock().lock();
        try {
            return super.get();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Collection<String> get(String word) {
        lock.readLock().lock();
        try {
            return super.get(word);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Collection<Integer> get(String word, String location) {
        lock.readLock().lock();
        try {
            return super.get(word, location);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Map<String, Integer> getFileCount() {
        lock.readLock().lock();
        try {
            return super.getFileCount();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int size() {
        lock.readLock().lock();
        try {
            return super.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int size(String word) {
        lock.readLock().lock();
        try {
            return super.size(word);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int size(String word, String location) {
        lock.readLock().lock();
        try {
            return super.size(word, location);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public String toString() {
        lock.readLock().lock();
        try {
            return super.toString();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void toJSON(Path writer) throws IOException {
        lock.readLock().lock();
        try {
            super.toJSON(writer);
        } finally {
            lock.readLock().unlock();
        }
    }
}
