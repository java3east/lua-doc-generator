package de.fb.ldg;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a class in the lua language.
 */
public class Class {

    /**
     * The name of the class.
     */
    public final String name;

    /**
     * All the names of the classes this class inherits from.
     */
    public final List<String> parent;

    /**
     * All fields of this class.
     */
    public final List<Field> fields;

    /**
     * All functions of this class.
     */
    public final List<Function> functions;

    /**
     * @see references for this class.
     */
    public final List<SeeReference> seeReferences;

    /**
     * Creates a new class.
     * @param name the name of the class
     * @param parents the classes this class inherits from
     * @param fields the fields of this class
     * @param functions the functions of this class
     * @param seeReferences @see references for this class
     */
    public Class(String name, List<String> parents, List<Field> fields,
                List<Function> functions, List<SeeReference> seeReferences) {
        this.name = name;
        this.parent = parents;
        this.fields = fields;
        this.functions = functions;
        this.seeReferences = seeReferences != null ? seeReferences : new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.name);
        if (!this.parent.isEmpty()) {
            sb.append(" : ");
            sb.append(String.join(", ", this.parent));
        }
        sb.append("\n");
        for (Field field : this.fields) {
            sb.append("  ").append(field.toString()).append("\n");
        }
        for (Function function : this.functions) {
            sb.append("  ").append(function.toString()).append("\n");
        }
        return sb.toString();
    }
}
