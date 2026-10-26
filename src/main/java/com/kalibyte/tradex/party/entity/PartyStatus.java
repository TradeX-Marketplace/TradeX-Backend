package com.kalibyte.tradex.party.entity;

/**
 * PartyStatus Enum
 * 
 * Represents the lifecycle status of a party (company/trader) in the system.
 * 
 * Workflow:
 * - Self-registered party starts at PENDING_APPROVAL
 * - Admin can: APPROVE → ACTIVE, REJECT → REJECTED, REQUEST CHANGES → CHANGES_REQUESTED
 * - Admin-created party starts at ACTIVE
 * - Party can be SUSPENDED for violations
 * - Party can be INACTIVE (temporarily disabled)
 * - DELETED is soft delete (kept for audit)
 * 
 * Access Rights by Status:
 * - PENDING_APPROVAL: Cannot login, awaiting admin decision
 * - ACTIVE: Full access to platform
 * - REJECTED: Cannot login, account blocked
 * - CHANGES_REQUESTED: Can resubmit, still cannot login
 * - SUSPENDED: Temporarily blocked by admin
 * - INACTIVE: Temporarily disabled
 * - DELETED: Soft deleted, no access
 * 
 * Created: September 26, 2026
 */
public enum PartyStatus {
    /**
     * Party submitted self-registration, waiting for admin approval
     * Party cannot login in this status
     */
    PENDING_APPROVAL("Pending Admin Approval", "Party registration submitted and waiting for admin review"),

    /**
     * Party approved by admin or created directly by admin
     * Party can login and access platform
     */
    ACTIVE("Active", "Party is active and can trade"),

    /**
     * Party registration rejected by admin
     * Party cannot login
     * Account is permanently blocked until resubmitted and approved
     */
    REJECTED("Rejected", "Party registration rejected by admin"),

    /**
     * Admin requested party to make changes to registration
     * Party cannot login but can resubmit with changes
     * Status moves back to PENDING_APPROVAL after resubmission
     */
    CHANGES_REQUESTED("Changes Requested", "Admin requested party to update their information"),

    /**
     * Party account suspended by admin for policy violation
     * Temporary suspension, admin can unsuspend
     * Party cannot login during suspension
     */
    SUSPENDED("Suspended", "Party account suspended by admin"),

    /**
     * Party account temporarily inactive
     * Can be reactivated
     * Party cannot login in this status
     */
    INACTIVE("Inactive", "Party account is temporarily inactive"),

    /**
     * Soft delete - party record kept for audit trail
     * Party cannot login
     * Not shown in active party lists
     */
    DELETED("Deleted", "Party account has been deleted (soft delete)");

    private final String displayName;
    private final String description;

    PartyStatus(String displayName, String description) {
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
     * Check if party in this status can login
     * Only ACTIVE parties can login
     */
    public boolean canLogin() {
        return this == ACTIVE;
    }

    /**
     * Check if party in this status is visible in active party lists
     */
    public boolean isVisible() {
        return this != DELETED;
    }

    /**
     * Check if admin can change party status
     */
    public boolean canChangeStatus() {
        return this != DELETED;
    }

    /**
     * Check if party can resubmit after CHANGES_REQUESTED
     */
    public boolean canResubmit() {
        return this == CHANGES_REQUESTED;
    }

    /**
     * Parse string to PartyStatus enum
     * Case-insensitive
     */
    public static PartyStatus fromString(String status) {
        if (status == null || status.isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        try {
            return PartyStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status, e);
        }
    }
}
