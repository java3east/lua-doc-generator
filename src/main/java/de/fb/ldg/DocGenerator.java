package de.fb.ldg;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocGenerator {

    private static class DocBlock {
        boolean hadContent = false;
        boolean expectFunction = false;
        boolean hasClassOrFieldTags = false;
        boolean hasTypeTag = false; // Für @type Annotations

        List<String> lines = new ArrayList<>();
        String description = "";
        List<String> examples = new ArrayList<>(); // Code-Beispiele
        List<SeeReference> seeReferences = new ArrayList<>(); // @see Referenzen
        String className = "";
        List<String> classParents = new ArrayList<>();
        List<Field> fields = new ArrayList<>();
        String functionName = "";
        String functionClassName = ""; // Klasse zu der die Funktion gehört
        boolean isStatic = false; // Ob die Funktion statisch ist
        List<Function.Parameter> parameters = new ArrayList<>();
        String returnType = "";
        String returnDescription = "";
        boolean nodiscard = false;

        // Variable-spezifische Felder
        String variableName = "";
        String variableType = "";
        String variableDescription = "";
        boolean isLocalVariable = false;

        boolean inCodeBlock = false; // Ob wir uns in einem Codeblock befinden (zwischen ```)

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[BLOCK_TYPE=").append(hasClassOrFieldTags ? "CLASS" : "FUNCTION").append("]\n");
            for (String line : lines) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }

    public static Documentation generate(String content) {
        Documentation documentation = new Documentation();
        List<DocBlock> blocks = new ArrayList<>();
        DocBlock block = new DocBlock();

        int lineNumber = 1;
        String[] lines = content.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (isDocLine(line)) {
                processDocLine(line, block);
                block.lines.add(line);
            } else if (block.hadContent) {
                processCodeLine(line, block, lineNumber);
                blocks.add(block);
                block = new DocBlock();
            }

            lineNumber++;
        }

        // Zuerst alle Klassen sammeln
        for (DocBlock b : blocks) {
            if (b.hasClassOrFieldTags) {
                processClassBlock(b, documentation);
            }
        }

        // Dann Funktionen verarbeiten und den Klassen zuordnen
        for (DocBlock b : blocks) {
            if (b.expectFunction) {
                processFunctionBlock(b, documentation);
            }
        }

        // Dann Variablen verarbeiten - dabei auch Felder zu Klassen zuordnen
        for (DocBlock b : blocks) {
            if (b.hasTypeTag) {
                processVariableBlock(b, documentation);
            }
        }

        // Zusätzlich: Standalone @field Blöcke verarbeiten
        processStandaloneFields(blocks, documentation);

        return documentation;
    }

    private static void processClassBlock(DocBlock block, Documentation documentation) {
        if (!block.className.isEmpty()) {
            // Debug output to see what's happening
            System.out.println("Processing class: " + block.className + " with " + block.fields.size() + " fields");
            for (Field field : block.fields) {
                System.out.println("  Field: " + field.name + " (" + field.type + ")");
            }

            // Create class with fields from the same block and see references
            Class clazz = new Class(block.className, block.classParents, new ArrayList<>(block.fields), new ArrayList<>(), block.seeReferences);
            documentation.addClass(clazz);
        }
    }

    private static void processFunctionBlock(DocBlock block, Documentation documentation) {
        if (!block.functionName.isEmpty()) {
            // Skip local functions - they are not part of the public API
            if (isLocalFunction(block)) {
                return;
            }

            Function function = new Function(
                block.functionName,
                block.description,
                block.parameters,
                block.returnType,
                block.returnDescription,
                block.nodiscard,
                block.isStatic,
                block.examples,
                block.seeReferences
            );

            // Prüfen, ob die Funktion zu einer Klasse gehört
            if (!block.functionClassName.isEmpty()) {
                // Funktion zur entsprechenden Klasse hinzufügen
                Class targetClass = findClassByName(documentation, block.functionClassName);
                if (targetClass != null) {
                    targetClass.functions.add(function);
                } else {
                    // Wenn Klasse nicht gefunden, als standalone Funktion hinzufügen
                    documentation.addFunction(function);
                }
            } else {
                // Standalone Funktion
                documentation.addFunction(function);
            }
        }
    }

    private static void processVariableBlock(DocBlock block, Documentation documentation) {
        if (!block.variableName.isEmpty()) {
            // Skip local variables - they are not part of the public API
            if (block.isLocalVariable) {
                return;
            }

            // Check if this variable name matches a class name (for nested classes)
            // If we have a @class tag and the variable name matches, don't process as variable
            if (!block.className.isEmpty() && block.variableName.equals(block.className)) {
                // This is a class assignment, not a variable - skip it
                return;
            }

            // Check if this is a class field assignment (e.g., Config.my_value)
            if (block.variableName.contains(".")) {
                String[] parts = block.variableName.split("\\.", 2);
                String className = parts[0];
                String fieldName = parts[1];

                // Skip assignments to nested objects (e.g., Config.General.framework)
                // These are value assignments to already defined nested classes, not field definitions
                if (fieldName.contains(".")) {
                    // This is a nested assignment like Config.General.framework = "value"
                    // Skip it as it's not a field definition but a value assignment
                    return;
                }

                // Find the class and add this as a field
                Class targetClass = findClassByName(documentation, className);
                if (targetClass != null) {
                    // Create a field for this class property
                    Field field = new Field(fieldName, block.variableType, "public", block.variableDescription);
                    targetClass.fields.add(field);
                    return; // Don't add as global variable
                }
            }

            Variable variable = new Variable(block.variableName, block.variableType, block.variableDescription, block.isLocalVariable);
            documentation.addVariable(variable);
        }
    }

    private static Class findClassByName(Documentation documentation, String className) {
        for (Class clazz : documentation.classes) {
            if (clazz.name.equals(className)) {
                return clazz;
            }
        }
        return null;
    }

    private static boolean isDocLine(String line) {
        return line.trim().startsWith("---");
    }

    private static void processDocLine(String line, DocBlock block) {
        updateBlockFlags(line, block);
        parseDocContent(line, block);
        block.hadContent = true;
    }

    private static void parseDocContent(String line, DocBlock block) {
        line = line.trim();

        if (line.startsWith("---@class")) {
            parseClassLine(line, block);
        } else if (line.startsWith("---@field")) {
            parseFieldLine(line, block);
        } else if (line.startsWith("---@param")) {
            parseParamLine(line, block);
        } else if (line.startsWith("---@return")) {
            parseReturnLine(line, block);
        } else if (line.startsWith("---@nodiscard")) {
            block.nodiscard = true;
        } else if (line.startsWith("---") && !line.startsWith("---@")) {
            // Plain description line - check for code blocks
            String desc = line.substring(3).trim();
            parseDescriptionWithCodeBlocks(desc, block);
        } else if (line.startsWith("---@type")) {
            parseTypeLine(line, block);
        } else if (line.startsWith("---@see")) {
            parseSeeLine(line, block);
        }
    }

    private static void parseDescriptionWithCodeBlocks(String desc, DocBlock block) {
        // Check if this line starts or ends a code block
        if (desc.startsWith("```")) {
            if (block.inCodeBlock) {
                // End of code block
                block.inCodeBlock = false;
                // Don't add the closing ``` to examples
            } else {
                // Start of code block
                block.inCodeBlock = true;
                // Don't add the opening ``` to examples
            }
        } else if (block.inCodeBlock) {
            // We're inside a code block, add this line to examples
            if (!desc.isEmpty()) {
                block.examples.add(desc);
            }
        } else {
            // Normal description line
            if (!desc.isEmpty()) {
                if (!block.description.isEmpty()) {
                    block.description += " ";
                }
                block.description += desc;
            }
        }
    }

    private static void parseClassLine(String line, DocBlock block) {
        // Pattern: ---@class ClassName : ParentClass description
        // Updated to support complex parent types like Parent<{field: type}>
        String content = line.trim().replaceFirst("^---@class\\s+", "");

        // Split by first whitespace to get class name
        String[] firstSplit = content.split("\\s+", 2);
        if (firstSplit.length == 0) return;

        String classNamePart = firstSplit[0];
        String remaining = firstSplit.length > 1 ? firstSplit[1] : "";

        // Check if class name contains inheritance separator ':'
        if (classNamePart.contains(":")) {
            String[] classParts = classNamePart.split(":", 2);
            block.className = classParts[0].trim();

            // Parse the parent type (could be complex) + remaining description
            String parentAndDesc = classParts[1].trim();
            if (!remaining.isEmpty()) {
                parentAndDesc += " " + remaining;
            }

            // Use the same parsing logic as for types to handle complex parent types
            String[] parentTypeAndDesc = parseTypeAndDescription(parentAndDesc);
            String parentType = parentTypeAndDesc[0];
            String description = parentTypeAndDesc[1];

            if (!parentType.isEmpty()) {
                block.classParents.add(parentType);
            }
            if (!description.isEmpty()) {
                block.description = description;
            }
        } else {
            // No inheritance, check if remaining part starts with ':'
            block.className = classNamePart;

            if (!remaining.isEmpty() && remaining.startsWith(":")) {
                // Remove the ':' and parse parent type + description
                String parentAndDesc = remaining.substring(1).trim();
                String[] parentTypeAndDesc = parseTypeAndDescription(parentAndDesc);
                String parentType = parentTypeAndDesc[0];
                String description = parentTypeAndDesc[1];

                if (!parentType.isEmpty()) {
                    block.classParents.add(parentType);
                }
                if (!description.isEmpty()) {
                    block.description = description;
                }
            } else if (!remaining.isEmpty()) {
                // Just description, no inheritance
                block.description = remaining;
            }
        }
    }

    private static void parseFieldLine(String line, DocBlock block) {
        // Remove the @field prefix
        String content = line.trim().replaceFirst("^---@field\\s+", "");

        // Split by first whitespace to get field name
        String[] firstSplit = content.split("\\s+", 2);
        if (firstSplit.length < 2) return;

        String fieldName = firstSplit[0];
        String remaining = firstSplit[1];

        // Check if field name includes visibility modifier
        String visibility = "public"; // Default
        String actualFieldName = fieldName;

        if (fieldName.equals("public") || fieldName.equals("private") || fieldName.equals("protected")) {
            // Visibility modifier present
            String[] secondSplit = remaining.split("\\s+", 2);
            if (secondSplit.length < 2) return;

            visibility = fieldName;
            actualFieldName = secondSplit[0];
            remaining = secondSplit[1];
        }

        // Parse type and description
        String[] typeAndDesc = parseTypeAndDescription(remaining);
        String fieldType = typeAndDesc[0];
        String description = typeAndDesc[1];

        Field field = new Field(actualFieldName, fieldType, visibility, description);
        block.fields.add(field);

        // Debug output
        System.out.println("Parsed field: " + actualFieldName + " (" + fieldType + ") - " + description);
    }

    private static void parseParamLine(String line, DocBlock block) {
        // Pattern: ---@param paramName type description
        // Updated to handle union types like 'esx'|'qb' and complex types
        String content = line.trim().replaceFirst("^---@param\\s+", "");

        // Split by first whitespace to get parameter name
        String[] firstSplit = content.split("\\s+", 2);
        if (firstSplit.length < 2) return;

        String paramName = firstSplit[0];
        String remaining = firstSplit[1];

        // Parse type (can contain unions, generics, etc.)
        String[] typeAndDesc = parseTypeAndDescription(remaining);
        String paramType = typeAndDesc[0];
        String description = typeAndDesc[1];

        Function.Parameter param = new Function.Parameter(paramName, paramType, description);
        block.parameters.add(param);
    }

    private static void parseReturnLine(String line, DocBlock block) {
        // Pattern: ---@return type description
        // Updated to handle union types and complex types
        String content = line.trim().replaceFirst("^---@return\\s+", "");

        String[] typeAndDesc = parseTypeAndDescription(content);
        block.returnType = typeAndDesc[0];
        block.returnDescription = typeAndDesc[1];
    }

    private static void parseTypeLine(String line, DocBlock block) {
        // Pattern: ---@type type description
        // Updated to handle union types and complex types
        String content = line.trim().replaceFirst("^---@type\\s+", "");

        String[] typeAndDesc = parseTypeAndDescription(content);
        block.variableType = typeAndDesc[0];
        block.variableDescription = typeAndDesc[1];
        block.hasTypeTag = true;
    }

    private static void parseSeeLine(String line, DocBlock block) {
        // Pattern: ---@see ClassName [description]
        String content = line.trim().replaceFirst("^---@see\\s+", "");

        if (!content.isEmpty()) {
            // Split by first whitespace to separate reference name from description
            String[] parts = content.split("\\s+", 2);
            String referenceName = parts[0];
            String description = parts.length > 1 ? parts[1] : "";

            // Determine the type of reference based on the name pattern
            SeeReference.ReferenceType type = determineReferenceType(referenceName);
            SeeReference ref = new SeeReference(referenceName, type, description);
            block.seeReferences.add(ref);
        }
    }

    /**
     * Determines the type of a @see reference based on its name pattern.
     * @param referenceName The name of the reference
     * @return The determined reference type
     */
    private static SeeReference.ReferenceType determineReferenceType(String referenceName) {
        // If it contains a dot, it's likely a method (ClassName.methodName)
        if (referenceName.contains(".")) {
            return SeeReference.ReferenceType.METHOD;
        }

        // Check if it starts with lowercase (likely a function)
        if (!referenceName.isEmpty() && Character.isLowerCase(referenceName.charAt(0))) {
            return SeeReference.ReferenceType.GLOBAL_FUNCTION;
        }

        // Default to class for uppercase names
        return SeeReference.ReferenceType.CLASS;
    }

    /**
     * Parses a type and description from a string, handling union types, generics, and quoted strings.
     * @param input The input string containing type and optional description
     * @return Array with [type, description]
     */
    private static String[] parseTypeAndDescription(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new String[]{"", ""};
        }

        input = input.trim();
        StringBuilder typeBuilder = new StringBuilder();
        boolean inQuotes = false;
        int inGeneric = 0; // Track nesting levels for < >
        int inBraces = 0;  // Track nesting levels for { }
        char quoteChar = '\0';
        int i = 0;

        // Parse the type part character by character
        while (i < input.length()) {
            char c = input.charAt(i);

            if (!inQuotes && (c == '\'' || c == '"')) {
                // Start of quoted string
                inQuotes = true;
                quoteChar = c;
                typeBuilder.append(c);
            } else if (inQuotes && c == quoteChar) {
                // End of quoted string
                inQuotes = false;
                typeBuilder.append(c);
            } else if (!inQuotes && c == '<') {
                // Start of generic type
                inGeneric++;
                typeBuilder.append(c);
            } else if (!inQuotes && c == '>') {
                // End of generic type
                inGeneric--;
                typeBuilder.append(c);
            } else if (!inQuotes && c == '{') {
                // Start of brace block (for table types)
                inBraces++;
                typeBuilder.append(c);
            } else if (!inQuotes && c == '}') {
                // End of brace block
                inBraces--;
                typeBuilder.append(c);
            } else if (!inQuotes && inGeneric == 0 && inBraces == 0 && Character.isWhitespace(c)) {
                // Found whitespace outside of quotes, generics, and braces - this marks end of type
                break;
            } else {
                typeBuilder.append(c);
            }
            i++;
        }

        String type = typeBuilder.toString().trim();

        // Extract description from remaining text
        String description = "";
        if (i < input.length()) {
            description = input.substring(i).trim();
        }

        return new String[]{type, description};
    }

    private static void updateBlockFlags(String line, DocBlock block) {
        if (isClassOrFieldTag(line)) {
            block.hasClassOrFieldTags = true;
        }

        // Set expectFunction if we have function-related tags (@param, @return, @function) or plain doc lines
        // Make sure we set expectFunction for any function-related documentation
        if (isFunctionTag(line) && !block.hasClassOrFieldTags && !line.trim().startsWith("---@type")) {
            block.expectFunction = true;
        }

        // Also set expectFunction for plain doc lines that aren't class/field related
        if (isPlainDocLine(line) && !block.hasClassOrFieldTags && !line.trim().startsWith("---@type")) {
            block.expectFunction = true;
        }
    }

    private static boolean isFunctionTag(String line) {
        String trimmed = line.trim();
        return trimmed.startsWith("---@param") ||
               trimmed.startsWith("---@return") ||
               trimmed.startsWith("---@function") ||
               trimmed.startsWith("---@nodiscard");
    }

    private static boolean isClassOrFieldTag(String line) {
        return line.trim().startsWith("---@class") || line.trim().startsWith("---@field");
    }

    private static boolean isPlainDocLine(String line) {
        String trimmed = line.trim();
        return trimmed.startsWith("---") && !trimmed.startsWith("---@");
    }

    private static void processCodeLine(String line, DocBlock block, int lineNumber) {
        if (block.expectFunction) {
            // Check if the next line is actually a variable declaration instead of a function
            if (isVariableDeclaration(line)) {
                // This is actually a variable with documentation, treat it as such
                parseVariableDeclaration(line, block);
                block.hasTypeTag = true; // Mark as variable block
                block.expectFunction = false; // No longer expecting a function
                block.lines.add(line);
            } else {
                validateFunctionDeclaration(line, lineNumber);
                parseFunctionName(line, block);
                block.lines.add(line);
            }
        } else if (block.hasTypeTag) {
            // Variable-Deklaration verarbeiten
            parseVariableDeclaration(line, block);
            block.lines.add(line);
        } else if (block.hasClassOrFieldTags) {
            // Class assignment - process normally without requiring function declaration
            parseVariableDeclaration(line, block);
            block.lines.add(line);
        }
    }

    private static boolean isVariableDeclaration(String line) {
        String trimmedLine = line.trim();
        // Prüfe zuerst, ob es sich um eine Funktionszuweisung handelt
        if (trimmedLine.matches(".*=\\s*function\\s*\\(.*")) {
            return false; // Das ist eine Funktionszuweisung, keine Variable
        }

        // Check for variable declarations including nested table assignments
        return trimmedLine.startsWith("local ") && trimmedLine.contains("=") ||
               (trimmedLine.matches("[\\w\\.]+\\s*=.*") && !trimmedLine.startsWith("function") && !trimmedLine.startsWith("local function"));
    }

    private static void parseVariableDeclaration(String line, DocBlock block) {
        String trimmedLine = line.trim();

        // Pattern für lokale Variablen: local variableName = value
        Pattern localPattern = Pattern.compile("local\\s+(\\w+)\\s*=");
        // Pattern für globale Variablen und verschachtelte Tabellen: variableName = value oder Table.property = value
        Pattern globalPattern = Pattern.compile("([\\w\\.]+)\\s*=");

        Matcher localMatcher = localPattern.matcher(trimmedLine);
        Matcher globalMatcher = globalPattern.matcher(trimmedLine);

        if (localMatcher.find()) {
            // Lokale Variable
            block.variableName = localMatcher.group(1);
            block.isLocalVariable = true;
        } else if (globalMatcher.find()) {
            // Globale Variable oder verschachtelte Tabellenzuweisung
            String fullName = globalMatcher.group(1);

            // Prüfe, ob es sich um eine verschachtelte Klassenzuweisung handelt
            // z.B. Config.General = {} oder Config.Target.FloatingText = {}
            if (fullName.contains(".") && trimmedLine.contains("=") &&
                (trimmedLine.contains("{}") || trimmedLine.contains("= {}"))) {
                // Dies ist wahrscheinlich eine verschachtelte Klasse
                // Verwende den vollen Namen als Klassennamen wenn es bereits einen @class Tag gab
                if (!block.className.isEmpty() && block.className.equals(fullName)) {
                    // Der Klassenname stimmt mit der Zuweisung überein
                    block.variableName = fullName;
                } else {
                    // Normale Variable mit verschachteltem Namen
                    block.variableName = fullName;
                }
            } else {
                // Normale Variable
                block.variableName = fullName;
            }
            block.isLocalVariable = false;
        }
    }

    private static void parseFunctionName(String line, DocBlock block) {
        // Pattern für verschiedene Funktionstypen
        Pattern instancePattern = Pattern.compile("(?:local\\s+)?function\\s+(\\w+):(\\w+)\\s*\\(");
        Pattern staticPattern = Pattern.compile("(?:local\\s+)?function\\s+(\\w+)\\.(\\w+)\\s*\\(");
        Pattern standalonePattern = Pattern.compile("(?:local\\s+)?function\\s+(\\w+)\\s*\\(");

        // Neue Pattern für verschachtelte Funktionszuweisungen
        Pattern nestedFunctionAssignmentPattern = Pattern.compile("([\\w\\.]+)\\s*=\\s*function\\s*\\(");
        Pattern localNestedFunctionAssignmentPattern = Pattern.compile("local\\s+([\\w\\.]+)\\s*=\\s*function\\s*\\(");

        Matcher instanceMatcher = instancePattern.matcher(line.trim());
        Matcher staticMatcher = staticPattern.matcher(line.trim());
        Matcher standaloneMatcher = standalonePattern.matcher(line.trim());
        Matcher nestedMatcher = nestedFunctionAssignmentPattern.matcher(line.trim());
        Matcher localNestedMatcher = localNestedFunctionAssignmentPattern.matcher(line.trim());

        if (instanceMatcher.find()) {
            // Instanz-Methode: ClassName:methodName (kann lokal sein)
            block.functionClassName = instanceMatcher.group(1);
            block.functionName = instanceMatcher.group(2);
            block.isStatic = false;
        } else if (staticMatcher.find()) {
            // Statische Methode: ClassName.staticMethod (kann lokal sein)
            block.functionClassName = staticMatcher.group(1);
            block.functionName = staticMatcher.group(2);
            block.isStatic = true;
        } else if (standaloneMatcher.find()) {
            // Standalone-Funktion (kann lokal sein)
            block.functionName = standaloneMatcher.group(1);
            block.functionClassName = "";
            block.isStatic = false;
        } else if (localNestedMatcher.find()) {
            // Lokale verschachtelte Funktionszuweisung: local Table.subtable.func = function()
            String fullName = localNestedMatcher.group(1);
            parseNestedFunctionName(fullName, block);
        } else if (nestedMatcher.find()) {
            // Verschachtelte Funktionszuweisung: Table.subtable.func = function()
            String fullName = nestedMatcher.group(1);
            parseNestedFunctionName(fullName, block);
        }
    }

    /**
     * Parst verschachtelte Funktionsnamen wie Server.Core.myFunction
     * @param fullName Der vollständige Name der Funktion (z.B. "Server.Core.myFunction")
     * @param block Der DocBlock, der aktualisiert werden soll
     */
    private static void parseNestedFunctionName(String fullName, DocBlock block) {
        if (fullName.contains(".")) {
            // Finde den letzten Punkt, um den Funktionsnamen zu extrahieren
            int lastDotIndex = fullName.lastIndexOf(".");
            String className = fullName.substring(0, lastDotIndex);
            String functionName = fullName.substring(lastDotIndex + 1);

            block.functionClassName = className;
            block.functionName = functionName;
            block.isStatic = true; // Verschachtelte Zuweisungen sind normalerweise statisch
        } else {
            // Keine Verschachtelung, einfacher Funktionsname
            block.functionName = fullName;
            block.functionClassName = "";
            block.isStatic = false;
        }
    }

    private static void validateFunctionDeclaration(String line, int lineNumber) {
        String trimmed = line.trim();
        if (trimmed.startsWith("function ") || trimmed.startsWith("local function ") ||
            trimmed.matches(".*=\\s*function\\s*\\(.*") || trimmed.matches("local\\s+.*=\\s*function\\s*\\(.*")) {
            return;
        }
        throw new RuntimeException(
            String.format("Expected function declaration after documentation at line %d got '%s'",
                        lineNumber, line)
        );
    }

    public static Documentation generate(Path path) {
        try {
            String content = Files.readString(path);
            return generate(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates documentation from multiple Lua files.
     * @param filePaths Array of file paths to process
     * @return Combined documentation from all files
     */
    public static Documentation generateFromFiles(String[] filePaths) {
        Documentation combinedDocumentation = new Documentation();

        for (String filePath : filePaths) {
            try {
                System.out.println("Processing file: " + filePath);
                Path path = Paths.get(filePath);
                Documentation fileDocumentation = generate(path);

                // Merge documentation from this file into the combined documentation
                mergeDocumentation(combinedDocumentation, fileDocumentation, filePath);

            } catch (Exception e) {
                System.err.println("Error processing file " + filePath + ": " + e.getMessage());
                throw new RuntimeException("Failed to process file: " + filePath, e);
            }
        }

        return combinedDocumentation;
    }

    /**
     * Merges documentation from one file into the combined documentation.
     * @param target The target documentation to merge into
     * @param source The source documentation to merge from
     * @param sourceFileName The name of the source file (for error reporting)
     */
    private static void mergeDocumentation(Documentation target, Documentation source, String sourceFileName) {
        // Check for class name conflicts and merge fields
        for (Class sourceClass : source.classes) {
            Class existingClass = findClassByName(target, sourceClass.name);
            if (existingClass != null) {
                // Class already exists - merge fields and functions instead of skipping
                System.out.println("Merging class '" + sourceClass.name + "' - adding " +
                    sourceClass.fields.size() + " new fields and " + sourceClass.functions.size() + " new functions");

                // Add all new fields from source class to existing class
                for (Field newField : sourceClass.fields) {
                    // Check if field already exists
                    boolean fieldExists = false;
                    for (Field existingField : existingClass.fields) {
                        if (existingField.name.equals(newField.name)) {
                            fieldExists = true;
                            break;
                        }
                    }
                    if (!fieldExists) {
                        existingClass.fields.add(newField);
                    }
                }

                // Add all new functions from source class to existing class
                for (Function newFunction : sourceClass.functions) {
                    // Check if function already exists
                    boolean functionExists = false;
                    for (Function existingFunction : existingClass.functions) {
                        if (existingFunction.name.equals(newFunction.name)) {
                            functionExists = true;
                            break;
                        }
                    }
                    if (!functionExists) {
                        existingClass.functions.add(newFunction);
                    }
                }

                // Merge parent classes if they don't already exist
                for (String newParent : sourceClass.parent) {
                    if (!existingClass.parent.contains(newParent)) {
                        existingClass.parent.add(newParent);
                    }
                }
            } else {
                // New class - add it normally
                target.addClass(sourceClass);
            }
        }

        // Add all functions (global functions can have the same name in different contexts)
        for (Function function : source.functions) {
            target.addFunction(function);
        }

        // Add all variables
        for (Variable variable : source.variables) {
            target.addVariable(variable);
        }
    }

    /**
     * Checks if a function block represents a local function.
     * @param block The doc block to check
     * @return true if this is a local function
     */
    private static boolean isLocalFunction(DocBlock block) {
        // Check if any line in the block contains "local function"
        for (String line : block.lines) {
            if (line.trim().startsWith("local function")) {
                return true;
            }
        }
        return false;
    }

    private static void processStandaloneFields(List<DocBlock> blocks, Documentation documentation) {
        // Verarbeite @field Blöcke die nach Klassendefinitionen stehen
        for (DocBlock block : blocks) {
            // Suche nach @field Blöcken ohne eigene Klassendefinition
            if (!block.fields.isEmpty() && block.className.isEmpty()) {
                // Analysiere die Code-Zeilen, um die Zielklasse zu bestimmen
                String targetClassName = findTargetClassForFields(block, documentation);

                if (targetClassName != null) {
                    Class targetClass = findClassByName(documentation, targetClassName);
                    if (targetClass != null) {
                        // Füge alle Felder zur gefundenen Klasse hinzu
                        targetClass.fields.addAll(block.fields);
                    }
                } else {
                    // Wenn keine Zielklasse gefunden wurde, versuche aus den @field Namen zu inferieren
                    for (Field field : block.fields) {
                        // Suche nach einer Klasse basierend auf Code-Zeilen
                        String inferredClassName = inferClassNameFromCodeLines(block, field.name, documentation);
                        if (inferredClassName != null) {
                            Class targetClass = findClassByName(documentation, inferredClassName);
                            if (targetClass != null) {
                                targetClass.fields.add(field);
                            }
                        }
                    }
                }
            }
        }
    }

    private static String findTargetClassForFields(DocBlock fieldBlock, Documentation documentation) {
        // Analysiere die Code-Zeilen im Field-Block, um die Zielklasse zu finden
        for (String line : fieldBlock.lines) {
            String trimmed = line.trim();
            // Suche nach Zuweisungen wie "Config.General.name = ..."
            if (trimmed.matches("[\\w.]+\\s*=.*")) {
                Pattern pattern = Pattern.compile("([\\w.]+)\\.[\\w]+\\s*=");
                Matcher matcher = pattern.matcher(trimmed);
                if (matcher.find()) {
                    String possibleClassName = matcher.group(1);
                    // Prüfe, ob diese Klasse existiert
                    if (findClassByName(documentation, possibleClassName) != null) {
                        return possibleClassName;
                    }
                }
            }
        }
        return null;
    }

    private static String inferClassNameFromCodeLines(DocBlock block, String fieldName, Documentation documentation) {
        // Suche nach Code-Zeilen, die den Feldnamen verwenden
        for (String line : block.lines) {
            String trimmed = line.trim();
            // Pattern für Zuweisungen wie "Config.General.fieldName = ..."
            if (trimmed.contains(fieldName) && trimmed.contains("=")) {
                Pattern pattern = Pattern.compile("([\\w.]+)\\." + Pattern.quote(fieldName) + "\\s*=");
                Matcher matcher = pattern.matcher(trimmed);
                if (matcher.find()) {
                    String possibleClassName = matcher.group(1);
                    // Prüfe, ob diese Klasse existiert
                    if (findClassByName(documentation, possibleClassName) != null) {
                        return possibleClassName;
                    }
                }
            }
        }
        return null;
    }
}
