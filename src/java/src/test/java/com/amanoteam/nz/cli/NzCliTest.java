package com.amanoteam.nz.cli;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class NzCliTest {

    private static final String HOME_DIR = System.getProperty("user.home");
    private static final String CONFIG_DIR = HOME_DIR + "/nouzen";
    private static final String LIB_PATH = System.getProperty("java.library.path");
    private static final String C_ROOT = "/tmp/nz-root";
    private static final String C_CLI = C_ROOT + "/bin/nz";

    @BeforeAll
    static void setUp() throws Exception {
        final String realBinary = findRealCNzBinary();
        assertNotNull(realBinary, "C nz binary not found");

        // Clean any stale state from previous runs
        final Path rootPath = Paths.get(C_ROOT);
        if (Files.exists(rootPath)) {
            Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                    if (!dir.equals(rootPath)) {
                        Files.delete(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        // Create a proper directory structure so the C binary can find its config
        Files.createDirectories(Paths.get(C_ROOT + "/bin"));
        Files.createDirectories(Paths.get(C_ROOT + "/etc/nouzen"));

        Files.copy(Paths.get(realBinary), Paths.get(C_CLI), StandardCopyOption.REPLACE_EXISTING);

        // Copy config from existing config
        final Path srcConfig = Paths.get(CONFIG_DIR + "/etc/nouzen");
        final Path dstConfig = Paths.get(C_ROOT + "/etc/nouzen");
        if (Files.exists(srcConfig)) {
            try (final DirectoryStream<Path> stream = Files.newDirectoryStream(srcConfig)) {
                for (final Path entry : stream) {
                    final Path target = dstConfig.resolve(entry.getFileName());
                    if (Files.isDirectory(entry)) {
                        copyDirectory(entry, target);
                    } else {
                        Files.copy(entry, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }
    }

    private static void copyDirectory(final Path src, final Path dst) throws IOException {
        Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(dst.resolve(src.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                Files.copy(file, dst.resolve(src.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @AfterAll
    static void tearDown() throws Exception {
        deleteRecursively(Paths.get(C_ROOT));
    }

    private static void deleteRecursively(final Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (final DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (final Path entry : stream) {
                    deleteRecursively(entry);
                }
            }
        }
        Files.deleteIfExists(path);
    }

    private static String findRealCNzBinary() {
        final String[] candidates = {
            "/tmp/nz",
            "/root/nz/build/nz",
            System.getProperty("user.dir") + "/../../build/nz",
        };
        for (final String c : candidates) {
            if (new java.io.File(c).canExecute()) {
                return c;
            }
        }
        return null;
    }

    private static String runC_CLI(final String... args) throws Exception {
        final String[] cmd = new String[args.length + 1];
        cmd[0] = C_CLI;
        System.arraycopy(args, 0, cmd, 1, args.length);
        final ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(false);
        final Process p = pb.start();

        final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        final ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        final Thread stderrThread = new Thread(() -> {
            try { p.getErrorStream().transferTo(stderr); } catch (final IOException e) {}
        });
        stderrThread.start();
        try { p.getInputStream().transferTo(stdout); } catch (final IOException e) {}

        final int exitCode = p.waitFor();
        stderrThread.join();

        if (exitCode != 0) {
            throw new RuntimeException("C CLI exited with code " + exitCode + ": " + stderr.toString());
        }
        return stdout.toString();
    }

    private static String runJavaCli(final String... args) throws Exception {
        final String classpath = System.getProperty("java.class.path");
        final String javaHome = System.getProperty("java.home");
        final String javaBin = javaHome + "/bin/java";

        if (classpath == null) {
            throw new RuntimeException("java.class.path is null");
        }
        if (!new java.io.File(javaBin).canExecute()) {
            throw new RuntimeException("Java binary not found: " + javaBin);
        }

        final java.util.List<String> cmdList = new java.util.ArrayList<>();
        cmdList.add(javaBin);
        cmdList.add("-Djava.library.path=" + (LIB_PATH != null ? LIB_PATH : "."));
        cmdList.add("-Dnz.config.dir=" + CONFIG_DIR);
        cmdList.add("-Xmx512m");
        cmdList.add("-cp");
        cmdList.add(classpath);
        cmdList.add("com.amanoteam.nz.cli.Nz");
        if (args != null) {
            for (final String arg : args) {
                cmdList.add(arg);
            }
        }

        final ProcessBuilder pb = new ProcessBuilder(cmdList);
        pb.redirectErrorStream(false);
        final Process p = pb.start();

        final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        final ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        final Thread stderrThread = new Thread(() -> {
            try { p.getErrorStream().transferTo(stderr); } catch (final IOException e) {}
        });
        stderrThread.start();
        try { p.getInputStream().transferTo(stdout); } catch (final IOException e) {}

        final int exitCode = p.waitFor();
        stderrThread.join();

        if (exitCode != 0) {
            throw new RuntimeException("Java CLI exited with code " + exitCode + ": " + stderr.toString());
        }
        return stdout.toString();
    }

    static String normalizeOutput(final String output) {
        return output
            .replaceAll("\r\n", "\n")
            .replaceAll("\r", "\n")
            .replaceAll("\u001b\\[[0-9;]*[a-zA-Z]", "")
            .replaceAll(" +", " ")
            .replaceAll("\n ", "\n")
            .replaceAll(" \n", "\n")
            .toLowerCase()
            .trim();
    }

    @Test
    void searchGccProducesSameResults() throws Exception {
        final String cOutput = runC_CLI("--search=gcc");
        final String javaOutput = runJavaCli("--search=gcc");

        assertFalse(cOutput.isEmpty(), "C CLI search should produce output");
        assertFalse(javaOutput.isEmpty(), "Java CLI search should produce output");

        assertEquals(normalizeOutput(cOutput), normalizeOutput(javaOutput));
    }

    @Test
    void searchBashProducesSameResults() throws Exception {
        final String cOutput = runC_CLI("--search=bash");
        final String javaOutput = runJavaCli("--search=bash");

        assertFalse(cOutput.isEmpty(), "C CLI search should produce output");
        assertFalse(javaOutput.isEmpty(), "Java CLI search should produce output");

        assertEquals(normalizeOutput(cOutput), normalizeOutput(javaOutput));
    }

    @Test
    void showBashProducesSameResults() throws Exception {
        final String cOutput = runC_CLI("--show=bash");
        final String javaOutput = runJavaCli("--show=bash");

        assertFalse(cOutput.isEmpty(), "C CLI show should produce output");
        assertFalse(javaOutput.isEmpty(), "Java CLI show should produce output");

        assertEquals(normalizeOutput(cOutput), normalizeOutput(javaOutput));
    }

    @Test
    void showGccDefaultsProducesSameResults() throws Exception {
        final String cOutput = runC_CLI("--show=gcc-defaults");
        final String javaOutput = runJavaCli("--show=gcc-defaults");

        assertEquals(normalizeOutput(cOutput), normalizeOutput(javaOutput));
    }

    @Test
    void versionOutputFormat() throws Exception {
        final String cOutput = runC_CLI("--version");
        final String javaOutput = runJavaCli("--version");

        final String cNorm = normalizeOutput(cOutput);
        final String jNorm = normalizeOutput(javaOutput);

        assertFalse(cNorm.isEmpty());
        assertFalse(jNorm.isEmpty());

        assertTrue(cNorm.startsWith("nouzen v"), "C version should start with 'Nouzen v'");
        assertTrue(jNorm.startsWith("nouzen v"), "Java version should start with 'Nouzen v'");
    }

    @Test
    void helpOutputFormat() throws Exception {
        final String cOutput = runC_CLI("--help");
        final String javaOutput = runJavaCli("--help");

        final String cNorm = normalizeOutput(cOutput);
        final String jNorm = normalizeOutput(javaOutput);

        assertFalse(cNorm.isEmpty());
        assertFalse(jNorm.isEmpty());

        assertTrue(cNorm.startsWith("usage:"), "C help should start with 'usage:'");
        assertTrue(jNorm.startsWith("usage:"), "Java help should start with 'usage:'");
    }
}
