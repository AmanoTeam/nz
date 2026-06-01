package com.amanoteam.nz.library;

class LibQuery {

    static {
        System.loadLibrary("query-jni");
    }

    static native long queryInit(char sep, String subsep);
    static native void queryFree(long query);

    static native int queryAddString(long query, String key, String value);
    static native int queryAddInt(long query, String key, long value);
    static native int queryAddUint(long query, String key, long value);
    static native int queryAddFloat(long query, String key, double value);

    static native long queryGetItem(long query, long index);

    static native String queryGetString(long query, String key);
    static native String paramGetString(long param);
    static native long queryGetInt(long query, String key);
    static native long paramGetInt(long param);
    static native long queryGetUint(long query, String key);
    static native long paramGetUint(long param);
    static native double queryGetFloat(long query, String key);
    static native double paramGetFloat(long param);
    static native int queryGetBool(long query, String key);
    static native int paramGetBool(long param);

    static native int queryLoadString(long query, String string);
    static native int queryLoadFile(long query, String filename);
    static native int queryLoadEnviron(long query);

    static native int queryDumpString(long query, byte[] destination);
    static native int queryDumpFile(long query, String filename);

    static native void paramFree(long param);
}
