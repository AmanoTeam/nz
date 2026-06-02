package com.amanoteam.nz.cli;

import com.amanoteam.nz.library.*;
import java.util.ArrayList;
import java.util.List;

public class Nz {

    static final String PROJECT_NAME = "Nouzen";
    static final String PROJECT_VERSION = "0.1-alpha8";
    static final String PROJECT_REPOSITORY = "https://github.com/AmanoTeam/Nouzen";

    static final int PKGS_QUEUE_MAX = 128;

    static final int ACTION_UNKNOWN = 0;
    static final int ACTION_INSTALL = 1;
    static final int ACTION_UNINSTALL = 2;
    static final int ACTION_PARALLELISM = 3;
    static final int ACTION_FORCE_REFRESH = 4;
    static final int ACTION_PREFIX = 5;
    static final int ACTION_ASSUME_YES = 6;
    static final int ACTION_LOGLEVEL = 7;
    static final int ACTION_UPDATE = 8;
    static final int ACTION_DESTROY = 9;
    static final int ACTION_HELP = 10;
    static final int ACTION_VERSION = 11;
    static final int ACTION_SEARCH = 12;
    static final int ACTION_SHOW = 13;

    public static void main(final String[] args) {
        int err = 0;
        int operation = ACTION_UNKNOWN;
        String searchQuery = null;
        final List<String> packages = new ArrayList<>();
        String configDir = null;

        final String propConfigDir = System.getProperty("nz.config.dir");
        if (propConfigDir != null) {
            RepoList.setConfigDir(propConfigDir);
        }

        configDir = RepoList.getConfigDir();
        if (configDir == null) {
            System.err.println("fatal error: (-43) Could not get configuration directory");
            System.exit(1);
        }

        err = Repository.optionsLoad(configDir);
        if (err != 0) {
            System.err.println("fatal error: (" + err + ") Could not load options");
            System.exit(1);
        }

        String osName = System.getProperty("os.name").toLowerCase();

        final List<String> argList = new ArrayList<>();
        for (final String arg : args) {
            if (arg.startsWith("--") && arg.contains("=")) {
                argList.add(arg.substring(0, arg.indexOf('=')));
                argList.add(arg.substring(arg.indexOf('=') + 1));
            } else {
                argList.add(arg);
            }
        }
        final String[] parsedArgs = argList.toArray(new String[0]);

        for (int i = 0; i < parsedArgs.length; i++) {
            final String arg = parsedArgs[i];
            final int action = getAction(arg);
            String value = null;
            if (i + 1 < parsedArgs.length && needsValue(action)) {
                value = parsedArgs[++i];
            }

            switch (action) {
                case ACTION_INSTALL:
                case ACTION_UNINSTALL: {
                    if (value == null) {
                        err = -3;
                        break;
                    }
                    final String[] parts = value.split(";");
                    for (final String p : parts) {
                        if (packages.size() + 2 > PKGS_QUEUE_MAX) {
                            System.err.println("fatal error: (-6) Too many packages");
                            System.exit(1);
                        }
                        packages.add(p);
                    }
                    operation = action;
                    break;
                }
                case ACTION_PARALLELISM:
                case ACTION_PREFIX:
                case ACTION_LOGLEVEL: {
                    break;
                }
                case ACTION_FORCE_REFRESH:
                case ACTION_UPDATE: {
                    break;
                }
                case ACTION_ASSUME_YES: {
                    break;
                }
                case ACTION_DESTROY: {
                    operation = action;
                    break;
                }
                case ACTION_HELP: {
                    printHelp();
                    return;
                }
                case ACTION_VERSION: {
                    final String platform = Repository.osdetectGetPlatform();
                    final String os = platform != null ? platform : osName;
                    System.out.println(PROJECT_NAME + " v" + PROJECT_VERSION + " (" + os + ")");
                    return;
                }
                case ACTION_SEARCH:
                case ACTION_SHOW: {
                    if (value == null) {
                        err = -3;
                        break;
                    }
                    searchQuery = value;
                    operation = action;
                    break;
                }
                case ACTION_UNKNOWN: {
                    err = -3;
                    break;
                }
            }
            if (err != 0) break;
        }

        if (err != 0) {
            System.err.println("fatal error: (" + err + ") Argument parse error");
            System.exit(1);
        }

        try (final RepoList list = new RepoList()) {
            list.load();

            switch (operation) {
                case ACTION_INSTALL: {
                    if (!packages.isEmpty()) {
                        list.installPackage(packages.toArray(new String[0]));
                    }
                    break;
                }
                case ACTION_UNINSTALL: {
                    if (!packages.isEmpty()) {
                        list.removePackage(packages.toArray(new String[0]));
                    }
                    break;
                }
                case ACTION_DESTROY: {
                    list.destroy();
                    break;
                }
                case ACTION_SEARCH: {
                    if (searchQuery != null) {
                        err = performSearch(list, searchQuery);
                    }
                    break;
                }
                case ACTION_SHOW: {
                    if (searchQuery != null) {
                        err = performShow(list, searchQuery);
                    }
                    break;
                }
                default: {
                    break;
                }
            }
        }

        if (err != 0 && operation != ACTION_SEARCH && operation != ACTION_SHOW) {
            System.exit(err);
        }
    }

