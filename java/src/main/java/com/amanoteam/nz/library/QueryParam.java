package com.amanoteam.nz.library;

public class QueryParam implements AutoCloseable {

    private long pointer;

    QueryParam(final long pointer) {
        this.pointer = pointer;
    }

    public String getString() {
        return LibQuery.paramGetString(pointer);
    }

    public long getInt() {
        return LibQuery.paramGetInt(pointer);
    }

    public long getUint() {
        return LibQuery.paramGetUint(pointer);
    }

    public double getFloat() {
        return LibQuery.paramGetFloat(pointer);
    }

    public Boolean getBool() {
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
