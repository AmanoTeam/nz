package com.amanoteam.nz.library;

public enum Architecture {
    UNKNOWN,
    AMD64,
    i386,
    ARMEL,
    ARMHF,
    MIPS64EL,
    PPC64EL,
    S390X,
    MIPS,
    MIPSEL,
    ARM64,
    IA64,
    ALPHA,
    S390,
    SPARC,
    HPPA,
    POWERPC;

    public static Architecture fromInt(final int value) {
        final Architecture[] values = values();
        if (value >= 0 && value < values.length) {
            return values[value];
        }
        return UNKNOWN;
    }
}
