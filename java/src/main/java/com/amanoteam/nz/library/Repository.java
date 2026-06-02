package com.amanoteam.nz.library;

public class Repository {

    private final long nativePtr;
    private final RepoList repoList;

    Repository(final long nativePtr, final RepoList repoList) {
        this.nativePtr = nativePtr;
        this.repoList = repoList;
    }

    void ensureNotFreed() {
        if (nativePtr == 0) {
            throw new IllegalStateException("Repository native pointer is invalid");
        }
    }

    public int getType() {
        ensureNotFreed();
        return LibRepository.repoGetType(nativePtr);
    }

    public String getName() {
        ensureNotFreed();
        return LibRepository.repoGetName(nativePtr);
    }

    public String getRelease() {
        ensureNotFreed();
        return LibRepository.repoGetRelease(nativePtr);
    }

    public String getResource() {
        ensureNotFreed();
        return LibRepository.repoGetResource(nativePtr);
    }

    public String getPlatform() {
        ensureNotFreed();
        return LibRepository.repoGetPlatform(nativePtr);
    }

    public String getLocation() {
        ensureNotFreed();
        return LibRepository.repoGetLocation(nativePtr);
    }

    public String getSpecification() {
        ensureNotFreed();
        return LibRepository.repoGetSpecification(nativePtr);
    }

    public Architecture getArchitecture() {
        ensureNotFreed();
        return Architecture.fromInt(LibRepository.repoGetArchitecture(nativePtr));
    }

    public String getBaseUri() {
        ensureNotFreed();
        return LibRepository.repoGetBaseUri(nativePtr);
    }

    public PackageList getPackages() {
        ensureNotFreed();
        final long ptr = LibRepository.repoGetPkgs(nativePtr);
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
