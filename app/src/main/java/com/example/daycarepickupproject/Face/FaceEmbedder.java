package com.example.daycarepickupproject.Face;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Turns an aligned face image into an embedding vector using MobileFaceNet,
 * running entirely on the device. Similar faces give vectors that point the same way.
 */
public class FaceEmbedder implements Closeable {

    public static final String MODEL_FILE = "mobile_face_net.tflite";

    private final Interpreter interpreter;
    private final int inputSize;
    private final int embeddingSize;
    private final ByteBuffer input;
    private final int[] pixels;

    public FaceEmbedder(Context context) throws IOException {
        Interpreter.Options options = new Interpreter.Options().setNumThreads(4);
        interpreter = new Interpreter(loadModel(context), options);

        int[] inShape = interpreter.getInputTensor(0).shape(); // [batch, h, w, 3]
        if (inShape[0] != 1) {
            interpreter.resizeInput(0, new int[]{1, inShape[1], inShape[2], inShape[3]});
            interpreter.allocateTensors();
        }
        inputSize = inShape[1];
        int[] outShape = interpreter.getOutputTensor(0).shape(); // [1, embeddingSize]
        embeddingSize = outShape[outShape.length - 1];

        input = ByteBuffer.allocateDirect(inputSize * inputSize * 3 * 4).order(ByteOrder.nativeOrder());
        pixels = new int[inputSize * inputSize];
    }

    public int getInputSize() {
        return inputSize;
    }

    /** Returns the L2-normalised embedding of an aligned face of size getInputSize(). */
    public synchronized float[] embed(Bitmap face) {
        Bitmap scaled = face.getWidth() == inputSize && face.getHeight() == inputSize
                ? face
                : Bitmap.createScaledBitmap(face, inputSize, inputSize, true);
        scaled.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize);
        input.rewind();
        for (int p : pixels) {
            // MobileFaceNet expects RGB scaled to roughly -1..1
            input.putFloat((((p >> 16) & 0xFF) - 127.5f) / 128f);
            input.putFloat((((p >> 8) & 0xFF) - 127.5f) / 128f);
            input.putFloat(((p & 0xFF) - 127.5f) / 128f);
        }
        float[][] output = new float[1][embeddingSize];
        interpreter.run(input, output);
        return FaceMath.normalize(output[0]);
    }

    @Override
    public void close() {
        interpreter.close();
    }

    private static MappedByteBuffer loadModel(Context context) throws IOException {
        try (AssetFileDescriptor fd = context.getAssets().openFd(MODEL_FILE);
             FileInputStream stream = new FileInputStream(fd.getFileDescriptor());
             FileChannel channel = stream.getChannel()) {
            return channel.map(FileChannel.MapMode.READ_ONLY, fd.getStartOffset(), fd.getDeclaredLength());
        }
    }
}
