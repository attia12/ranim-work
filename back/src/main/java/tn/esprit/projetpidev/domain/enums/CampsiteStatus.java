// Module: Official Campsite & Booking | Layer: Domain Enum
package tn.esprit.projetpidev.domain.enums;

public enum CampsiteStatus {
    /** Campsite not yet open (before startDate or newly created without dates) */
    PENDING,
    /** Campsite is open and available for bookings */
    ACTIVE,
    /** Campsite is at full capacity for today */
    FULL,
    /** Admin-suspended or weather-suspended */
    SUSPENDED,
    /** Past its endDate */
    EXPIRED,
    /** Soft-deleted */
    DELETED
}
