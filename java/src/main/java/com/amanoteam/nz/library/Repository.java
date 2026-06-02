package com.amanoteam.nz.library;

public class Repository {

    static {
        System.loadLibrary("query-jni");
    }

    private final long pointer;
    private final RepoList repoList;

    Repository(final long pointer, final RepoList repoList) {
        this.pointer = pointer;
        this.repoList = repoList;
    }

    // Config
    public static native int repoSetConfigDir(String directory);
    public static native String repoGetConfigDir();

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
    static native String repoGetBaseUri(long repoPtr);

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

    // Operations
    public static native int repolistInstallPackage(long listPtr, String[] packages);
    public static native int repolistRemovePackage(long listPtr, String[] packages);
    public static native int optionsLoad(String directory);
    public static native int getNproc();
    public static native String osdetectGetPlatform();

    // Architecture helpers
    static native String archUnstringify(int arch);
    static native int getArchitecture(String name);

    // === Repository instance methods ===

    void ensureNotFreed() {
        if (pointer == 0) {
            throw new IllegalStateException("Repository native pointer is invalid");
        }
    }

    public int getType() {
        ensureNotFreed();
        return repoGetType(pointer);
    }

    public String getName() {
        ensureNotFreed();
        return repoGetName(pointer);
    }

    public String getRelease() {
        ensureNotFreed();
        return repoGetRelease(pointer);
    }

    public String getResource() {
        ensureNotFreed();
        return repoGetResource(pointer);
    }

    public String getPlatform() {
        ensureNotFreed();
        return repoGetPlatform(pointer);
    }

    public String getLocation() {
        ensureNotFreed();
        return repoGetLocation(pointer);
    }

    public String getSpecification() {
        ensureNotFreed();
        return repoGetSpecification(pointer);
    }

    public Architecture getArchitecture() {
        ensureNotFreed();
        return Architecture.fromInt(repoGetArchitecture(pointer));
    }

    public String getBaseUri() {
        ensureNotFreed();
        return repoGetBaseUri(pointer);
    }

    public PackageList getPackages() {
        ensureNotFreed();
        final long ptr = repoGetPkgs(pointer);
        if (ptr == 0) {
            return null;
        }
        return new PackageList(ptr, repoList, false);
    }

    @Override
    public String toString() {
        final String name = getName();
        if (name == null) {
            return "Repository(null)";
        }
        final String release = getRelease();
        if (release == null) {
            return name;
        }
        return name + "/" + release;
    }
}
