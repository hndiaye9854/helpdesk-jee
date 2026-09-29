package model.enums;

public enum Role {
    USER("Utilisateur"),
    TECHNICIAN("Technicien");

    private final String label;

    Role(String label) { this.label = label; }

    public String getLabel() { return label; }
}
