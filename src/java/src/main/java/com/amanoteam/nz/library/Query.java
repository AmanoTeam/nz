package com.amanoteam.nz.library;

public class Query implements AutoCloseable {

    private long pointer;

    public Query() {
        this('&', "=");
    }

    public Query(final char sep, final String subsep) {
        this.pointer = LibQuery.queryInit(sep, subsep);
    }

    public Query add(final String key, final String value) {
        
        final int status = LibQuery.queryAddString(pointer, key, value);
        if (status != 0) {
            throw new RuntimeException("Failed to add string parameter: " + key);
        }
        return this;
    }

    public Query add(final String key, final long value) {
        
        final int status = LibQuery.queryAddInt(pointer, key, value);
        if (status != 0) {
            throw new RuntimeException("Failed to add int parameter: " + key);
        }
        return this;
    }

    public Query addUint(final String key, final long value) {
        
        final int status = LibQuery.queryAddUint(pointer, key, value);
        if (status != 0) {
            throw new RuntimeException("Failed to add uint parameter: " + key);
        }
        return this;
    }

    public Query add(final String key, final double value) {
        
        final int status = LibQuery.queryAddFloat(pointer, key, value);
        if (status != 0) {
            throw new RuntimeException("Failed to add float parameter: " + key);
        }
        return this;
    }

    public String getString(final String key) {
        
        return LibQuery.queryGetString(pointer, key);
    }

    public long getInt(final String key) {
        
        return LibQuery.queryGetInt(pointer, key);
    }

    public long getUint(final String key) {
        
        return LibQuery.queryGetUint(pointer, key);
    }

    public double getFloat(final String key) {
        
        return LibQuery.queryGetFloat(pointer, key);
    }

    public Boolean getBool(final String key) {
        
        final int result = LibQuery.queryGetBool(pointer, key);
        if (result == -1) {
            return null;
        }
        return result == 1;
    }

    public QueryParam getItem(final long index) {
        
        final long paramPtr = LibQuery.queryGetItem(pointer, index);
        if (paramPtr == 0) {
            return null;
        }
        return new QueryParam(paramPtr);
    }

    public void loadString(final String string) {
        
        final int status = LibQuery.queryLoadString(pointer, string);
        if (status != 0) {
            throw new RuntimeException("Failed to load query string");
        }
    }

    public void loadFile(final String filename) {
        
        final int status = LibQuery.queryLoadFile(pointer, filename);
        if (status != 0) {
            throw new RuntimeException("Failed to load query file: " + filename);
        }
    }

    public void loadEnviron() {
        
        final int status = LibQuery.queryLoadEnviron(pointer);
        if (status != 0) {
            throw new RuntimeException("Failed to load environment variables");
        }
    }

    public String dumpString() {
        
        final int size = LibQuery.queryDumpString(pointer, null);
        if (size < 0) {
            throw new RuntimeException("Failed to dump query string");
        }
        final byte[] buffer = new byte[size];
        final int len = LibQuery.queryDumpString(pointer, buffer);
        if (len < 0) {
            throw new RuntimeException("Failed to dump query string");
        }
        return new String(buffer, 0, len, java.nio.charset.StandardCharsets.UTF_8);
    }

    public void dumpFile(final String filename) {
        
        final int status = LibQuery.queryDumpFile(pointer, filename);
        if (status != 0) {
            throw new RuntimeException("Failed to dump query file: " + filename);
        }
    }

    @Override
    public void close() {
        if (pointer != 0) {
            LibQuery.queryFree(pointer);
            pointer = 0;
        }
    }
}
