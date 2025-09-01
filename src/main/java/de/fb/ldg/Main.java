package de.fb.ldg;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                System.err.println("Usage: java de.fb.ldg.Main <lua-file1> [lua-file2] ... [lua-fileN]");
                System.err.println("   OR: java de.fb.ldg.Main <directory>");
                System.err.println("Examples:");
                System.err.println("  java de.fb.ldg.Main test.lua example.lua");
                System.err.println("  java de.fb.ldg.Main src/lua/");
                System.exit(1);
            }

            List<String> luaFiles = new ArrayList<>();

            // Check if we have a single argument that's a directory
            if (args.length == 1) {
                Path path = Paths.get(args[0]);
                if (Files.isDirectory(path)) {
                    System.out.println("Scanning directory: " + path.toAbsolutePath());
                    luaFiles.addAll(findLuaFiles(path));
                    if (luaFiles.isEmpty()) {
                        System.err.println("No .lua files found in directory: " + args[0]);
                        System.exit(1);
                    }
                    System.out.println("Found " + luaFiles.size() + " Lua file(s):");
                    for (String file : luaFiles) {
                        System.out.println("  - " + file);
                    }
                } else {
                    // Single file
                    luaFiles.add(args[0]);
                }
            } else {
                // Multiple files specified
                for (String arg : args) {
                    luaFiles.add(arg);
                }
            }

            // Validate all files exist and are readable
            for (String filePath : luaFiles) {
                Path path = Paths.get(filePath);
                if (!Files.exists(path)) {
                    System.err.println("Error: File does not exist: " + filePath);
                    System.exit(1);
                }
                if (!Files.isReadable(path)) {
                    System.err.println("Error: Cannot read file: " + filePath);
                    System.exit(1);
                }
            }

            // Generate documentation from files
            Documentation documentation = DocGenerator.generateFromFiles(luaFiles.toArray(new String[0]));

            // Console output (optional)
            System.out.println("\n=== CONSOLE OUTPUT ===");
            System.out.println(documentation);

            // Generate HTML documentation
            System.out.println("\n=== HTML GENERATION ===");
            HtmlGenerator.generateHtml(documentation, "docs");

            System.out.println("\nProcessed " + luaFiles.size() + " file(s) successfully!");

        } catch (Exception e) {
            System.err.println("Error generating documentation: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Recursively finds all .lua files in the given directory and subdirectories.
     * @param directory The directory to search
     * @return List of absolute file paths
     * @throws IOException if there's an error reading the directory
     */
    private static List<String> findLuaFiles(Path directory) throws IOException {
        List<String> luaFiles = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(directory)) {
            paths.filter(Files::isRegularFile)
                 .filter(path -> path.toString().toLowerCase().endsWith(".lua"))
                 .map(Path::toAbsolutePath)
                 .map(Path::toString)
                 .sorted()
                 .forEach(luaFiles::add);
        }

        return luaFiles;
    }
}
