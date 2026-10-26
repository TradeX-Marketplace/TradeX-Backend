package com.kalibyte.tradex.party.entity;

/**
 * PartyRegistrationType Enum
 * 
 * Indicates how a party was registered in the system.
 * 
 * Two types:
 * 1. SELF_REGISTERED: Party filled registration form themselves
 *    - Goes through approval workflow
 *    - Admin must approve before party can login
 * 
 * 2. ADMIN_CREATED: Admin created party directly in system
 *    - Directly set to ACTIVE status
 *    - Party can login immediately (after getting credentials)
 *    - Used for bulk onboarding or special cases
 * 
 * This distinction affects:
 * - Initial status (PENDING_APPROVAL vs ACTIVE)
 * - Notification workflow
 * - Audit trails
 * 
 * Created: September 26, 2026
 */
public enum PartyRegistrationType {
    /**
     * Party self-registered via registration form
     * Status: PENDING_APPROVAL (initially)
     * Admin must approve: APPROVE → ACTIVE or REJECT → REJECTED
     */
    SELF_REGISTERED("Self Registered", "Party registered via self-service registration form"),

    /**
     * Admin created party directly in system
     * Status: ACTIVE (immediately)
     * Admin provides credentials, party can login immediately
     */
    ADMIN_CREATED("Admin Created", "Party created directly by admin");

    private final String displayName;
    private final String description;

    PartyRegistrationType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get initial status based on registration type
     * SELF_REGISTERED → PENDING_APPROVAL
     * ADMIN_CREATED → ACTIVE
     */
    public PartyStatus getInitialStatus() {
        return this == SELF_REGISTERED 
            ? PartyStatus.PENDING_APPROVAL 
            : PartyStatus.ACTIVE;
    }

    /**
     * Check if this type requires admin approval
     * SELF_REGISTERED requires approval
     * ADMIN_CREATED does not
     */
    public boolean requiresApproval() {
        return this == SELF_REGISTERED;
    }

    /**
     * Parse string to PartyRegistrationType enum
     * Case-insensitive
     */
    public static PartyRegistrationType fromString(String type) {
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("Registration type cannot be null or empty");
        }
        try {
            return PartyRegistrationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid registration type: " + type, e);
        }
    }
}
