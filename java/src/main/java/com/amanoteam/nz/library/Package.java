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

    public String getName() {
        return Repository.pkgGetName(pointer);
    }

    public String getVersion() {
        return Repository.pkgGetVersion(pointer);
    }

    public String getDescription() {
        return Repository.pkgGetDescription(pointer);
    }

    public String getHomepage() {
        return Repository.pkgGetHomepage(pointer);
    }

    public String getBugs() {
        return Repository.pkgGetBugs(pointer);
    }

    public String getFilename() {
        return Repository.pkgGetFilename(pointer);
    }

    public long getSize() {
        return Repository.pkgGetSize(pointer);
    }

    public long getInstalledSize() {
        return Repository.pkgGetInstalledSize(pointer);
    }

    public Architecture getArchitecture() {
        return Architecture.fromInt(Repository.pkgGetArchitecture(pointer));
    }

    public boolean isObsolete() {
        return Repository.pkgGetObsolete(pointer);
    }

    public boolean isInstalled() {
        return Repository.pkgGetInstalled(pointer);
    }

    public boolean isUpgradable() {
        return Repository.pkgGetUpgradable(pointer);
    }

    public boolean isRemovable() {
        return Repository.pkgGetRemovable(pointer);
    }

    public boolean isAutoInstall() {
        return Repository.pkgGetAutoinstall(pointer);
    }

    public String getProvides() {
        return Repository.pkgGetProvides(pointer);
    }

    public PackageList getDepends() {
        final long ptr = Repository.pkgGetDepends(pointer);
        if (ptr == 0) {
            return null;
        }
        return new PackageList(ptr, repoList, false);
    }

    public PackageList getRecommends() {
        final long ptr = Repository.pkgGetRecommends(pointer);
        if (ptr == 0) {
            return null;
        }
        return new PackageList(ptr, repoList, false);
    }

    public PackageList getSuggests() {
        final long ptr = Repository.pkgGetSuggests(pointer);
        if (ptr == 0) {
            return null;
        }
        return new PackageList(ptr, repoList, false);
    }

    public PackageList getBreaks() {
        final long ptr = Repository.pkgGetBreaks(pointer);
        if (ptr == 0) {
            return null;
        }
        return new PackageList(ptr, repoList, false);
    }

    public PackageList getReplaces() {
        final long ptr = Repository.pkgGetReplaces(pointer);
        if (ptr == 0) {
            return null;
        }
        return new PackageList(ptr, repoList, false);
    }

    public List<Maintainer> getMaintainers() {
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
        return (int) Repository.pkgGetRepo(pointer);
    }

    public Repository getRepository() {
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
