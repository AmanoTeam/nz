package com.amanoteam.nz.library;

import java.util.ArrayList;
import java.util.List;

public class Package {

    private final long pointer;
    private final RepoList repoList;

    Package(final long pointer, final RepoList repoList) {
        this.pointer = pointer;
        this.repoList = repoList;
    }

    long getPointer() {
        return pointer;
    }

    void ensureNotFreed() {
        if (pointer == 0) {
            throw new IllegalStateException("Package native pointer is invalid");
        }
    }

    public String getName() {
        ensureNotFreed();
        return Repository.pkgGetName(pointer);
    }

    public String getVersion() {
        ensureNotFreed();
        return Repository.pkgGetVersion(pointer);
    }

    public String getDescription() {
        ensureNotFreed();
        return Repository.pkgGetDescription(pointer);
    }

    public String getHomepage() {
        ensureNotFreed();
        return Repository.pkgGetHomepage(pointer);
    }

    public String getBugs() {
        ensureNotFreed();
        return Repository.pkgGetBugs(pointer);
    }

    public String getFilename() {
        ensureNotFreed();
        return Repository.pkgGetFilename(pointer);
    }

    public long getSize() {
        ensureNotFreed();
        return Repository.pkgGetSize(pointer);
    }

    public long getInstalledSize() {
        ensureNotFreed();
        return Repository.pkgGetInstalledSize(pointer);
    }

    public Architecture getArchitecture() {
        ensureNotFreed();
        return Architecture.fromInt(Repository.pkgGetArchitecture(pointer));
    }

    public boolean isObsolete() {
        ensureNotFreed();
        return Repository.pkgGetObsolete(pointer);
    }

    public boolean isInstalled() {
        ensureNotFreed();
        return Repository.pkgGetInstalled(pointer);
    }

    public boolean isUpgradable() {
        ensureNotFreed();
        return Repository.pkgGetUpgradable(pointer);
    }

    public boolean isRemovable() {
        ensureNotFreed();
        return Repository.pkgGetRemovable(pointer);
    }

    public boolean isAutoInstall() {
        ensureNotFreed();
        return Repository.pkgGetAutoinstall(pointer);
    }

    public String getProvides() {
        ensureNotFreed();
        return Repository.pkgGetProvides(pointer);
    }

    public PackageList getDepends() {
        ensureNotFreed();
        final long ptr = Repository.pkgGetDepends(pointer);
        if (ptr == 0) {
            return null;
        }
        return new PackageList(ptr, repoList, false);
    }

    public PackageList getRecommends() {
        ensureNotFreed();
        final long ptr = Repository.pkgGetRecommends(pointer);
        if (ptr == 0) {
            return null;
        }
        return new PackageList(ptr, repoList, false);
    }

    public PackageList getSuggests() {
        ensureNotFreed();
        final long ptr = Repository.pkgGetSuggests(pointer);
        if (ptr == 0) {
            return null;
        }
        return new PackageList(ptr, repoList, false);
    }

    public PackageList getBreaks() {
        ensureNotFreed();
        final long ptr = Repository.pkgGetBreaks(pointer);
        if (ptr == 0) {
            return null;
        }
        return new PackageList(ptr, repoList, false);
    }

    public PackageList getReplaces() {
        ensureNotFreed();
        final long ptr = Repository.pkgGetReplaces(pointer);
        if (ptr == 0) {
            return null;
        }
        return new PackageList(ptr, repoList, false);
    }

    public List<Maintainer> getMaintainers() {
        ensureNotFreed();
        final long ptr = Repository.pkgGetMaintainers(pointer);
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
        return (int) Repository.pkgGetRepo(pointer);
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
