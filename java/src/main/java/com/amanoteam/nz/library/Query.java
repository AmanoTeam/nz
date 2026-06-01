package com.amanoteam.nz.library;

public class Query implements AutoCloseable {

    private long nativePtr;

    public Query() {
        this('&', "=");
    }

    public Query(final char sep, final String subsep) {
        this.nativePtr = LibQuery.queryInit(sep, subsep);
    }

    private void ensureNotFreed() {
        if (nativePtr == 0) {
            throw new IllegalStateException("Query has been closed");
        }
    }

    public Query add(final String key, final String value) {
        ensureNotFreed();
        final int status = LibQuery.queryAddString(nativePtr, key, value);
        if (status != 0) {
            throw new RuntimeException("Failed to add string parameter: " + key);
        }
        return this;
    }

    public Query add(final String key, final long value) {
        ensureNotFreed();
        final int status = LibQuery.queryAddInt(nativePtr, key, value);
        if (status != 0) {
            throw new RuntimeException("Failed to add int parameter: " + key);
        }
        return this;
    }

    public Query addUint(final String key, final long value) {
        ensureNotFreed();
        final int status = LibQuery.queryAddUint(nativePtr, key, value);
        if (status != 0) {
            throw new RuntimeException("Failed to add uint parameter: " + key);
        }
        return this;
    }

    public Query add(final String key, final double value) {
        ensureNotFreed();
        final int status = LibQuery.queryAddFloat(nativePtr, key, value);
        if (status != 0) {
            throw new RuntimeException("Failed to add float parameter: " + key);
        }
        return this;
    }

    public String getString(final String key) {
        ensureNotFreed();
        return LibQuery.queryGetString(nativePtr, key);
    }

    public long getInt(final String key) {
        ensureNotFreed();
        return LibQuery.queryGetInt(nativePtr, key);
    }

    public long getUint(final String key) {
        ensureNotFreed();
        return LibQuery.queryGetUint(nativePtr, key);
    }

    public double getFloat(final String key) {
        ensureNotFreed();
        return LibQuery.queryGetFloat(nativePtr, key);
    }

    public Boolean getBool(final String key) {
        ensureNotFreed();
        final int result = LibQuery.queryGetBool(nativePtr, key);
        if (result == -1) {
            return null;
        }
        return result == 1;
    }

    public QueryParam getItem(final long index) {
        ensureNotFreed();
        final long paramPtr = LibQuery.queryGetItem(nativePtr, index);
        if (paramPtr == 0) {
            return null;
        }
        return new QueryParam(paramPtr);
    }

    public void loadString(final String string) {
        ensureNotFreed();
        final int status = LibQuery.queryLoadString(nativePtr, string);
        if (status != 0) {
            throw new RuntimeException("Failed to load query string");
        }
    }

    public void loadFile(final String filename) {
        ensureNotFreed();
        final int status = LibQuery.queryLoadFile(nativePtr, filename);
        if (status != 0) {
            throw new RuntimeException("Failed to load query file: " + filename);
        }
    }

    public void loadEnviron() {
        ensureNotFreed();
        final int status = LibQuery.queryLoadEnviron(nativePtr);
        if (status != 0) {
            throw new RuntimeException("Failed to load environment variables");
        }
    }

    public String dumpString() {
        ensureNotFreed();
        final int size = LibQuery.queryDumpString(nativePtr, null);
        if (size < 0) {
            throw new RuntimeException("Failed to dump query string");
        }
        final byte[] buffer = new byte[size];
        final int len = LibQuery.queryDumpString(nativePtr, buffer);
        if (len < 0) {
            throw new RuntimeException("Failed to dump query string");
        }
        return new String(buffer, 0, len, java.nio.charset.StandardCharsets.UTF_8);
    }

    public void dumpFile(final String filename) {
        ensureNotFreed();
        final int status = LibQuery.queryDumpFile(nativePtr, filename);
        if (status != 0) {
            throw new RuntimeException("Failed to dump query file: " + filename);
        }
    }

    @Override
    public void close() {
        if (nativePtr != 0) {
            LibQuery.queryFree(nativePtr);
            nativePtr = 0;
        }
    }
}
