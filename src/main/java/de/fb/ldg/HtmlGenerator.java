package de.fb.ldg;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Generator für HTML-Dokumentationsseiten aus Lua-Code.
 */
public class HtmlGenerator {

    private static final String CSS_STYLES = """
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            line-height: 1.6;
            margin: 0;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
            background: white;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        h1 { color: #2c3e50; border-bottom: 3px solid #3498db; padding-bottom: 10px; }
        h2 { color: #34495e; border-bottom: 2px solid #ecf0f1; padding-bottom: 5px; }
        h3 { color: #7f8c8d; }
        .class-header { background: #3498db; color: white; padding: 15px; border-radius: 5px; margin: 20px 0; }
        .inheritance { 
            background: #e8f4fd; 
            color: #2980b9; 
            padding: 8px 12px; 
            border-radius: 5px; 
            border-left: 4px solid #3498db;
            margin: 10px 0;
            font-weight: bold;
        }
        .inheritance a {
            color: #2980b9;
            text-decoration: none;
            font-weight: bold;
        }
        .inheritance a:hover {
            text-decoration: underline;
        }
        .field, .method, .variable { 
            background: #ecf0f1; 
            padding: 10px; 
            margin: 10px 0; 
            border-left: 4px solid #3498db; 
            border-radius: 3px;
        }
        .static { border-left-color: #e74c3c; }
        .private { border-left-color: #f39c12; }
        .local { border-left-color: #27ae60; }
        .type { color: #8e44ad; font-weight: bold; }
        .param-list { margin: 5px 0; }
        .param { color: #2980b9; }
        .description { color: #555; margin-top: 5px; }
        .navigation {
            background: #34495e;
            padding: 15px;
            margin: -30px -30px 30px -30px;
            border-radius: 8px 8px 0 0;
        }
        .navigation a {
            color: #ecf0f1;
            text-decoration: none;
            margin-right: 20px;
            padding: 5px 10px;
            border-radius: 3px;
            transition: background 0.3s;
        }
        .navigation a:hover {
            background: #2c3e50;
        }
        .overview-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 20px;
            margin: 20px 0;
        }
        .overview-card {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 5px;
            border-left: 4px solid #3498db;
        }
        .overview-card h3 {
            margin-top: 0;
            color: #2c3e50;
        }
        .overview-list {
            list-style: none;
            padding: 0;
        }
        .overview-list li {
            padding: 5px 0;
            border-bottom: 1px solid #ecf0f1;
        }
        .overview-list li:last-child {
            border-bottom: none;
        }
        .overview-list a {
            color: #3498db;
            text-decoration: none;
        }
        .overview-list a:hover {
            text-decoration: underline;
        }
        .class-link {
            color: #2980b9;
            text-decoration: none;
            font-weight: bold;
        }
        .class-link:hover {
            text-decoration: underline;
        }
        .class-hierarchy {
            margin: 0;
            padding: 0;
            list-style: none;
        }
        .class-hierarchy > li {
            margin: 8px 0;
            padding: 0;
            border-bottom: 1px solid #ecf0f1;
        }
        .class-hierarchy > li:last-child {
            border-bottom: none;
        }
        .parent-class {
            padding: 8px 0;
        }
        .parent-class a {
            text-decoration: none;
        }
        .parent-class a:hover {
            text-decoration: underline;
        }
        .subclass-list {
            padding: 0;
            list-style: none;
            background: none;
            border-radius: 0;
            display: block;
            margin-left: 20px;
            line-height: 1.8;
        }
        .subclass-list li {
            margin: 0;
            padding: 0;
            border-bottom: none;
            display: inline;
        }
        .subclass-list li:after {
            content: ", ";
            color: #6c757d;
        }
        .subclass-list li:last-child:after {
            content: "";
        }
        .subclass-list a {
            text-decoration: none;
            font-size: 0.85em;
            font-weight: normal;
        }
        .subclass-list a:hover {
            text-decoration: underline;
        }
        .code-examples {
            background: #f8f9fa;
            border: 1px solid #e9ecef;
            border-radius: 4px;
            padding: 15px;
            margin: 10px 0;
        }
        .code-examples h4 {
            color: #495057;
            margin-top: 0;
            margin-bottom: 10px;
            font-size: 0.9em;
            font-weight: bold;
        }
        .code-example {
            background: #2d3748;
            color: #e2e8f0;
            padding: 12px;
            border-radius: 4px;
            font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
            font-size: 0.85em;
            margin: 8px 0;
            overflow-x: auto;
            white-space: pre;
        }
        .see-also {
            background: #f8f9fa;
            border: 1px solid #e9ecef;
            border-radius: 4px;
            padding: 15px;
            margin: 10px 0;
        }
        .see-also h4 {
            color: #495057;
            margin-top: 0;
            margin-bottom: 10px;
            font-size: 0.9em;
            font-weight: bold;
        }
        .see-also ul {
            margin: 0;
            padding-left: 20px;
        }
        .see-also li {
            margin: 5px 0;
        }
        .see-also a {
            color: #2980b9;
            text-decoration: none;
            font-weight: bold;
        }
        .see-also a:hover {
            text-decoration: underline;
        }
        .see-description {
            color: #6c757d;
            font-weight: normal;
            margin-left: 5px;
        }
        """;

