package com.example.daycarepickupproject.Models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** A child enrolled at the daycare. */
@Entity(tableName = "children")
public class Child {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name = "";

    /** Room / class name, e.g. "Sunflower Room". May be empty. */
    @NonNull
    public String room = "";

    /** Age in years, 0 if not given. */
    public int age;

    public long createdAt;

    public String initials() {
        return NameUtils.initials(name);
    }
}
