package com.example.daycarepickupproject.Models;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * One pick-up attempt. Names are copied in (not just ids) so the audit trail
 * still reads correctly after a child or guardian is removed.
 */
@Entity(tableName = "pickup_log", indices = {@Index("timestamp"), @Index("childId")})
public class PickupLog {

    public static final String VERIFIED = "VERIFIED";
    public static final String OVERRIDE = "OVERRIDE";
    public static final String REJECTED = "REJECTED";

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long timestamp;

    /** VERIFIED, OVERRIDE or REJECTED. */
    public String outcome;

    @Nullable public Long childId;
    @Nullable public String childName;

    @Nullable public Long guardianId;
    /** Guardian name, or the name typed by staff for an override. */
    @Nullable public String guardianName;

    /** Best face-match similarity (0..1), or -1 if no face comparison was made. */
    public float score = -1f;

    public long staffId;
    @Nullable public String staffName;

    /** Reason for an override or rejection (e.g. "No blink detected"). */
    @Nullable public String note;

    public boolean isPickedUp() {
        return VERIFIED.equals(outcome) || OVERRIDE.equals(outcome);
    }
}
