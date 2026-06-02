package com.amanoteam.nz.library;

public class QueryParam implements AutoCloseable {

    private long pointer;

    QueryParam(final long pointer) {
        this.pointer = pointer;
    }

    private void ensureNotFreed() {
        if (pointer == 0) {
            throw new IllegalStateException("QueryParam has been closed");
        }
    }

    public String getString() {
        ensureNotFreed();
        return LibQuery.paramGetString(pointer);
    }

    public long getInt() {
        ensureNotFreed();
        return LibQuery.paramGetInt(pointer);
    }

    public long getUint() {
        ensureNotFreed();
        return LibQuery.paramGetUint(pointer);
    }

    public double getFloat() {
        ensureNotFreed();
        return LibQuery.paramGetFloat(pointer);
    }

    public Boolean getBool() {
        ensureNotFreed();
        final int result = LibQuery.paramGetBool(pointer);
        if (result == -1) {
            return null;
        }
        return result == 1;
    }

    @Override
    public void close() {
        if (pointer != 0) {
            LibQuery.paramFree(pointer);
            pointer = 0;
        }
    }
}
