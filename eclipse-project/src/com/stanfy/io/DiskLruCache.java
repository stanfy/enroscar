/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stanfy.io;

import static com.stanfy.io.IoUtils.*;

import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;

/**
 * A cache that uses a bounded amount of space on a filesystem. Each cache
 * entry has a string key and a fixed number of values. Values are byte
 * sequences, accessible as streams or files. Each value must be between {@code
 * 0} and {@code Integer.MAX_VALUE} bytes in length.
 *
 * <p>The cache stores its data in a directory on the filesystem. This
 * directory must be exclusive to the cache; the cache may delete or overwrite
 * files from its directory. It is an error for multiple processes to use the
 * same cache directory at the same time.
 *
 * <p>This cache limits the number of bytes that it will store on the
 * filesystem. When the number of stored bytes exceeds the limit, the cache will
 * remove entries in the background until the limit is satisfied. The limit is
 * not strict: the cache may temporarily exceed it while waiting for files to be
 * deleted. The limit does not include filesystem overhead or the cache
 * journal so space-sensitive applications should set a conservative limit.
 *
 * <p>Clients call {@link #edit} to create or update the values of an entry. An
 * entry may have only one editor at one time; if a value is not available to be
 * edited then {@link #edit} will return null.
 * <ul>
 *     <li>When an entry is being <strong>created</strong> it is necessary to
 *         supply a full set of values; the empty value should be used as a
 *         placeholder if necessary.
 *     <li>When an entry is being <strong>created</strong>, it is not necessary
 *         to supply data for every value; values default to their previous
 *         value.
 * </ul>
 * Every {@link #edit} call must be matched by a call to {@link Editor#commit}
 * or {@link Editor#abort}. Committing is atomic: a read observes the full set
 * of values as they were before or after the commit, but never a mix of values.
 *
 * <p>Clients call {@link #get} to read a snapshot of an entry. The read will
 * observe the value at the time that {@link #get} was called. Updates and
 * removals after the call do not impact ongoing reads.
 *
 * <p>This class is tolerant of some I/O errors. If files are missing from the
 * filesystem, the corresponding entries will be dropped from the cache. If
 * an error occurs while writing a cache value, the edit will fail silently.
 * Callers should handle other problems by catching {@code IOException} and
 * responding appropriately.
 */
public final class DiskLruCache implements Closeable {
  /** Logging tag. */
  private static final String TAG = "DiskLruCache";
  /** Journal file name. */
  static final String JOURNAL_FILE = "journal";
  /** Temporary journal file name. */
  static final String JOURNAL_FILE_TMP = "journal.tmp";
  /** Magic string. */
  static final String MAGIC = "libcore.io.DiskLruCache";
  /** Version string. */
  static final String VERSION_1 = "1";
  /** Operation name. */
  private static final String CLEAN = "CLEAN",
                              DIRTY = "DIRTY",
                              REMOVE = "REMOVE",
                              READ = "READ";

  /** Journal rebuild threshold (see {@link #journalRebuildRequired()}). */
  private static final int REDUNDANT_OP_COMPACT_THRESHOLD = 2000;

  /*
   * This cache uses a journal file named "journal". A typical journal file
   * looks like this:
   *     libcore.io.DiskLruCache
   *     1
   *     100
   *     2
   *
   *     CLEAN 3400330d1dfc7f3f7f4b8d4d803dfcf6 832 21054
   *     DIRTY 335c4c6028171cfddfbaae1a9c313c52
   *     CLEAN 335c4c6028171cfddfbaae1a9c313c52 3934 2342
   *     REMOVE 335c4c6028171cfddfbaae1a9c313c52
   *     DIRTY 1ab96a171faeeee38496d8b330771a7a
   *     CLEAN 1ab96a171faeeee38496d8b330771a7a 1600 234
   *     READ 335c4c6028171cfddfbaae1a9c313c52
   *     READ 3400330d1dfc7f3f7f4b8d4d803dfcf6
   *
   * The first five lines of the journal form its header. They are the
   * constant string "libcore.io.DiskLruCache", the disk cache's version,
   * the application's version, the value count, and a blank line.
   *
   * Each of the subsequent lines in the file is a record of the state of a
   * cache entry. Each line contains space-separated values: a state, a key,
   * and optional state-specific values.
   *   o DIRTY lines track that an entry is actively being created or updated.
   *     Every successful DIRTY action should be followed by a CLEAN or REMOVE
   *     action. DIRTY lines without a matching CLEAN or REMOVE indicate that
   *     temporary files may need to be deleted.
   *   o CLEAN lines track a cache entry that has been successfully published
   *     and may be read. A publish line is followed by the lengths of each of
   *     its values.
   *   o READ lines track accesses for LRU.
   *   o REMOVE lines track entries that have been deleted.
   *
   * The journal file is appended to as cache operations occur. The journal may
   * occasionally be compacted by dropping redundant lines. A temporary file named
   * "journal.tmp" will be used during compaction; that file should be deleted if
   * it exists when the cache is opened.
   */

