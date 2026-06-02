package com.amanoteam.nz.library;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PackageList implements Iterable<Package>, AutoCloseable {

    private long nativePtr;
    private final RepoList repoList;
    private final boolean ownsPtr;

    PackageList(final long nativePtr, final RepoList repoList, final boolean ownsPtr) {
        this.nativePtr = nativePtr;
        this.repoList = repoList;
        this.ownsPtr = ownsPtr;
    }

    private void ensureNotFreed() {
        if (nativePtr == 0) {
            throw new IllegalStateException("PackageList has been closed");
        }
    }

    public long size() {
        ensureNotFreed();
        return Repository.pkgsGetSize(nativePtr);
    }

    public Package get(final long index) {
        ensureNotFreed();
        final long pkgPtr = Repository.pkgsGetItem(nativePtr, index);
        if (pkgPtr == 0) {
            return null;
        }
        return new Package(pkgPtr, repoList);
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Iterator<Package> iterator() {
        ensureNotFreed();
        return new Iterator<Package>() {
            private long index = 0;
            private final long total = size();

            private Package advance() {
                while (index < total) {
                    final Package pkg = get(index++);
                    if (pkg != null) {
                        return pkg;
                    }
                }
                return null;
            }

            private Package nextPkg = advance();

            @Override
            public boolean hasNext() {
                return nextPkg != null;
            }

            @Override
            public Package next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                final Package result = nextPkg;
                nextPkg = advance();
                return result;
            }
        };
    }

    @Override
    public Spliterator<Package> spliterator() {
        return Spliterators.spliterator(iterator(), size(), 0);
    }

    public Stream<Package> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @Override
    public void close() {
        if (nativePtr != 0 && ownsPtr) {
            Repository.pkgsFree(nativePtr, 0);
        }
        nativePtr = 0;
    }
}
