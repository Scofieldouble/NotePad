package com.example.notepad;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class NoteStorage {
    private static final String FILE_NAME = "notes.dat";
    private static final String TAG = "NoteStorage";

    public static void saveNotes(Context context, List<Note> notes) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;

        try {
            fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(notes);
            Log.d(TAG, "Notes saved successfully");
        } catch (IOException e) {
            Log.e(TAG, "Error saving notes: " + e.getMessage());
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error closing streams: " + e.getMessage());
            }
        }
    }

    public static List<Note> loadNotes(Context context) {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        List<Note> notes = null;

        try {
            fis = context.openFileInput(FILE_NAME);
            ois = new ObjectInputStream(fis);
            notes = (List<Note>) ois.readObject();
            Log.d(TAG, "Notes loaded successfully");
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "Error loading notes: " + e.getMessage());
            // 如果加载失败，返回一个空列表
            notes = new ArrayList<>();
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error closing streams: " + e.getMessage());
            }
        }

        return notes;
    }
}