    /**
     * Generates HTML documentation for the given Documentation.
     * @param documentation The documentation structure to generate HTML for
     * @param outputDir The output directory for HTML files
     */
    public static void generateHtml(Documentation documentation, String outputDir) {
        try {
            Path outputPath = Paths.get(outputDir);
            Files.createDirectories(outputPath);

            // Generate overview page
            generateOverviewPage(documentation, outputPath);

            // Generate class pages
            for (Class clazz : documentation.classes) {
                generateClassPage(clazz, documentation, outputPath);
            }

            // Generate global functions and variables page
            if (!documentation.functions.isEmpty() || !documentation.variables.isEmpty()) {
                generateGlobalsPage(documentation, outputPath);
            }

            System.out.println("HTML documentation successfully generated in: " + outputPath.toAbsolutePath());

        } catch (IOException e) {
            throw new RuntimeException("Error generating HTML documentation", e);
        }
    }

    private static void generateOverviewPage(Documentation documentation, Path outputPath) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append(getHtmlHeader("Lua Documentation - Overview", ""));

        html.append("<div class=\"container\">\n");
        html.append(getNavigationBar(""));

        html.append("<h1>Lua Documentation - Overview</h1>\n");

        html.append("<div class=\"overview-grid\">\n");

        // Classes overview - hierarchical structure
        if (!documentation.classes.isEmpty()) {
            html.append("<div class=\"overview-card\">\n");
            html.append("<h3>Classes</h3>\n");
            html.append(generateClassHierarchy(documentation.classes));
            html.append("</div>\n");
        }

        // Global functions and variables
        if (!documentation.functions.isEmpty() || !documentation.variables.isEmpty()) {
            html.append("<div class=\"overview-card\">\n");
            html.append("<h3>Global Elements</h3>\n");
            html.append("<ul class=\"overview-list\">\n");
            html.append("<li><a href=\"globals.html\">Global Functions and Variables</a></li>\n");
            html.append("</ul>\n");
            html.append("</div>\n");
        }

        html.append("</div>\n");

        html.append("</div>\n");
        html.append("</body></html>");

