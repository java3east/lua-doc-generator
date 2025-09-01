package de.fb.ldg;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a function in a Lua class.
 */
public class Function {

    /**
     * The name of the function.
     */
    public final String name;

    /**
     * The description of the function.
     */
    public final String description;

    /**
     * The parameters of the function.
     */
    public final List<Parameter> parameters;

    /**
     * The return type and description.
     */
    public final String returnType;
    public final String returnDescription;

    /**
     * Whether the function is marked as @nodiscard.
     */
    public final boolean nodiscard;

    /**
     * Whether the function is static (uses . notation) or instance method (uses : notation).
     */
    public final boolean isStatic;

    /**
     * Code examples for this function.
     */
    public final List<String> examples;

    /**
     * @see references for this function.
     */
    public final List<SeeReference> seeReferences;

    /**
     * Creates a new function.
     * @param name the name of the function
     * @param description the description of the function
     * @param parameters the parameters of the function
     * @param returnType the return type
     * @param returnDescription the return description
     * @param nodiscard whether the function is marked as @nodiscard
     * @param isStatic whether the function is static
     * @param examples code examples for this function
     * @param seeReferences @see references for this function
     */
    public Function(String name, String description, List<Parameter> parameters,
                   String returnType, String returnDescription, boolean nodiscard,
                   boolean isStatic, List<String> examples, List<SeeReference> seeReferences) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.returnType = returnType;
        this.returnDescription = returnDescription;
        this.nodiscard = nodiscard;
        this.isStatic = isStatic;
        this.examples = examples;
        this.seeReferences = seeReferences != null ? seeReferences : new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isStatic) {
            sb.append("static ");
        }
        sb.append(name).append("(");
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) sb.append(", ");
            Parameter param = parameters.get(i);
            sb.append(param.name).append(": ").append(param.type);
        }
        sb.append(")");
        if (returnType != null && !returnType.isEmpty()) {
            sb.append(" -> ").append(returnType);
        }
        if (description != null && !description.isEmpty()) {
            sb.append(" - ").append(description);
        }
        return sb.toString();
    }

    /**
     * Represents a parameter of a function.
     */
    public static class Parameter {
        public final String name;
        public final String type;
        public final String description;

        public Parameter(String name, String type, String description) {
            this.name = name;
            this.type = type;
            this.description = description;
        }
    }
}
