package com.amanoteam.nz.library;

class LibRepository {
    static {
        System.loadLibrary("query-jni");
    }

    private LibRepository() {}

    // Config
    static native int repoSetConfigDir(String directory);
    static native String repoGetConfigDir();

    // RepoList lifecycle
    static native long repolistCreate();
    static native void repolistFree(long listPtr);
    static native int repolistLoad(long listPtr);
    static native int repolistDestroy(long listPtr);

    // RepoList queries
    static native long repolistGetSize(long listPtr);
    static native long repolistGetRepo(long listPtr, long index);
    static native long repolistGetInstalled(long listPtr);
    static native long repolistGetPkg(long listPtr, String name);
    static native long repolistSearchPkg(long listPtr, String query, long position, long maximum);
    static native long repolistGetPkgRepo(long listPtr, long pkgPtr);
    static native int repolistResolveDeps(long listPtr, long pkgPtr);

    // Repository field accessors
    static native int repoGetType(long repoPtr);
    static native String repoGetName(long repoPtr);
    static native String repoGetRelease(long repoPtr);
    static native String repoGetResource(long repoPtr);
    static native String repoGetPlatform(long repoPtr);
    static native String repoGetLocation(long repoPtr);
    static native String repoGetSpecification(long repoPtr);
    static native int repoGetArchitecture(long repoPtr);
    static native long repoGetPkgs(long repoPtr);

    // Package field accessors
    static native String pkgGetName(long pkgPtr);
    static native String pkgGetVersion(long pkgPtr);
    static native String pkgGetDescription(long pkgPtr);
    static native String pkgGetHomepage(long pkgPtr);
    static native String pkgGetBugs(long pkgPtr);
    static native String pkgGetFilename(long pkgPtr);
    static native long pkgGetSize(long pkgPtr);
    static native long pkgGetInstalledSize(long pkgPtr);
    static native int pkgGetArchitecture(long pkgPtr);
    static native boolean pkgGetObsolete(long pkgPtr);
    static native boolean pkgGetInstalled(long pkgPtr);
    static native boolean pkgGetUpgradable(long pkgPtr);
    static native boolean pkgGetRemovable(long pkgPtr);
    static native boolean pkgGetAutoinstall(long pkgPtr);
    static native long pkgGetDepends(long pkgPtr);
    static native long pkgGetRecommends(long pkgPtr);
    static native long pkgGetSuggests(long pkgPtr);
    static native long pkgGetBreaks(long pkgPtr);
    static native long pkgGetReplaces(long pkgPtr);
    static native String pkgGetProvides(long pkgPtr);
    static native long pkgGetMaintainers(long pkgPtr);
    static native long pkgGetRepo(long pkgPtr);

    // Packages (pkgs_t) access
    static native long pkgsGetSize(long pkgsPtr);
    static native long pkgsGetItem(long pkgsPtr, long index);
    static native void pkgsFree(long pkgsPtr, int copy);

    // Maintainers access
    static native long maintainersGetSize(long maintainersPtr);
    static native String maintainersGetName(long maintainersPtr, long index);
    static native String maintainersGetEmail(long maintainersPtr, long index);

    // Architecture helpers
    static native String archUnstringify(int arch);
    static native int getArchitecture(String name);
}
