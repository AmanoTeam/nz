package com.amanoteam.nz.library;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import java.util.List;

class RepoListTest {

    private static final String CONFIG_DIR = System.getProperty("user.home") + "/nouzen";
    private static RepoList sharedList;

    @BeforeAll
    static void setUp() {
        RepoList.setConfigDir(CONFIG_DIR);
        sharedList = new RepoList();
        sharedList.load();
    }

    @AfterAll
    static void tearDown() {
        if (sharedList != null) {
            sharedList.close();
        }
    }

    @Test
    void configDirRoundTrip() {
        final String dir = RepoList.getConfigDir();
        assertNotNull(dir);
        assertTrue(dir.contains("nouzen"), "Config dir should contain 'nouzen': " + dir);
        assertTrue(dir.endsWith("etc/nouzen"), "Config dir should end with etc/nouzen: " + dir);
    }

    @Test
    void loadRepos() {
        assertTrue(sharedList.getRepoCount() > 0, "Expected at least one repository");
    }

    @Test
    void repoFields() {
        final Repository repo = sharedList.getRepo(0);
        assertNotNull(repo);
        assertNotNull(repo.getName(), "Repo name should not be null");
        assertNotNull(repo.getRelease(), "Repo release should not be null");
        assertNotNull(repo.getResource(), "Repo resource should not be null");
        assertNotNull(repo.getPlatform(), "Repo platform should not be null");
        assertNotNull(repo.getArchitecture(), "Repo architecture should not be null");
        assertTrue(repo.getType() >= 0, "Repo type should be valid");
    }

    @Test
    void iterateAllPackages() {
        for (long i = 0; i < sharedList.getRepoCount(); i++) {
            final Repository repo = sharedList.getRepo(i);
            assertNotNull(repo);
            final PackageList pkgs = repo.getPackages();
            assertNotNull(pkgs);
            assertTrue(pkgs.size() > 0, "Repository should have packages");

            int count = 0;
            for (final Package pkg : pkgs) {
                assertNotNull(pkg.getName(), "Package name should not be null");
                assertNotNull(pkg.getVersion(), "Package version should not be null for " + pkg.getName());
                count++;
                if (count > 5) break;
            }
            assertTrue(count > 0, "Should have iterated at least one package");
        }
    }

    @Test
    void searchPackages() {
        try (final PackageList results = sharedList.search("gcc", 0, 15)) {
            assertNotNull(results);
            assertTrue(results.size() > 0, "Search for 'gcc' should return results");
            for (final Package pkg : results) {
                assertNotNull(pkg.getName());
                assertNotNull(pkg.getVersion());
            }
        }
    }

    @Test
    void getPackageByName() {
        final Package pkg = sharedList.getPackage("gcc-defaults");
        if (pkg != null) {
            assertEquals("gcc-defaults", pkg.getName());
            assertNotNull(pkg.getVersion());
            assertNotNull(pkg.getDescription());
        }
    }

    @Test
    void packageFields() {
        final Package pkg = sharedList.getPackage("bash");
        if (pkg != null) {
            assertEquals("bash", pkg.getName());
            assertNotNull(pkg.getVersion());
            assertTrue(pkg.getSize() >= 0);
            assertTrue(pkg.getInstalledSize() >= 0);
            assertNotNull(pkg.getArchitecture());
        }
    }

    @Test
    void packageDependencies() {
        final Package pkg = sharedList.getPackage("gcc-defaults");
        if (pkg != null) {
            final PackageList depends = pkg.getDepends();
            if (depends != null) {
                assertTrue(depends.size() > 0, "gcc-defaults should have dependencies");
                for (final Package dep : depends) {
                    assertNotNull(dep.getName());
                }
            }
        }
    }

    @Test
    void packageMaintainers() {
        final Package pkg = sharedList.getPackage("bash");
        if (pkg != null) {
            sharedList.resolveDeps(pkg);
            final List<Maintainer> maintainers = pkg.getMaintainers();
            if (maintainers != null) {
                assertFalse(maintainers.isEmpty());
                for (final Maintainer m : maintainers) {
                    assertNotNull(m.getName());
                }
            }
        }
    }

    @Test
    void architectureEnumRoundTrip() {
        assertEquals(Architecture.UNKNOWN, Architecture.fromInt(999));
        assertEquals(Architecture.UNKNOWN, Architecture.fromInt(-1));

        for (final Architecture arch : Architecture.values()) {
            final int ordinal = arch.ordinal();
            final Architecture roundTripped = Architecture.fromInt(ordinal);
            assertEquals(arch, roundTripped, "Architecture round-trip failed for " + arch);
        }
    }

    @Test
    void resolveDeps() {
        final Package pkg = sharedList.getPackage("bash");
        if (pkg != null) {
            sharedList.resolveDeps(pkg);
        }
    }

    @Test
    void getPackageReturnsNullForNonexistent() {
        assertNull(sharedList.getPackage("this-package-definitely-does-not-exist-12345"));
    }

    @Test
    void getRepoByIndex() {
        final long count = sharedList.getRepoCount();
        assertTrue(count > 0);
        final Repository repo = sharedList.getRepo(0);
        assertNotNull(repo);
        assertNull(sharedList.getRepo(count + 100), "Out-of-bounds index should return null");
    }

    @Test
    void installedPackageList() {
        final PackageList installed = sharedList.getInstalled();
        assertNotNull(installed);
    }
}
