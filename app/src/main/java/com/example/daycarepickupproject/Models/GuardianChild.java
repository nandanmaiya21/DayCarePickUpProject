package com.example.daycarepickupproject.Models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

/** Links a guardian to a child they are authorised to pick up (many-to-many). */
@Entity(
        tableName = "guardian_child",
        primaryKeys = {"guardianId", "childId"},
        foreignKeys = {
                @ForeignKey(entity = Guardian.class, parentColumns = "id",
                        childColumns = "guardianId", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Child.class, parentColumns = "id",
                        childColumns = "childId", onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("childId")})
public class GuardianChild {

    public long guardianId;
    public long childId;

    public GuardianChild(long guardianId, long childId) {
        this.guardianId = guardianId;
        this.childId = childId;
    }
}
