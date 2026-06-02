package com.amanoteam.nz.library;

import java.util.ArrayList;
import java.util.List;

public class RepoList implements AutoCloseable {

    private long nativePtr;

    public RepoList() {
        this.nativePtr = LibRepository.repolistCreate();
    }

    private void ensureNotFreed() {
        if (nativePtr == 0) {
            throw new IllegalStateException("RepoList has been closed");
        }
    }

    public static void setConfigDir(final String directory) {
        final int status = LibRepository.repoSetConfigDir(directory);
        if (status != 0) {
            throw new RuntimeException("Failed to set config directory: " + directory);
        }
    }

    public static String getConfigDir() {
        return LibRepository.repoGetConfigDir();
    }

    public void load() {
        ensureNotFreed();
        final int status = LibRepository.repolistLoad(nativePtr);
        if (status != 0) {
            throw new RuntimeException("Failed to load repository list (error: " + status + ")");
        }
    }

    public void destroy() {
        ensureNotFreed();
        final int status = LibRepository.repolistDestroy(nativePtr);
        if (status != 0) {
            throw new RuntimeException("Failed to destroy repository cache (error: " + status + ")");
        }
    }

    public long getRepoCount() {
        ensureNotFreed();
        return LibRepository.repolistGetSize(nativePtr);
    }

    public Repository getRepo(final long index) {
        ensureNotFreed();
        final long ptr = LibRepository.repolistGetRepo(nativePtr, index);
        if (ptr == 0) {
            return null;
        }
        return new Repository(ptr, this);
    }

    public List<Repository> getRepos() {
        ensureNotFreed();
        final long count = getRepoCount();
        final List<Repository> result = new ArrayList<>((int) count);
        for (long i = 0; i < count; i++) {
            result.add(getRepo(i));
        }
        return result;
    }

    public PackageList getInstalled() {
        ensureNotFreed();
        final long ptr = LibRepository.repolistGetInstalled(nativePtr);
        if (ptr == 0) {
            return null;
        }
        return new PackageList(ptr, this, false);
    }

    public Package getPackage(final String name) {
        ensureNotFreed();
        final long ptr = LibRepository.repolistGetPkg(nativePtr, name);
        if (ptr == 0) {
            return null;
        }
        return new Package(ptr, this);
    }

    public PackageList search(final String query, final long position, final long maximum) {
        ensureNotFreed();
        final long ptr = LibRepository.repolistSearchPkg(nativePtr, query, position, maximum);
        if (ptr == 0) {
            return null;
        }
        return new PackageList(ptr, this, true);
    }

    public Repository getPackageRepo(final Package pkg) {
        ensureNotFreed();
        final long ptr = LibRepository.repolistGetPkgRepo(nativePtr, pkg.getNativePtr());
        if (ptr == 0) {
            return null;
        }
        return new Repository(ptr, this);
    }

    public void resolveDeps(final Package pkg) {
        ensureNotFreed();
        final int status = LibRepository.repolistResolveDeps(nativePtr, pkg.getNativePtr());
        if (status != 0) {
            throw new RuntimeException("Failed to resolve dependencies for " + pkg.getName());
        }
    }

    public void installPackage(final String... packages) {
        ensureNotFreed();
        final int status = LibRepository.repolistInstallPackage(nativePtr, packages);
        if (status != 0) {
            throw new RuntimeException("Failed to install packages (error: " + status + ")");
        }
    }

    public void removePackage(final String... packages) {
        ensureNotFreed();
        final int status = LibRepository.repolistRemovePackage(nativePtr, packages);
        if (status != 0) {
            throw new RuntimeException("Failed to remove packages (error: " + status + ")");
        }
    }

    @Override
    public void close() {
        if (nativePtr != 0) {
            LibRepository.repolistFree(nativePtr);
            nativePtr = 0;
        }
    }
}
