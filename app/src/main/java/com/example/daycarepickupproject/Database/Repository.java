package com.example.daycarepickupproject.Database;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Runs database work off the main thread and delivers results back on it.
 */
public final class Repository {

    public interface Work<T> {
        T run(DaycareDao dao);
    }

    public interface Callback<T> {
        void onResult(T result);
    }

    private static final ExecutorService IO = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private Repository() {
    }

    public static <T> void run(Context context, Work<T> work, Callback<T> callback) {
        DaycareDao dao = AppDatabase.get(context).dao();
        IO.execute(() -> {
            T result = work.run(dao);
            if (callback != null) {
                MAIN.post(() -> callback.onResult(result));
            }
        });
    }

    public static void run(Context context, Work<?> work) {
        run(context, work, null);
    }
}
