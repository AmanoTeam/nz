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
        return LibRepository.pkgGetName(nativePtr);
    }

    public String getVersion() {
        ensureNotFreed();
        return LibRepository.pkgGetVersion(nativePtr);
    }

    public String getDescription() {
        ensureNotFreed();
        return LibRepository.pkgGetDescription(nativePtr);
    }

    public String getHomepage() {
        ensureNotFreed();
        return LibRepository.pkgGetHomepage(nativePtr);
    }

    public String getBugs() {
        ensureNotFreed();
        return LibRepository.pkgGetBugs(nativePtr);
    }

    public String getFilename() {
        ensureNotFreed();
        return LibRepository.pkgGetFilename(nativePtr);
    }

    public long getSize() {
        ensureNotFreed();
        return LibRepository.pkgGetSize(nativePtr);
    }

    public long getInstalledSize() {
        ensureNotFreed();
        return LibRepository.pkgGetInstalledSize(nativePtr);
    }

    public Architecture getArchitecture() {
        ensureNotFreed();
        return Architecture.fromInt(LibRepository.pkgGetArchitecture(nativePtr));
    }

    public boolean isObsolete() {
        ensureNotFreed();
        return LibRepository.pkgGetObsolete(nativePtr);
    }

    public boolean isInstalled() {
        ensureNotFreed();
        return LibRepository.pkgGetInstalled(nativePtr);
    }

    public boolean isUpgradable() {
        ensureNotFreed();
        return LibRepository.pkgGetUpgradable(nativePtr);
    }

    public boolean isRemovable() {
        ensureNotFreed();
        return LibRepository.pkgGetRemovable(nativePtr);
    }

    public boolean isAutoInstall() {
        ensureNotFreed();
        return LibRepository.pkgGetAutoinstall(nativePtr);
    }

    public String getProvides() {
        ensureNotFreed();
        return LibRepository.pkgGetProvides(nativePtr);
    }

    public PackageList getDepends() {
        ensureNotFreed();
        final long ptr = LibRepository.pkgGetDepends(nativePtr);
        if (ptr == 0) {
            return null;
        }
        return new PackageList(ptr, repoList, false);
    }

    public PackageList getRecommends() {
        ensureNotFreed();
        final long ptr = LibRepository.pkgGetRecommends(nativePtr);
        if (ptr == 0) {
            return null;
        }
        return new PackageList(ptr, repoList, false);
    }

    public PackageList getSuggests() {
        ensureNotFreed();
        final long ptr = LibRepository.pkgGetSuggests(nativePtr);
        if (ptr == 0) {
            return null;
        }
        return new PackageList(ptr, repoList, false);
    }

    public PackageList getBreaks() {
        ensureNotFreed();
        final long ptr = LibRepository.pkgGetBreaks(nativePtr);
        if (ptr == 0) {
            return null;
        }
        return new PackageList(ptr, repoList, false);
    }

    public PackageList getReplaces() {
        ensureNotFreed();
        final long ptr = LibRepository.pkgGetReplaces(nativePtr);
        if (ptr == 0) {
            return null;
        }
        return new PackageList(ptr, repoList, false);
    }

    public List<Maintainer> getMaintainers() {
        ensureNotFreed();
        final long ptr = LibRepository.pkgGetMaintainers(nativePtr);
        if (ptr == 0) {
            return null;
        }
        final long size = LibRepository.maintainersGetSize(ptr);
        final List<Maintainer> result = new ArrayList<>((int) size);
        for (long i = 0; i < size; i++) {
            final String name = LibRepository.maintainersGetName(ptr, i);
            final String email = LibRepository.maintainersGetEmail(ptr, i);
            result.add(new Maintainer(name, email));
        }
        return result;
    }

    public int getRepoIndex() {
        ensureNotFreed();
        return (int) LibRepository.pkgGetRepo(nativePtr);
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
