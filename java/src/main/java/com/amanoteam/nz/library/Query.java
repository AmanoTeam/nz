package com.amanoteam.nz.library;

public class Query implements AutoCloseable {

    private long nativePtr;

    public Query() {
        this('&', "=");
    }

    public Query(char sep, String subsep) {
        this.nativePtr = LibQuery.queryInit(sep, subsep);
    }

    private void ensureNotFreed() {
        if (nativePtr == 0) {
            throw new IllegalStateException("Query has been closed");
        }
    }

    public Query add(String key, String value) {
        ensureNotFreed();
        int status = LibQuery.queryAddString(nativePtr, key, value);
        if (status != 0) {
            throw new RuntimeException("Failed to add string parameter: " + key);
        }
        return this;
    }

    public Query add(String key, long value) {
        ensureNotFreed();
        int status = LibQuery.queryAddInt(nativePtr, key, value);
        if (status != 0) {
            throw new RuntimeException("Failed to add int parameter: " + key);
        }
        return this;
    }

    public Query addUint(String key, long value) {
        ensureNotFreed();
        int status = LibQuery.queryAddUint(nativePtr, key, value);
        if (status != 0) {
            throw new RuntimeException("Failed to add uint parameter: " + key);
        }
        return this;
    }

    public Query add(String key, double value) {
        ensureNotFreed();
        int status = LibQuery.queryAddFloat(nativePtr, key, value);
        if (status != 0) {
            throw new RuntimeException("Failed to add float parameter: " + key);
        }
        return this;
    }

    public String getString(String key) {
        ensureNotFreed();
        return LibQuery.queryGetString(nativePtr, key);
    }

    public long getInt(String key) {
        ensureNotFreed();
        return LibQuery.queryGetInt(nativePtr, key);
    }

    public long getUint(String key) {
        ensureNotFreed();
        return LibQuery.queryGetUint(nativePtr, key);
    }

    public double getFloat(String key) {
        ensureNotFreed();
        return LibQuery.queryGetFloat(nativePtr, key);
    }

    public Boolean getBool(String key) {
        ensureNotFreed();
        int result = LibQuery.queryGetBool(nativePtr, key);
        if (result == -1) {
            return null;
        }
        return result == 1;
    }

    public QueryParam getItem(long index) {
        ensureNotFreed();
        long paramPtr = LibQuery.queryGetItem(nativePtr, index);
        if (paramPtr == 0) {
            return null;
        }
        return new QueryParam(paramPtr);
    }

    public void loadString(String string) {
        ensureNotFreed();
        int status = LibQuery.queryLoadString(nativePtr, string);
        if (status != 0) {
            throw new RuntimeException("Failed to load query string");
        }
    }

    public void loadFile(String filename) {
        ensureNotFreed();
        int status = LibQuery.queryLoadFile(nativePtr, filename);
        if (status != 0) {
            throw new RuntimeException("Failed to load query file: " + filename);
        }
    }

    public void loadEnviron() {
        ensureNotFreed();
        int status = LibQuery.queryLoadEnviron(nativePtr);
        if (status != 0) {
            throw new RuntimeException("Failed to load environment variables");
        }
    }

    public String dumpString() {
        ensureNotFreed();
        int size = LibQuery.queryDumpString(nativePtr, null);
        if (size < 0) {
            throw new RuntimeException("Failed to dump query string");
        }
        byte[] buffer = new byte[size];
        int len = LibQuery.queryDumpString(nativePtr, buffer);
        if (len < 0) {
            throw new RuntimeException("Failed to dump query string");
        }
        return new String(buffer, 0, len, java.nio.charset.StandardCharsets.UTF_8);
    }

    public void dumpFile(String filename) {
        ensureNotFreed();
        int status = LibQuery.queryDumpFile(nativePtr, filename);
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
