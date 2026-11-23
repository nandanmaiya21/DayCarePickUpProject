package com.example.daycarepickupproject.Models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** A person allowed to pick up one or more children. */
@Entity(tableName = "guardians")
public class Guardian {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name = "";

    /** Relation to the child, e.g. "Father", "Grandmother". */
    @NonNull
    public String relation = "";

    /** Number of face photos enrolled (one embedding per photo). */
    public int photoCount;

    public long createdAt;

    public String initials() {
        return NameUtils.initials(name);
    }
}
