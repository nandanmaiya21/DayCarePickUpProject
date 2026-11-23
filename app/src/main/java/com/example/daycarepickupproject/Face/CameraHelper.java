package com.example.daycarepickupproject.Face;

import android.content.Context;
import android.util.Log;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Starts the front camera with a preview and the face analyzer attached. */
public final class CameraHelper {

    private static final String TAG = "CameraHelper";

    private CameraHelper() {
    }

    /** Returns the executor the analyzer runs on; shut it down in onDestroy. */
    public static ExecutorService start(Context context, LifecycleOwner owner,
                                        PreviewView previewView, FaceAnalyzer analyzer,
                                        Runnable onError) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(context);
        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build();
                analysis.setAnalyzer(executor, analyzer);

                CameraSelector selector = provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
                        ? CameraSelector.DEFAULT_FRONT_CAMERA
                        : CameraSelector.DEFAULT_BACK_CAMERA;
                provider.unbindAll();
                provider.bindToLifecycle(owner, selector, preview, analysis);
            } catch (Exception e) {
                Log.e(TAG, "Could not start camera", e);
                onError.run();
            }
        }, ContextCompat.getMainExecutor(context));
        return executor;
    }
}
