package com.example.daycarepickupproject.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.daycarepickupproject.Models.Child;
import com.example.daycarepickupproject.Models.FaceEmbedding;
import com.example.daycarepickupproject.Models.Guardian;
import com.example.daycarepickupproject.Models.GuardianChild;
import com.example.daycarepickupproject.Models.PickupLog;
import com.example.daycarepickupproject.Models.Staff;

import java.util.List;

/** All database access. Methods are blocking; call them from a background thread. */
@Dao
public abstract class DaycareDao {

    // ---- Staff ----

    @Insert
    public abstract long insertStaff(Staff staff);

    @Query("SELECT * FROM staff WHERE pinHash = :pinHash LIMIT 1")
    public abstract Staff findStaffByPinHash(String pinHash);

    @Query("SELECT COUNT(*) FROM staff")
    public abstract int countStaff();

    // ---- Children ----

    @Insert
    public abstract long insertChild(Child child);

    @Update
    public abstract void updateChild(Child child);

    @Delete
    public abstract void deleteChild(Child child);

    @Query("SELECT * FROM children ORDER BY name COLLATE NOCASE")
    public abstract List<Child> getChildren();

    @Query("SELECT * FROM children WHERE id = :id")
    public abstract Child getChild(long id);

    @Query("SELECT COUNT(*) FROM children")
    public abstract int countChildren();

    @Query("SELECT c.* FROM children c JOIN guardian_child gc ON gc.childId = c.id "
            + "WHERE gc.guardianId = :guardianId ORDER BY c.name COLLATE NOCASE")
    public abstract List<Child> getChildrenOfGuardian(long guardianId);

    // ---- Guardians ----

    @Insert
    public abstract long insertGuardian(Guardian guardian);

    @Delete
    public abstract void deleteGuardian(Guardian guardian);

    @Query("SELECT * FROM guardians WHERE id = :id")
    public abstract Guardian getGuardian(long id);

    @Query("SELECT g.* FROM guardians g JOIN guardian_child gc ON gc.guardianId = g.id "
            + "WHERE gc.childId = :childId ORDER BY g.name COLLATE NOCASE")
    public abstract List<Guardian> getGuardiansOfChild(long childId);

    @Query("SELECT COUNT(*) FROM guardian_child WHERE childId = :childId")
    public abstract int countGuardiansOfChild(long childId);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insertLinks(List<GuardianChild> links);

    // ---- Face embeddings ----

    @Insert
    public abstract void insertEmbeddings(List<FaceEmbedding> embeddings);

    @Query("SELECT * FROM face_embeddings")
    public abstract List<FaceEmbedding> getAllEmbeddings();

    /** Saves a guardian, their face samples and child links in one transaction. */
    @Transaction
    public long enrolGuardian(Guardian guardian, List<FaceEmbedding> embeddings, List<Long> childIds) {
        long guardianId = insertGuardian(guardian);
        for (FaceEmbedding e : embeddings) {
            e.guardianId = guardianId;
        }
        insertEmbeddings(embeddings);
        List<GuardianChild> links = new java.util.ArrayList<>();
        for (Long childId : childIds) {
            links.add(new GuardianChild(guardianId, childId));
        }
        insertLinks(links);
        return guardianId;
    }

    // ---- Pick-up log ----

    @Insert
    public abstract long insertLog(PickupLog log);

    @Insert
    public abstract void insertLogs(List<PickupLog> logs);

    @Query("SELECT * FROM pickup_log WHERE timestamp >= :from AND timestamp < :to ORDER BY timestamp DESC")
    public abstract List<PickupLog> getLogsBetween(long from, long to);

    @Query("SELECT * FROM pickup_log WHERE childId = :childId AND timestamp >= :from "
            + "AND outcome != 'REJECTED' ORDER BY timestamp DESC")
    public abstract List<PickupLog> getPickupsOfChildSince(long childId, long from);

    @Query("SELECT * FROM pickup_log WHERE outcome != 'REJECTED' AND timestamp >= :from "
            + "ORDER BY timestamp DESC LIMIT :limit")
    public abstract List<PickupLog> getRecentPickups(long from, int limit);

    /** Ids of children who were picked up (verified or override) since the given time. */
    @Query("SELECT DISTINCT childId FROM pickup_log WHERE outcome != 'REJECTED' "
            + "AND childId IS NOT NULL AND timestamp >= :from")
    public abstract List<Long> getPickedUpChildIdsSince(long from);

    @Query("SELECT COUNT(*) FROM pickup_log WHERE outcome = :outcome AND timestamp >= :from")
    public abstract int countOutcomeSince(String outcome, long from);
}
