package com.example.daycarepickupproject.Database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.daycarepickupproject.Models.Child;
import com.example.daycarepickupproject.Models.FaceEmbedding;
import com.example.daycarepickupproject.Models.Guardian;
import com.example.daycarepickupproject.Models.GuardianChild;
import com.example.daycarepickupproject.Models.PickupLog;
import com.example.daycarepickupproject.Models.PinHasher;
import com.example.daycarepickupproject.Models.Staff;

/** The app's local (on-device only) database. */
@Database(
        entities = {Staff.class, Child.class, Guardian.class, GuardianChild.class,
                FaceEmbedding.class, PickupLog.class},
        version = 1,
        exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    /** PIN of the staff account created on first launch. */
    public static final String DEFAULT_STAFF_PIN = "1234";
    public static final String DEFAULT_STAFF_NAME = "Admin";

    private static volatile AppDatabase instance;

    public abstract DaycareDao dao();

    public static AppDatabase get(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "daycare.db")
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull androidx.sqlite.db.SupportSQLiteDatabase db) {
                                    // Seed one staff account so the app can be signed into
                                    db.execSQL("INSERT INTO staff (name, pinHash) VALUES (?, ?)",
                                            new Object[]{DEFAULT_STAFF_NAME,
                                                    PinHasher.hash(DEFAULT_STAFF_PIN)});
                                }
                            })
                            .build();
                }
            }
        }
        return instance;
    }
}