  /** Working directory. */
  private final File directory;
  /** Journal file. */
  private final File journalFile;
  /** Journal temporary file. */
  private final File journalFileTmp;
  /** Application version. */
  private final int appVersion;
  /** Max cache size. */
  private final long maxSize;
  /** Number of values per cache entry. */
  private final int valueCount;
  /** Current size. */
  private long size = 0;
  /** Journal writer. */
  private Writer journalWriter;
  /** LRU entries. */
  private final LinkedHashMap<String, Entry> lruEntries = new LinkedHashMap<String, Entry>(0, 0.75f, true);
  /** Count of redundant operations. */
  private int redundantOpCount;

  /** Buffers pool. */
  private final BuffersPool buffersPool;

  /** This cache uses a single background thread to evict entries. */
  private final ExecutorService executorService = new ThreadPoolExecutor(0, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
  /** Cleanup worker. */
  private final Callable<Void> cleanupCallable = new Callable<Void>() {
    @Override public Void call() throws Exception {
      synchronized (DiskLruCache.this) {
        if (journalWriter == null) {
          return null; // closed
        }
        trimToSize();
        if (journalRebuildRequired()) {
          rebuildJournal();
          redundantOpCount = 0;
        }
      }
      return null;
    }
  };

  private DiskLruCache(final File directory, final BuffersPool buffersPool, final int appVersion, final int valueCount, final long maxSize) {
    this.directory = directory;
    this.appVersion = appVersion;
    this.journalFile = new File(directory, JOURNAL_FILE);
    this.journalFileTmp = new File(directory, JOURNAL_FILE_TMP);
    this.valueCount = valueCount;
    this.maxSize = maxSize;
    this.buffersPool = buffersPool;
  }

  /**
   * Opens the cache in {@code directory}, creating a cache if none exists
   * there.
   *
   * @param directory a writable directory
   * @param buffersPool buffers pool instance
   * @param appVersion
   * @param valueCount the number of values per cache entry. Must be positive.
   * @param maxSize the maximum number of bytes this cache should use to store
   * @param asyncInit whether use another thread for initialization
   * @throws IOException if reading or writing the cache directory fails
   */
  public static DiskLruCache open(final File directory, final BuffersPool buffersPool, final int appVersion, final int valueCount, final long maxSize, final boolean asyncInit)
      throws IOException {
    if (maxSize <= 0) {
      throw new IllegalArgumentException("maxSize <= 0");
    }
    if (valueCount <= 0) {
      throw new IllegalArgumentException("valueCount <= 0");
    }

    // prefer to pick up where we left off
    final DiskLruCache cache = new DiskLruCache(directory, buffersPool, appVersion, valueCount, maxSize);

    if (asyncInit) {

      if (Looper.myLooper() != null) {
        // use async task
        new AsyncTask<Void, Void, Void>() {
          @Override
          protected Void doInBackground(final Void... params) {
            cache.asyncInit();
            return null;
          }
        }
        .execute();
      } else {
        // start new thread
        new Thread() {
          @Override
          public void run() {
            cache.asyncInit();
          }
        }
        .start();
      }

    } else {
      cache.initCache();
    }

    return cache;
  }

  private void asyncInit() {
    try {
      initCache();
    } catch (final IOException e) {
      journalWriter = null;
      Log.e(TAG, "Could not initialize cache asynchrnously dir=" + directory, e);
    }
  }

  private synchronized void initCache() throws IOException {
    if (journalFile.exists()) {
      try {
        readJournal();
        processJournal();
        this.journalWriter = new OutputStreamWriter(
            new PoolableBufferedOutputStream(new FileOutputStream(journalFile, true), buffersPool), IoUtils.US_ASCII
        );
        return;
      } catch (final IOException journalIsCorrupt) {
        Log.w(TAG, directory + " is corrupt: " + journalIsCorrupt.getMessage() + ", removing", journalIsCorrupt);
        // cleanup
        delete();
        lruEntries.clear();
      }
    }

    // create a new empty cache
    directory.mkdirs();
    rebuildJournal();
  }

  private void readJournal() throws IOException {
    final InputStream in = new PoolableBufferedInputStream(new FileInputStream(journalFile), buffersPool);
    try {
      final String magic = readAsciiLine(in);
      final String version = readAsciiLine(in);
      final String appVersionString = readAsciiLine(in);
      final String valueCountString = readAsciiLine(in);
      final String blank = readAsciiLine(in);
      if (!MAGIC.equals(magic)
          || !VERSION_1.equals(version)
          || !Integer.toString(appVersion).equals(appVersionString)
          || !Integer.toString(valueCount).equals(valueCountString)
          || !"".equals(blank)) {
        throw new IOException("unexpected journal header: ["
            + magic + ", " + version + ", " + valueCountString + ", " + blank + "]");
      }

      while (true) {
        try {
          readJournalLine(readAsciiLine(in));
        } catch (final EOFException endOfJournal) {
          break;
        }
      }
    } finally {
      closeQuietly(in);
    }
  }

  private void readJournalLine(final String line) throws IOException {
    final String[] parts = line.split(" ");
    if (parts.length < 2) {
      throw new IOException("unexpected journal line: " + line);
    }

    final String key = parts[1];
    if (parts[0].equals(REMOVE) && parts.length == 2) {
      lruEntries.remove(key);
      return;
    }

    Entry entry = lruEntries.get(key);
    if (entry == null) {
      entry = new Entry(key);
      lruEntries.put(key, entry);
    }

    if (parts[0].equals(CLEAN) && parts.length == 2 + valueCount) {
      entry.readable = true;
      entry.currentEditor = null;
      entry.setLengths(copyOfRange(parts, 2, parts.length));
    } else if (parts[0].equals(DIRTY) && parts.length == 2) {
      entry.currentEditor = new Editor(entry);
    /*} else if (parts[0].equals(READ) && parts.length == 2) {
      // this work was already done by calling lruEntries.get()
    } else {
      throw new IOException("unexpected journal line: " + line);
    }*/
    } else if (!(parts[0].equals(READ) && parts.length == 2)) { // this work has been already done by calling lruEntries.get()
      throw new IOException("unexpected journal line: " + line);
    }
  }

  /**
   * Computes the initial size and collects garbage as a part of opening the
   * cache. Dirty entries are assumed to be inconsistent and will be deleted.
   */
  private void processJournal() throws IOException {
    deleteIfExists(journalFileTmp);
    for (final Iterator<Entry> i = lruEntries.values().iterator(); i.hasNext();) {
      final Entry entry = i.next();
      if (entry.currentEditor == null) {
        for (int t = 0; t < valueCount; t++) {
          size += entry.lengths[t];
        }
      } else {
        entry.currentEditor = null;
        for (int t = 0; t < valueCount; t++) {
          deleteIfExists(entry.getCleanFile(t));
          deleteIfExists(entry.getDirtyFile(t));
        }
        i.remove();
      }
    }
  }

  private Writer asciiWriter(final File file, final boolean append) throws IOException {
    return new OutputStreamWriter(
        new PoolableBufferedOutputStream(new FileOutputStream(file, append), buffersPool), IoUtils.US_ASCII
    );
  }

  /**
   * Creates a new journal that omits redundant information. This replaces the
   * current journal if it exists.
   */
  private synchronized void rebuildJournal() throws IOException {
    if (journalWriter != null) {
      journalWriter.close();
    }

    final Writer writer = asciiWriter(journalFileTmp, false);
    writer.write(MAGIC);
    writer.write("\n");
    writer.write(VERSION_1);
    writer.write("\n");
    writer.write(Integer.toString(appVersion));
    writer.write("\n");
    writer.write(Integer.toString(valueCount));
    writer.write("\n");
    writer.write("\n");

    for (final Entry entry : lruEntries.values()) {
      if (entry.currentEditor != null) {
        writer.write(DIRTY + ' ' + entry.key + '\n');
      } else {
        writer.write(CLEAN + ' ' + entry.key + entry.getLengths() + '\n');
      }
    }

    writer.close();
    journalFileTmp.renameTo(journalFile);
    journalWriter = asciiWriter(journalFile, true);
  }

  private static void deleteIfExists(final File file) throws IOException {
//    try {
//      Libcore.os.remove(file.getPath());
//    } catch (final ErrnoException errnoException) {
//      if (errnoException.errno != OsConstants.ENOENT) {
//        throw errnoException.rethrowAsIOException();
//      }
//    }
    if (file.exists() && !file.delete()) {
      throw new IOException("Cannot delete file " + file);
    }
  }

  /**
   * Returns a snapshot of the entry named {@code key}, or null if it doesn't
   * exist is not currently readable. If a value is returned, it is moved to
   * the head of the LRU queue.
   */
  public synchronized Snapshot get(final String key) throws IOException {
    checkNotClosed();
    validateKey(key);
    final Entry entry = lruEntries.get(key);
    if (entry == null) {
      return null;
    }

    if (!entry.readable) {
      return null;
    }

    /*
     * Open all streams eagerly to guarantee that we see a single published
     * snapshot. If we opened streams lazily then the streams could come
     * from different edits.
     */
    final InputStream[] ins = new InputStream[valueCount];
    final String[] paths = new String[valueCount];
    try {
      for (int i = 0; i < valueCount; i++) {
        final File file = entry.getCleanFile(i);
        ins[i] = new FileInputStream(file);
        paths[i] = file.getAbsolutePath();
      }
    } catch (final FileNotFoundException e) {
      // a file must have been deleted manually!
      return null;
    }

    redundantOpCount++;
    journalWriter.append(READ + ' ' + key + '\n');
    if (journalRebuildRequired()) {
      executorService.submit(cleanupCallable);
    }

    return new Snapshot(ins, paths);
  }

  public String getLocalPath(final String key, final int index) {
    return new Entry(key).getCleanFile(index).getAbsolutePath();
  }

  /**
   * Returns an editor for the entry named {@code key}, or null if it cannot
   * currently be edited.
   */
  public synchronized Editor edit(final String key) throws IOException {
    checkNotClosed();
    validateKey(key);
    Entry entry = lruEntries.get(key);
    if (entry == null) {
      entry = new Entry(key);
      lruEntries.put(key, entry);
    } else if (entry.currentEditor != null) {
      return null;
    }

    final Editor editor = new Editor(entry);
    entry.currentEditor = editor;

    // flush the journal before creating files to prevent file leaks
    journalWriter.write(DIRTY + ' ' + key + '\n');
    journalWriter.flush();
    return editor;
  }

  /**
   * Returns the directory where this cache stores its data.
   */
  public File getDirectory() {
    return directory;
  }

  /**
   * Returns the maximum number of bytes that this cache should use to store
   * its data.
   */
  public long maxSize() {
    return maxSize;
  }

  /**
   * Returns the number of bytes currently being used to store the values in
   * this cache. This may be greater than the max size if a background
   * deletion is pending.
   */
  public synchronized long size() {
    return size;
  }

  private synchronized void completeEdit(final Editor editor, final boolean success) throws IOException {
    final Entry entry = editor.entry;
    if (entry.currentEditor != editor) {
      throw new IllegalStateException();
    }

    // if this edit is creating the entry for the first time, every index must have a value
    if (success && !entry.readable) {
      for (int i = 0; i < valueCount; i++) {
        if (!entry.getDirtyFile(i).exists()) {
          editor.abort();
          throw new IllegalStateException("edit didn't create file " + i);
        }
      }
    }

    for (int i = 0; i < valueCount; i++) {
      final File dirty = entry.getDirtyFile(i);
      if (success) {
        if (dirty.exists()) {
          final File clean = entry.getCleanFile(i);
          dirty.renameTo(clean);
          final long oldLength = entry.lengths[i];
          final long newLength = clean.length();
          entry.lengths[i] = newLength;
          size = size - oldLength + newLength;
        }
      } else {
        deleteIfExists(dirty);
      }
    }

    redundantOpCount++;
    entry.currentEditor = null;
    if (entry.readable | success) {
      entry.readable = true;
      journalWriter.write(CLEAN + ' ' + entry.key + entry.getLengths() + '\n');
    } else {
      lruEntries.remove(entry.key);
      journalWriter.write(REMOVE + ' ' + entry.key + '\n');
    }

    if (size > maxSize || journalRebuildRequired()) {
      executorService.submit(cleanupCallable);
    }
  }

  /**
   * We only rebuild the journal when it will halve the size of the journal
   * and eliminate at least 2000 ops.
   */
  private boolean journalRebuildRequired() {
    return redundantOpCount >= REDUNDANT_OP_COMPACT_THRESHOLD
        && redundantOpCount >= lruEntries.size();
  }

  /**
   * Drops the entry for {@code key} if it exists and can be removed. Entries
   * actively being edited cannot be removed.
   *
   * @return true if an entry was removed.
   */
  public synchronized boolean remove(final String key) throws IOException {
    checkNotClosed();
    validateKey(key);
    final Entry entry = lruEntries.get(key);
    if (entry == null || entry.currentEditor != null) {
      return false;
    }

    for (int i = 0; i < valueCount; i++) {
      final File file = entry.getCleanFile(i);
      if (!file.delete()) {
        throw new IOException("failed to delete " + file);
      }
      size -= entry.lengths[i];
      entry.lengths[i] = 0;
    }

    redundantOpCount++;
    journalWriter.append(REMOVE + ' ' + key + '\n');
    lruEntries.remove(key);

    if (journalRebuildRequired()) {
      executorService.submit(cleanupCallable);
    }

    return true;
  }

  /**
   * Returns true if this cache has been closed.
   */
  public boolean isClosed() {
    return journalWriter == null;
  }

  private void checkNotClosed() {
    if (journalWriter == null) {
      throw new IllegalStateException("cache is closed");
    }
  }

  /**
   * Force buffered operations to the filesystem.
   */
  public synchronized void flush() throws IOException {
    checkNotClosed();
    trimToSize();
    journalWriter.flush();
  }

  /**
   * Closes this cache. Stored values will remain on the filesystem.
   */
  @Override
  public synchronized void close() throws IOException {
    if (journalWriter == null) {
      return; // already closed
    }
    for (final Entry entry : new ArrayList<Entry>(lruEntries.values())) {
      if (entry.currentEditor != null) {
        entry.currentEditor.abort();
      }
    }
    trimToSize();
    journalWriter.close();
    journalWriter = null;
  }

  private void trimToSize() throws IOException {
    while (size > maxSize) {
      final Map.Entry<String, Entry> toEvict = lruEntries.entrySet().iterator().next(); //lruEntries.eldest();
      remove(toEvict.getKey());
    }
  }

  /**
   * Closes the cache and deletes all of its stored values. This will delete
   * all files in the cache directory including files that weren't created by
   * the cache.
   */
  public void delete() throws IOException {
    close();
    try {
      deleteContents(directory);
    } catch (final IllegalArgumentException e) {
      Log.w(TAG, "Cannot delete contents of " + directory, e);
    }
  }

  private void validateKey(final String key) {
    if (key.contains(" ") || key.contains("\n") || key.contains("\r")) {
      throw new IllegalArgumentException(
          "keys must not contain spaces or newlines: \"" + key + "\"");
    }
  }

  /**
   * A snapshot of the values for an entry.
   */
  public static final class Snapshot implements Closeable {
    /** Input. */
    private final InputStream[] ins;
    /** Local path. */
    private final String[] localPaths;

    Snapshot(final InputStream[] ins, final String[] localPaths) {
      this.ins = ins;
      this.localPaths = localPaths;
    }

    /**
     * Returns the unbuffered stream with the value for {@code index}.
     */
    public InputStream getInputStream(final int index) {
      return ins[index];
    }

    /**
     * Returns the string value for {@code index}.
     */
    public String getString(final int index) throws IOException {
      return streamToString(getInputStream(index));
    }

    @Override public void close() {
      for (final InputStream in : ins) {
        closeQuietly(in);
      }
    }

    /** @return local path to the cached content */
    public String getLocalPath(final int index) { return localPaths[index]; }
  }

  /**
   * Edits the values for an entry.
   */
  public final class Editor {
    /** Editor entry. */
    private final Entry entry;
    /** Error flag (set by {@link FaultHidingOutputStream}). */
    private boolean hasErrors;

    private Editor(final Entry entry) {
      this.entry = entry;
    }

    /**
     * Returns an unbuffered input stream to read the last committed value,
     * or null if no value has been committed.
     */
    public InputStream newInputStream(final int index) throws IOException {
      synchronized (DiskLruCache.this) {
        if (entry.currentEditor != this) {
          throw new IllegalStateException();
        }
        if (!entry.readable) {
          return null;
        }
        return new FileInputStream(entry.getCleanFile(index));
      }
    }

    /**
     * Returns the last committed value as a string, or null if no value
     * has been committed.
     */
    public String getString(final int index) throws IOException {
      final InputStream in = newInputStream(index);
      return in != null ? streamToString(in) : null;
    }

    /**
     * Returns a new unbuffered output stream to write the value at
     * {@code index}. If the underlying output stream encounters errors
     * when writing to the filesystem, this edit will be aborted when
     * {@link #commit} is called. The returned output stream does not throw
     * IOExceptions.
     */
    public OutputStream newOutputStream(final int index) throws IOException {
      synchronized (DiskLruCache.this) {
        if (entry.currentEditor != this) {
          throw new IllegalStateException();
        }
        return new FaultHidingOutputStream(new FileOutputStream(entry.getDirtyFile(index)));
      }
    }

    /**
     * Sets the value at {@code index} to {@code value}.
     */
    public void set(final int index, final String value) throws IOException {
      Writer writer = null;
      try {
        writer = new OutputStreamWriter(newOutputStream(index), IoUtils.UTF_8);
        writer.write(value);
      } finally {
        closeQuietly(writer);
      }
    }

    /**
     * Commits this edit so it is visible to readers.  This releases the
     * edit lock so another edit may be started on the same key.
     */
    public void commit() throws IOException {
      if (hasErrors) {
        completeEdit(this, false);
        remove(entry.key); // the previous entry is stale
      } else {
        completeEdit(this, true);
      }
    }

    /**
     * Aborts this edit. This releases the edit lock so another edit may be
     * started on the same key.
     */
    public void abort() throws IOException {
      completeEdit(this, false);
    }

    /**
     * Output streams that sets a flag about I/O errors.
     */
    private final class FaultHidingOutputStream extends FilterOutputStream {

      FaultHidingOutputStream(final OutputStream out) {
        super(out);
      }

      @Override public void write(final int oneByte) {
        try {
          out.write(oneByte);
        } catch (final IOException e) {
          hasErrors = true;
        }
      }

      @Override public void write(final byte[] buffer, final int offset, final int length) {
        try {
          out.write(buffer, offset, length);
        } catch (final IOException e) {
          hasErrors = true;
        }
      }

      @Override public void close() {
        try {
          out.close();
        } catch (final IOException e) {
          hasErrors = true;
        }
      }

      @Override public void flush() {
        try {
          out.flush();
        } catch (final IOException e) {
          hasErrors = true;
        }
      }
    }
  }

  /** Editor entry. */
  private final class Entry {
    /** Entry key. */
    private final String key;

    /** Lengths of this entry's files. */
    private final long[] lengths;

    /** True if this entry has ever been published. */
    private boolean readable;

    /** The ongoing edit or null if this entry is not being edited. */
    private Editor currentEditor;

    private Entry(final String key) {
      this.key = key;
      this.lengths = new long[valueCount];
    }

    public String getLengths() throws IOException {
      final StringBuilder result = new StringBuilder();
      for (final long size : lengths) {
        result.append(' ').append(size);
      }
      return result.toString();
    }

    /**
     * Set lengths using decimal numbers like "10123".
     */
    private void setLengths(final String[] strings) throws IOException {
      if (strings.length != valueCount) {
        throw invalidLengths(strings);
      }

      try {
        for (int i = 0; i < strings.length; i++) {
          lengths[i] = Long.parseLong(strings[i]);
        }
      } catch (final NumberFormatException e) {
        throw invalidLengths(strings);
      }
    }

    private IOException invalidLengths(final String[] strings) throws IOException {
      throw new IOException("unexpected journal line: " + Arrays.toString(strings));
    }

    public File getCleanFile(final int i) {
      final int addLen = 10;
      return new File(directory, new StringBuilder(key.length() + addLen).append(key).append(".").append(i).toString());
    }

    public File getDirtyFile(final int i) {
      final int addLen = 14;
      return new File(directory, new StringBuilder(key.length() + addLen).append(key).append(".").append(i).append(".tmp").toString());
    }
  }
}
