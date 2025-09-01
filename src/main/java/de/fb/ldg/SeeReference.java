package de.fb.ldg;

/**
 * Represents a @see reference in documentation.
 */
public class SeeReference {
    /**
     * The name of the referenced element (class, function, etc.).
     */
    public final String referenceName;

    /**
     * The type of reference (class, function, etc.).
     */
    public final ReferenceType type;

    /**
     * Optional description for the reference.
     */
    public final String description;

    /**
     * Enum for different types of references.
     */
    public enum ReferenceType {
        CLASS,
        FUNCTION,
        GLOBAL_FUNCTION,
        METHOD
    }

    /**
     * Creates a new see reference for a class.
     * @param className the name of the referenced class
     * @param description optional description for the reference
     */
    public SeeReference(String className, String description) {
        this.referenceName = className;
        this.type = ReferenceType.CLASS;
        this.description = description != null ? description : "";
    }

    /**
     * Creates a new see reference with explicit type.
     * @param referenceName the name of the referenced element
     * @param type the type of reference
     * @param description optional description for the reference
     */
    public SeeReference(String referenceName, ReferenceType type, String description) {
        this.referenceName = referenceName;
        this.type = type;
        this.description = description != null ? description : "";
    }

    /**
     * @deprecated Use referenceName instead
     */
    @Deprecated
    public String getClassName() {
        return referenceName;
    }

    @Override
    public String toString() {
        if (description.isEmpty()) {
            return referenceName;
        }
        return referenceName + " - " + description;
    }
}
