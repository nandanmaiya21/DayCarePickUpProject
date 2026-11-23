package com.example.daycarepickupproject.Models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** A staff member who can sign in with a PIN. Only a salted hash of the PIN is stored. */
@Entity(tableName = "staff", indices = {@Index(value = "pinHash", unique = true)})
public class Staff {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name = "";

    @NonNull
    public String pinHash = "";

    public String initials() {
        return NameUtils.initials(name);
    }
}
