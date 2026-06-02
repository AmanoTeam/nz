package com.amanoteam.nz.library;

import java.util.ArrayList;
import java.util.List;

public class Package {

    private final long nativePtr;
    private final RepoList repoList;

    Package(final long nativePtr, final RepoList repoList) {
        this.nativePtr = nativePtr;
        this.repoList = repoList;
    }

    long getNativePtr() {
        return nativePtr;
    }

    void ensureNotFreed() {
        if (nativePtr == 0) {
            throw new IllegalStateException("Package native pointer is invalid");
        }
    }

    public String getName() {
        ensureNotFreed();
        return Repository.pkgGetName(nativePtr);
    }

    public String getVersion() {
        ensureNotFreed();
        return Repository.pkgGetVersion(nativePtr);
    }

    public String getDescription() {
        ensureNotFreed();
        return Repository.pkgGetDescription(nativePtr);
    }

    public String getHomepage() {
        ensureNotFreed();
        return Repository.pkgGetHomepage(nativePtr);
    }

    public String getBugs() {
        ensureNotFreed();
        return Repository.pkgGetBugs(nativePtr);
    }

    public String getFilename() {
        ensureNotFreed();
        return Repository.pkgGetFilename(nativePtr);
    }

    public long getSize() {
        ensureNotFreed();
        return Repository.pkgGetSize(nativePtr);
    }

    public long getInstalledSize() {
        ensureNotFreed();
        return Repository.pkgGetInstalledSize(nativePtr);
    }

    public Architecture getArchitecture() {
        ensureNotFreed();
        return Architecture.fromInt(Repository.pkgGetArchitecture(nativePtr));
    }

    public boolean isObsolete() {
        ensureNotFreed();
        return Repository.pkgGetObsolete(nativePtr);
    }

    public boolean isInstalled() {
        ensureNotFreed();
        return Repository.pkgGetInstalled(nativePtr);
    }

    public boolean isUpgradable() {
        ensureNotFreed();
        return Repository.pkgGetUpgradable(nativePtr);
    }

    public boolean isRemovable() {
        ensureNotFreed();
        return Repository.pkgGetRemovable(nativePtr);
    }

    public boolean isAutoInstall() {
        ensureNotFreed();
        return Repository.pkgGetAutoinstall(nativePtr);
    }

    public String getProvides() {
        ensureNotFreed();
        return Repository.pkgGetProvides(nativePtr);
    }

    public PackageList getDepends() {
        ensureNotFreed();
        final long ptr = Repository.pkgGetDepends(nativePtr);
        if (ptr == 0) {
            return null;
        }
        return new PackageList(ptr, repoList, false);
    }

    public PackageList getRecommends() {
        ensureNotFreed();
        final long ptr = Repository.pkgGetRecommends(nativePtr);
        if (ptr == 0) {
            return null;
        }
        return new PackageList(ptr, repoList, false);
    }

    public PackageList getSuggests() {
        ensureNotFreed();
        final long ptr = Repository.pkgGetSuggests(nativePtr);
        if (ptr == 0) {
            return null;
        }
        return new PackageList(ptr, repoList, false);
    }

    public PackageList getBreaks() {
        ensureNotFreed();
        final long ptr = Repository.pkgGetBreaks(nativePtr);
        if (ptr == 0) {
            return null;
        }
        return new PackageList(ptr, repoList, false);
    }

    public PackageList getReplaces() {
        ensureNotFreed();
        final long ptr = Repository.pkgGetReplaces(nativePtr);
        if (ptr == 0) {
            return null;
        }
        return new PackageList(ptr, repoList, false);
    }

    public List<Maintainer> getMaintainers() {
        ensureNotFreed();
        final long ptr = Repository.pkgGetMaintainers(nativePtr);
        if (ptr == 0) {
            return null;
        }
        final long size = Repository.maintainersGetSize(ptr);
        final List<Maintainer> result = new ArrayList<>((int) size);
        for (long i = 0; i < size; i++) {
            final String name = Repository.maintainersGetName(ptr, i);
            final String email = Repository.maintainersGetEmail(ptr, i);
            result.add(new Maintainer(name, email));
        }
        return result;
    }

    public int getRepoIndex() {
        ensureNotFreed();
        return (int) Repository.pkgGetRepo(nativePtr);
    }

    public Repository getRepository() {
        ensureNotFreed();
        return repoList.getRepo(getRepoIndex());
    }

    @Override
    public String toString() {
        final String name = getName();
        final String version = getVersion();
        if (name == null) {
            return "Package(null)";
        }
        if (version == null) {
            return name;
        }
        return name + "/" + version;
    }
}
