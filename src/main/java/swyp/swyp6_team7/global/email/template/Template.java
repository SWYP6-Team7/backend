package swyp.swyp6_team7.global.email.template;

public enum Template {
    EMAIL_VERIFICATION_CODE("email-verification-code");

    private final String identifier;

    // Constructor
    Template(String identifier) {
        this.identifier = identifier;
    }

    // Getter method for 'identifier'
    public String getIdentifier() {
        return identifier;
    }
}