    static int getAction(final String key) {
        switch (key) {
            case "-i":
            case "--install":
                return ACTION_INSTALL;
            case "-u":
            case "--uninstall":
            case "-r":
            case "--remove":
                return ACTION_UNINSTALL;
            case "-c":
            case "--concurrency":
            case "--parallelism":
                return ACTION_PARALLELISM;
            case "-f":
            case "--force-refresh":
            case "--ignore-cached-index":
                return ACTION_FORCE_REFRESH;
            case "-p":
            case "--prefix":
            case "--install-prefix":
                return ACTION_PREFIX;
            case "-y":
            case "--yes":
            case "--assume-yes":
                return ACTION_ASSUME_YES;
            case "--loglevel":
                return ACTION_LOGLEVEL;
            case "--update":
            case "--refresh":
                return ACTION_UPDATE;
            case "--destroy":
                return ACTION_DESTROY;
            case "-h":
            case "--help":
                return ACTION_HELP;
            case "-v":
            case "--version":
                return ACTION_VERSION;
            case "-s":
            case "--search":
                return ACTION_SEARCH;
            case "--show":
                return ACTION_SHOW;
            default:
                return ACTION_UNKNOWN;
        }
    }

    static boolean needsValue(final int action) {
        switch (action) {
            case ACTION_INSTALL:
            case ACTION_UNINSTALL:
            case ACTION_PARALLELISM:
            case ACTION_PREFIX:
            case ACTION_LOGLEVEL:
            case ACTION_SEARCH:
            case ACTION_SHOW:
                return true;
            default:
                return false;
        }
    }

    static int performSearch(final RepoList repolist, final String query) {
        try (final PackageList pkgs = repolist.search(query, 0, 10000)) {
            if (pkgs == null || pkgs.isEmpty()) {
                System.err.println("No packages matched the query");
                return -35;
            }

            for (final com.amanoteam.nz.library.Package pkg : pkgs) {
                final Repository repo = repolist.getPackageRepo(pkg);
                if (repo != null) {
                    System.out.printf(
                        "\r\n\u001b[92m%s\u001b[0m/%s %s %s\r\n  %s\r\n",
                        nullable(pkg.getName()),
                        nullable(repo.getRelease()),
                        nullable(pkg.getVersion()),
                        nullable(pkg.getArchitecture()),
                        nullable(pkg.getDescription())
                    );
                }
            }
            System.out.println();
            return 0;
        }
    }