        Files.writeString(outputPath.resolve("index.html"), html.toString());
    }

    /**
     * Generates a hierarchical view of classes, grouping by namespace.
     */
    private static String generateClassHierarchy(List<Class> classes) {
        StringBuilder html = new StringBuilder();
        html.append("<ul class=\"class-hierarchy\">\n");

        // Group classes by parent namespace
        java.util.Map<String, java.util.List<Class>> classGroups = new java.util.HashMap<>();
        java.util.Set<String> processedClasses = new java.util.HashSet<>();

        for (Class clazz : classes) {
            String className = clazz.name;

            // Check if this is a nested class (contains dot)
            if (className.contains(".")) {
                String[] parts = className.split("\\.");
                if (parts.length >= 2) {
                    String parentName = parts[0];

                    // Find if parent class exists
                    boolean parentExists = classes.stream().anyMatch(c -> c.name.equals(parentName));

                    if (parentExists) {
                        classGroups.computeIfAbsent(parentName, k -> new java.util.ArrayList<>()).add(clazz);
                        processedClasses.add(className);
                    }
                }
            }
        }

        // First, display parent classes with their children
        for (Class clazz : classes) {
            String className = clazz.name;

            // Skip if this class is already processed as a child
            if (processedClasses.contains(className)) {
                continue;
            }

            // Check if this class has children
            if (classGroups.containsKey(className)) {
                html.append("<li>\n");
                html.append(String.format("<div class=\"parent-class\"><a href=\"%s.html\">%s</a></div>\n",
                    sanitizeFileName(className), className));

                html.append("<ul class=\"subclass-list\">\n");
                for (Class subclass : classGroups.get(className)) {
                    html.append(String.format("<li><a href=\"%s.html\">%s</a></li>\n",
                        sanitizeFileName(subclass.name), subclass.name));
                }
                html.append("</ul>\n");
                html.append("</li>\n");
            } else {
                // Standalone class
                html.append(String.format("<li class=\"standalone-class\"><a href=\"%s.html\">%s</a></li>\n",
                    sanitizeFileName(className), className));
            }
        }

        html.append("</ul>\n");
        return html.toString();
    }

    private static void generateClassPage(Class clazz, Documentation documentation, Path outputPath) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append(getHtmlHeader("Class " + clazz.name, "../"));

        html.append("<div class=\"container\">\n");
        html.append(getNavigationBar(""));

        html.append(String.format("<div class=\"class-header\">\n<h1>Class %s</h1>\n", clazz.name));
        html.append("</div>\n");

        // Inheritance information
        if (!clazz.parent.isEmpty()) {
            html.append("<div class=\"inheritance\">\n");
            html.append("Extends: ");
            for (int i = 0; i < clazz.parent.size(); i++) {
                if (i > 0) html.append(", ");
                String parentName = clazz.parent.get(i);
                if (classExists(documentation, parentName)) {
                    html.append(String.format("<a href=\"%s.html\" class=\"class-link\">%s</a>",
                        parentName, parentName));
                } else {
                    html.append(parentName);
                }
            }
            html.append("\n</div>\n");
        }

        // Fields
        if (!clazz.fields.isEmpty()) {
            html.append("<h2>Fields</h2>\n");
            for (Field field : clazz.fields) {
                String cssClass = field.visibility.equals("private") ? "field private" : "field";
                html.append(String.format("<div class=\"%s\">\n", cssClass));
                html.append(String.format("<strong>%s</strong>: <span class=\"type\">%s</span>\n",
                    field.name, linkifyType(field.type, documentation)));

                // Add modifiers in documentation section
                if (!field.visibility.isEmpty()) {
                    html.append(String.format("<div><strong>Visibility:</strong> %s</div>\n", field.visibility));
                }

                if (!field.description.isEmpty()) {
                    html.append(String.format("<div class=\"description\">%s</div>\n", field.description));
                }
                html.append("</div>\n");
            }
        }

        // Methods
        if (!clazz.functions.isEmpty()) {
            html.append("<h2>Methods</h2>\n");
            for (Function function : clazz.functions) {
                String cssClass = function.isStatic ? "method static" : "method";
                html.append(String.format("<div class=\"%s\" id=\"%s\">\n", cssClass, sanitizeFunctionId(function.name)));

                html.append("<strong>");
                html.append(function.name).append("(");

                for (int i = 0; i < function.parameters.size(); i++) {
                    if (i > 0) html.append(", ");
                    Function.Parameter param = function.parameters.get(i);
                    html.append(String.format("<span class=\"param\">%s: %s</span>",
                        param.name, linkifyType(param.type, documentation)));
                }
                html.append(")");

                if (function.returnType != null && !function.returnType.isEmpty()) {
                    html.append(": <span class=\"type\">").append(linkifyType(function.returnType, documentation)).append("</span>");
                }
                html.append("</strong>\n");

                // Add modifiers in documentation section
                if (function.isStatic) {
                    html.append("<div><strong>Modifier:</strong> static</div>\n");
                }

                if (function.nodiscard) {
                    html.append("<div><strong>Note:</strong> @nodiscard - Return value should not be ignored</div>\n");
                }

                if (!function.description.isEmpty()) {
                    html.append(String.format("<div class=\"description\">%s</div>\n", function.description));
                }

                if (!function.parameters.isEmpty()) {
                    html.append("<div class=\"param-list\"><strong>Parameters:</strong><ul>\n");
                    for (Function.Parameter param : function.parameters) {
                        html.append(String.format("<li><span class=\"param\">%s</span> (<span class=\"type\">%s</span>): %s</li>\n",
                            param.name, linkifyType(param.type, documentation), param.description));
                    }
                    html.append("</ul></div>\n");
                }

                if (function.returnType != null && !function.returnType.isEmpty() &&
                    !function.returnDescription.isEmpty()) {
                    html.append(String.format("<div><strong>Returns:</strong> %s</div>\n",
                        function.returnDescription));
                }

                // Add code examples if present
                if (!function.examples.isEmpty()) {
                    html.append(generateCodeExamples(function.examples));
                }

                // Add @see references if present
                if (!function.seeReferences.isEmpty()) {
                    html.append(generateSeeAlsoSection(function.seeReferences, documentation));
                }

                html.append("</div>\n");
            }
        }

        // See Also section for class
        if (!clazz.seeReferences.isEmpty()) {
            html.append(generateSeeAlsoSection(clazz.seeReferences, documentation));
        }

        html.append("</div>\n");
        html.append("</body></html>");

        Files.writeString(outputPath.resolve(sanitizeFileName(clazz.name) + ".html"), html.toString());
    }

    private static void generateGlobalsPage(Documentation documentation, Path outputPath) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append(getHtmlHeader("Global Functions and Variables", ""));

        html.append("<div class=\"container\">\n");
        html.append(getNavigationBar(""));

        html.append("<h1>Global Functions and Variables</h1>\n");

        // Global Functions
        if (!documentation.functions.isEmpty()) {
            html.append("<h2>Global Functions</h2>\n");
            for (Function function : documentation.functions) {
                html.append(String.format("<div class=\"method\" id=\"%s\">\n", sanitizeFunctionId(function.name)));

                html.append(String.format("<strong>%s(", function.name));
                for (int i = 0; i < function.parameters.size(); i++) {
                    if (i > 0) html.append(", ");
                    Function.Parameter param = function.parameters.get(i);
                    html.append(String.format("<span class=\"param\">%s: %s</span>",
                        param.name, linkifyType(param.type, documentation)));
                }
                html.append(")");

                if (function.returnType != null && !function.returnType.isEmpty()) {
                    html.append(": <span class=\"type\">").append(linkifyType(function.returnType, documentation)).append("</span>");
                }
                html.append("</strong>\n");

                if (!function.description.isEmpty()) {
                    html.append(String.format("<div class=\"description\">%s</div>\n", function.description));
                }

                if (!function.parameters.isEmpty()) {
                    html.append("<div class=\"param-list\"><strong>Parameters:</strong><ul>\n");
                    for (Function.Parameter param : function.parameters) {
                        html.append(String.format("<li><span class=\"param\">%s</span> (<span class=\"type\">%s</span>): %s</li>\n",
                            param.name, linkifyType(param.type, documentation), param.description));
                    }
                    html.append("</ul></div>\n");
                }

                if (function.returnType != null && !function.returnType.isEmpty() &&
                    !function.returnDescription.isEmpty()) {
                    html.append(String.format("<div><strong>Returns:</strong> %s</div>\n",
                        function.returnDescription));
                }

                // Add code examples if present
                if (!function.examples.isEmpty()) {
                    html.append(generateCodeExamples(function.examples));
                }

                // Add @see references if present
                if (!function.seeReferences.isEmpty()) {
                    html.append(generateSeeAlsoSection(function.seeReferences, documentation));
                }

                html.append("</div>\n");
            }
        }

        // Variables
        if (!documentation.variables.isEmpty()) {
            html.append("<h2>Variables</h2>\n");
            for (Variable variable : documentation.variables) {
                String cssClass = variable.isLocal ? "variable local" : "variable";
                html.append(String.format("<div class=\"%s\">\n", cssClass));

                html.append("<strong>");
                html.append(variable.name).append("</strong>: <span class=\"type\">")
                    .append(linkifyType(variable.type, documentation)).append("</span>\n");

                // Add modifiers in documentation section
                if (variable.isLocal) {
                    html.append("<div><strong>Scope:</strong> local</div>\n");
                } else {
                    html.append("<div><strong>Scope:</strong> global</div>\n");
                }

                if (!variable.description.isEmpty()) {
                    html.append(String.format("<div class=\"description\">%s</div>\n", variable.description));
                }

                html.append("</div>\n");
            }
        }

        html.append("</div>\n");
        html.append("</body></html>");

        Files.writeString(outputPath.resolve("globals.html"), html.toString());
    }

    private static String getHtmlHeader(String title, String relativePath) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
                <style>%s</style>
            </head>
            <body>
            """, title, CSS_STYLES);
    }

    private static String getNavigationBar(String relativePath) {
        return String.format("""
            <div class="navigation">
                <a href="%sindex.html">🏠 Overview</a>
                <a href="%sglobals.html">🌐 Global Elements</a>
            </div>
            """, relativePath, relativePath);
    }

    private static boolean classExists(Documentation documentation, String className) {
        for (Class clazz : documentation.classes) {
            if (clazz.name.equals(className)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts a type string to HTML with potential class links.
     * @param type the type string (e.g. "MyDocumentedClass", "string?", "MyClass|number")
     * @param documentation the documentation to check for existing classes
     * @return HTML string with linked types where applicable
     */
    private static String linkifyType(String type, Documentation documentation) {
        if (type == null || type.isEmpty()) {
            return type;
        }

        // First escape all HTML characters in the type
        type = escapeHtml(type);

        // Handle union types (e.g., "string|MyClass|number")
        if (type.contains("|")) {
            String[] parts = type.split("\\|");
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) result.append("|");
                result.append(linkifyTypeEscaped(parts[i].trim(), documentation));
            }
            return result.toString();
        }

        return linkifyTypeEscaped(type, documentation);
    }

    private static String linkifyTypeEscaped(String type, Documentation documentation) {
        // Handle optional types (e.g., "MyClass?")
        boolean isOptional = type.endsWith("?");
        String baseType = isOptional ? type.substring(0, type.length() - 1) : type;

        // Handle array types (e.g., "MyClass[]")
        boolean isArray = baseType.endsWith("[]");
        if (isArray) {
            baseType = baseType.substring(0, baseType.length() - 2);
        }

        // Handle generic types (e.g., "List&lt;MyClass&gt;", "table&lt;string, any&gt;")
        if (baseType.contains("&lt;") && baseType.contains("&gt;")) {
            int start = baseType.indexOf("&lt;");
            int end = baseType.lastIndexOf("&gt;");
            String containerType = baseType.substring(0, start);
            String innerType = baseType.substring(start + 4, end); // +4 for "&lt;" length

            // Unescape the container type for class checking
            String unescapedContainer = unescapeHtml(containerType);
            String linkedContainer = classExists(documentation, unescapedContainer) ?
                String.format("<a href=\"%s.html\" class=\"class-link\">%s</a>", sanitizeFileName(unescapedContainer), containerType) :
                containerType;

            // Handle comma-separated types in generics (e.g., "string, any" in "table&lt;string, any&gt;")
            String linkedInner;
            if (innerType.contains(",")) {
                // Split by comma and process each type separately
                String[] innerTypes = innerType.split(",");
                StringBuilder innerResult = new StringBuilder();
                for (int i = 0; i < innerTypes.length; i++) {
                    if (i > 0) innerResult.append(", ");
                    String trimmedInner = innerTypes[i].trim();
                    String unescapedInner = unescapeHtml(trimmedInner);
                    if (classExists(documentation, unescapedInner)) {
                        innerResult.append(String.format("<a href=\"%s.html\" class=\"class-link\">%s</a>",
                            sanitizeFileName(unescapedInner), trimmedInner));
                    } else {
                        innerResult.append(trimmedInner);
                    }
                }
                linkedInner = innerResult.toString();
            } else {
                String unescapedInner = unescapeHtml(innerType);
                linkedInner = classExists(documentation, unescapedInner) ?
                    String.format("<a href=\"%s.html\" class=\"class-link\">%s</a>", sanitizeFileName(unescapedInner), innerType) :
                    innerType;
            }

            baseType = linkedContainer + "&lt;" + linkedInner + "&gt;";
        } else {
            // Simple type - check if it's a class (including nested classes with dots)
            String unescapedType = unescapeHtml(baseType);
            if (classExists(documentation, unescapedType)) {
                baseType = String.format("<a href=\"%s.html\" class=\"class-link\">%s</a>", sanitizeFileName(unescapedType), baseType);
            }
        }

        // Reconstruct the type with modifiers
        String result = baseType;
        if (isArray) {
            result += "[]";
        }
        if (isOptional) {
            result += "?";
        }

        return result;
    }

    /**
     * Escapes HTML special characters in a string.
     */
    private static String escapeHtml(String text) {
        if (text == null) return null;
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    /**
     * Unescapes HTML entities back to normal characters for class name checking.
     */
    private static String unescapeHtml(String text) {
        if (text == null) return null;
        return text.replace("&amp;", "&")
                   .replace("&lt;", "<")
                   .replace("&gt;", ">")
                   .replace("&quot;", "\"")
                   .replace("&#39;", "'");
    }

    private static String sanitizeFileName(String className) {
        // Replace all non-alphanumeric characters with underscores
        return className.replaceAll("[^a-zA-Z0-9]", "_");
    }

    /**
     * Generates HTML for code examples.
     * @param examples The list of code examples to include
     * @return HTML string for the code examples section
     */
    private static String generateCodeExamples(List<String> examples) {
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"code-examples\">\n");
        html.append("<h4>Code Examples</h4>\n");

        for (String example : examples) {
            html.append("<div class=\"code-example\">");
            html.append(escapeHtml(example));
            html.append("</div>\n");
        }

        html.append("</div>\n");
        return html.toString();
    }

    /**
     * Generates the "See Also" section for a class.
     * @param seeReferences The list of references for the "See Also" section
     * @param documentation The documentation structure to link to
     * @return HTML string for the "See Also" section
     */
    private static String generateSeeAlsoSection(List<SeeReference> seeReferences, Documentation documentation) {
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"see-also\">\n");
        html.append("<h4>See Also</h4>\n");
        html.append("<ul>\n");
        for (SeeReference ref : seeReferences) {
            String linkHtml = generateReferenceLink(ref, documentation);
            html.append(String.format("<li>%s", linkHtml));

            // Add description if present
            if (!ref.description.isEmpty()) {
                html.append(String.format("<span class=\"see-description\">- %s</span>", escapeHtml(ref.description)));
            }

            html.append("</li>\n");
        }
        html.append("</ul>\n");
        html.append("</div>\n");
        return html.toString();
    }

    /**
     * Generates a link for a see reference based on its type.
     * @param ref The see reference to generate a link for
     * @param documentation The documentation structure to check against
     * @return HTML string with link if possible, otherwise just the name
     */
    private static String generateReferenceLink(SeeReference ref, Documentation documentation) {
        String name = ref.referenceName;

        switch (ref.type) {
            case CLASS:
                if (classExists(documentation, name)) {
                    return String.format("<a href=\"%s.html\">%s</a>", sanitizeFileName(name), name);
                }
                break;

            case GLOBAL_FUNCTION:
                if (globalFunctionExists(documentation, name)) {
                    return String.format("<a href=\"globals.html#%s\">%s</a>", sanitizeFunctionId(name), name);
                }
                break;

            case FUNCTION:
            case METHOD:
                // For methods, try to find the class and create a link to the class page with anchor
                String[] parts = name.split("\\.");
                if (parts.length >= 2) {
                    String className = parts[0];
                    String methodName = parts[1];
                    if (classExists(documentation, className)) {
                        return String.format("<a href=\"%s.html#%s\">%s</a>",
                            sanitizeFileName(className), sanitizeFunctionId(methodName), name);
                    }
                } else {
                    // Could be a global function
                    if (globalFunctionExists(documentation, name)) {
                        return String.format("<a href=\"globals.html#%s\">%s</a>", sanitizeFunctionId(name), name);
                    }
                }
                break;
        }

        // If no link can be created, just return the name
        return name;
    }

    /**
     * Checks if a global function exists in the documentation.
     * @param documentation The documentation to search
     * @param functionName The name of the function to find
     * @return true if the function exists, false otherwise
     */
    private static boolean globalFunctionExists(Documentation documentation, String functionName) {
        return documentation.functions.stream()
            .anyMatch(function -> function.name.equals(functionName));
    }

    /**
     * Sanitizes a function name for use as an HTML anchor ID.
     * @param functionName The function name to sanitize
     * @return A sanitized string suitable for use as an HTML ID
     */
    private static String sanitizeFunctionId(String functionName) {
        return functionName.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
