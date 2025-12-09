package com.example.notepad;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BackupManager {
    private static final String TAG = "BackupManager";
    private static final String BACKUP_DIR = "backups";
    
    public static boolean backupNotes(Context context, List<Note> notes) {
        try {
            File backupDir = new File(context.getExternalFilesDir(null), BACKUP_DIR);
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String fileName = "notes_backup_" + sdf.format(new Date()) + ".dat";
            File backupFile = new File(backupDir, fileName);
            
            FileOutputStream fos = new FileOutputStream(backupFile);
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(fos);
            oos.writeObject(notes);
            oos.close();
            fos.close();
            
            Log.d(TAG, "Backup created: " + backupFile.getAbsolutePath());
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error creating backup: " + e.getMessage());
            return false;
        }
    }
    
    public static List<Note> restoreNotes(Context context, String backupFileName) {
        try {
            File backupDir = new File(context.getExternalFilesDir(null), BACKUP_DIR);
            File backupFile = new File(backupDir, backupFileName);
            
            if (!backupFile.exists()) {
                Log.e(TAG, "Backup file not found: " + backupFileName);
                return null;
            }
            
            FileInputStream fis = new FileInputStream(backupFile);
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(fis);
            List<Note> notes = (List<Note>) ois.readObject();
            ois.close();
            fis.close();
            
            Log.d(TAG, "Backup restored: " + backupFileName);
            return notes;
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "Error restoring backup: " + e.getMessage());
            return null;
        }
    }
    
    public static String[] getBackupFiles(Context context) {
        File backupDir = new File(context.getExternalFilesDir(null), BACKUP_DIR);
        if (!backupDir.exists()) {
            return new String[0];
        }
        
        File[] files = backupDir.listFiles((dir, name) -> name.endsWith(".dat"));
        if (files == null) {
            return new String[0];
        }
        
        String[] fileNames = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            fileNames[i] = files[i].getName();
        }
        return fileNames;
    }
}