    static int performShow(final RepoList repolist, final String query) {
        final com.amanoteam.nz.library.Package pkg = repolist.getPackage(query);
        if (pkg == null) {
            System.err.println("No packages matched the query");
            return -35;
        }

        repolist.resolveDeps(pkg);

        final Repository repo = repolist.getPackageRepo(pkg);

        System.out.println("\r\nPackage: " + nullable(pkg.getName()));
        System.out.println("Version: " + nullable(pkg.getVersion()));

        final List<Maintainer> maintainers = pkg.getMaintainers();
        if (maintainers != null && !maintainers.isEmpty()) {
            System.out.print("Maintainer: ");
            boolean first = true;
            for (final Maintainer m : maintainers) {
                if (!first) System.out.print(", ");
                first = false;
                final String email = m.getEmail() != null ? m.getEmail() : "user@example.com";
                System.out.print(m.getName() + " <" + email + ">");
            }
            System.out.println();
        }

        System.out.println("Installed-Size: " + formatSize(pkg.getInstalledSize()));

        final String[][] depKeys = {
            {"Depends", "getDepends"},
            {"Breaks", "getBreaks"},
            {"Replaces", "getReplaces"}
        };

        for (final String[] depKey : depKeys) {
            PackageList deps = null;
            switch (depKey[1]) {
                case "getDepends": deps = pkg.getDepends(); break;
                case "getBreaks": deps = pkg.getBreaks(); break;
                case "getReplaces": deps = pkg.getReplaces(); break;
            }
            if (deps != null && deps.size() > 0) {
                System.out.print(depKey[0] + ": ");
                boolean first = true;
                for (final com.amanoteam.nz.library.Package dep : deps) {
                    if (!first) System.out.print(", ");
                    first = false;
                    System.out.print(nullable(dep.getName()));
                }
                System.out.println();
            }
        }

        if (pkg.getProvides() != null) {
            System.out.println("Provides: " + pkg.getProvides());
        }
        if (pkg.getHomepage() != null) {
            System.out.println("Homepage: " + pkg.getHomepage());
        }

        System.out.println("Download-Size: " + formatSize(pkg.getSize()));
        System.out.println("Filename: " + nullable(pkg.getFilename()));
        System.out.println("APT-Manual-Installed: " + (pkg.isAutoInstall() ? "no" : "yes"));

        if (repo != null) {
            System.out.println("APT-Sources: " + nullable(repo.getBaseUri()) + " " +
                nullable(repo.getRelease()) + "/" + nullable(repo.getResource()) + " " +
                nullable(repo.getPlatform()) + " Packages");
        }

        if (pkg.getDescription() != null) {
            System.out.println("Description: " + pkg.getDescription());
        }

        System.out.println();
        return 0;
    }

    static void printHelp() {
        System.out.println(
            "usage: nz [-h] [-v] [--update] [-i PACKAGE] [-u PACKAGE] [-s PACKAGE] [-c CONCURRENCY] [-f] [-p PREFIX] [-y] [--loglevel LOGLEVEL]\n" +
            "\n" +
            "A command-line utility for downloading and installing packages from APT repositories.\n" +
            "\n" +
            "options:\n" +
            "  -h, --help            Display this help text and exit.\n" +
            "  -v, --version         Print version information and exit.\n" +
            "  --update              Update the local package index from remote repositories.\n" +
            "  -i PACKAGE, --install PACKAGE\n" +
            "                        Install one or more packages. Use a semicolon-separated list (e.g. 'pkg1;pkg2').\n" +
            "  -u PACKAGE, --uninstall PACKAGE\n" +
            "                        Uninstall one or more packages. Use a semicolon-separated list (e.g. 'pkg1;pkg2').\n" +
            "  -s PACKAGE, --search PACKAGE\n" +
            "                        Search available repositories for packages matching the given query.\n" +
            "  -c CONCURRENCY, --concurrency CONCURRENCY\n" +
            "                        Set the number of parallel downloads. Use '0' for automatic detection, or '1' to disable parallelism.\n" +
            "  -f, --force-refresh   Force a complete rebuild of the local repository index.\n" +
            "  -p PREFIX, --prefix PREFIX\n" +
            "                        Specify an alternate installation root (prefix) for packages.\n" +
            "  -y, --assume-yes      Automatically answer 'yes' to all prompts (non-interactive mode).\n" +
            "  --loglevel LOGLEVEL   Set output verbosity. Valid levels: 'quiet', 'standard', 'warning', 'error', 'verbose'.\n" +
            "\n" +
            "Note: options that take a value must use an equal sign (e.g. --install=PACKAGE).\n"
        );
    }

    static String formatSize(final long bytes) {
        if (bytes == 0) return "0";
        final String[] units = {"", "K", "M", "G", "T", "P", "E", "Z"};
        double value = bytes;
        int unitIndex = 0;
        for (unitIndex = 0; unitIndex < units.length; unitIndex++) {
            if (value < 1024.0) break;
            value /= 1024.0;
        }
        final String unit = unitIndex < units.length ? units[unitIndex] : "Y";
        return String.format("%.1f %sB", value, unit);
    }

    static String nullable(final Object value) {
        return value != null ? value.toString() : "";
    }
}
