package de.fb.ldg;

/**
 * Represents a variable in Lua (local or global).
 */
public class Variable {

    /**
     * The name of the variable.
     */
    public final String name;

    /**
     * The type of the variable.
     */
    public final String type;

    /**
     * The description of the variable.
     */
    public final String description;

    /**
     * Whether the variable is local or global.
     */
    public final boolean isLocal;

    /**
     * Creates a new variable.
     * @param name the name of the variable
     * @param type the type of the variable
     * @param description the description of the variable
     * @param isLocal whether the variable is local
     */
    public Variable(String name, String type, String description, boolean isLocal) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.isLocal = isLocal;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isLocal) {
            sb.append("local ");
        } else {
            sb.append("global ");
        }
        sb.append(name).append(": ").append(type);
        if (description != null && !description.isEmpty()) {
            sb.append(" - ").append(description);
        }
        return sb.toString();
    }
}
