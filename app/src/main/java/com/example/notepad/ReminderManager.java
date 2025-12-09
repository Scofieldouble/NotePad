package com.example.notepad;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReminderManager {
    private static final String ACTION_REMINDER = "com.example.notepad.REMINDER";
    
    public static void setReminder(Context context, Note note) {
        if (note.getReminderDate() == null || note.getReminderDate().before(new Date())) {
            return;
        }
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("note_title", note.getTitle());
        intent.putExtra("note_content", note.getContent());
        intent.putExtra("note_id", note.hashCode());
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, 
            note.hashCode(), 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, note.getReminderDate().getTime(), pendingIntent);
    }
    
    public static void cancelReminder(Context context, Note note) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, 
            note.hashCode(), 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }
    
    public static void updateAllReminders(Context context, List<Note> notes) {
        for (Note note : notes) {
            if (note.isTodo() && note.getReminderDate() != null) {
                setReminder(context, note);
            }
        }
    }
}

