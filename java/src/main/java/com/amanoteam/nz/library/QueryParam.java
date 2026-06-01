package com.amanoteam.nz.library;

public class QueryParam implements AutoCloseable {

    private long nativePtr;

    QueryParam(final long nativePtr) {
        this.nativePtr = nativePtr;
    }

    private void ensureNotFreed() {
        if (nativePtr == 0) {
            throw new IllegalStateException("QueryParam has been closed");
        }
    }

    public String getString() {
        ensureNotFreed();
        return LibQuery.paramGetString(nativePtr);
    }

    public long getInt() {
        ensureNotFreed();
        return LibQuery.paramGetInt(nativePtr);
    }

    public long getUint() {
        ensureNotFreed();
        return LibQuery.paramGetUint(nativePtr);
    }

    public double getFloat() {
        ensureNotFreed();
        return LibQuery.paramGetFloat(nativePtr);
    }

    public Boolean getBool() {
        ensureNotFreed();
        final int result = LibQuery.paramGetBool(nativePtr);
        if (result == -1) {
            return null;
        }
        return result == 1;
    }

    @Override
    public void close() {
        if (nativePtr != 0) {
            LibQuery.paramFree(nativePtr);
            nativePtr = 0;
        }
    }
}
