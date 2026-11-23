package com.example.daycarepickupproject.Models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * One enrolled face sample of a guardian, stored only as a numeric face embedding
 * (not as a photo). Deleted automatically when the guardian is deleted.
 */
@Entity(
        tableName = "face_embeddings",
        foreignKeys = @ForeignKey(entity = Guardian.class, parentColumns = "id",
                childColumns = "guardianId", onDelete = ForeignKey.CASCADE),
        indices = {@Index("guardianId")})
public class FaceEmbedding {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long guardianId;

    /** L2-normalised embedding vector, stored as little-endian floats. */
    @NonNull
    public byte[] vector = new byte[0];

    public long createdAt;
}
