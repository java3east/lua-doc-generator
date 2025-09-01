package de.fb.ldg;

/**
 * Represents a field in a Lua class.
 */
public class Field {

    /**
     * The name of the field.
     */
    public final String name;

    /**
     * The type of the field.
     */
    public final String type;

    /**
     * The visibility of the field (public, private, etc.).
     */
    public final String visibility;

    /**
     * The description of the field.
     */
    public final String description;

    /**
     * Creates a new field.
     * @param name the name of the field
     * @param type the type of the field
     * @param visibility the visibility of the field
     * @param description the description of the field
     */
    public Field(String name, String type, String visibility, String description) {
        this.name = name;
        this.type = type;
        this.visibility = visibility;
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format("%s %s: %s - %s", visibility, name, type, description);
    }
}
