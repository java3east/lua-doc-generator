package de.fb.ldg;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the complete documentation of a Lua file.
 */
public class Documentation {

    /**
     * All classes found in the Lua file.
     */
    public final List<Class> classes;

    /**
     * All standalone functions found in the Lua file.
     */
    public final List<Function> functions;

    /**
     * All variables found in the Lua file.
     */
    public final List<Variable> variables;

    /**
     * Creates a new documentation object.
     */
    public Documentation() {
        this.classes = new ArrayList<>();
        this.functions = new ArrayList<>();
        this.variables = new ArrayList<>();
    }

    /**
     * Adds a class to the documentation.
     * @param clazz the class to add
     */
    public void addClass(Class clazz) {
        this.classes.add(clazz);
    }

    /**
     * Adds a function to the documentation.
     * @param function the function to add
     */
    public void addFunction(Function function) {
        this.functions.add(function);
    }

    /**
     * Adds a variable to the documentation.
     * @param variable the variable to add
     */
    public void addVariable(Variable variable) {
        this.variables.add(variable);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== DOCUMENTATION ===\n");

        if (!classes.isEmpty()) {
            sb.append("\n--- CLASSES ---\n");
            for (Class clazz : classes) {
                sb.append(clazz.toString()).append("\n");
            }
        }

        if (!functions.isEmpty()) {
            sb.append("\n--- FUNCTIONS ---\n");
            for (Function function : functions) {
                sb.append(function.toString()).append("\n");
            }
        }

        if (!variables.isEmpty()) {
            sb.append("\n--- VARIABLES ---\n");
            for (Variable variable : variables) {
                sb.append(variable.toString()).append("\n");
            }
        }

        return sb.toString();
    }
}
