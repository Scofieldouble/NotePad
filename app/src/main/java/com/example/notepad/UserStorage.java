package com.example.notepad;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class UserStorage {
    private static final String PREFS_NAME = "NotepadUserPrefs";
    private static final String KEY_CURRENT_USER = "current_user";
    private static final String FILE_NAME = "users.dat";
    private static final String TAG = "UserStorage";
    
    // 保存用户列表
    public static void saveUsers(Context context, List<User> users) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        
        try {
            fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(users);
            Log.d(TAG, "Users saved successfully");
        } catch (IOException e) {
            Log.e(TAG, "Error saving users: " + e.getMessage());
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
    
    // 加载用户列表
    public static List<User> loadUsers(Context context) {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        List<User> users = null;
        
        try {
            fis = context.openFileInput(FILE_NAME);
            ois = new ObjectInputStream(fis);
            users = (List<User>) ois.readObject();
            Log.d(TAG, "Users loaded successfully");
        } catch (IOException | ClassNotFoundException e) {
            Log.d(TAG, "No existing users file, creating new list");
            users = new ArrayList<>();
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
        
        return users != null ? users : new ArrayList<>();
    }
    
    // 注册新用户
    public static boolean registerUser(Context context, String username, String password, String email) {
        List<User> users = loadUsers(context);
        
        // 检查用户名是否已存在
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return false; // 用户名已存在
            }
        }
        
        // 添加新用户
        User newUser = new User(username, password, email);
        users.add(newUser);
        saveUsers(context, users);
        return true;
    }
    
    // 验证登录
    public static User loginUser(Context context, String username, String password) {
        List<User> users = loadUsers(context);
        
        for (User user : users) {
            if (user.getUsername().equals(username) && user.validatePassword(password)) {
                // 保存当前登录用户
                saveCurrentUser(context, username);
                return user;
            }
        }
        
        return null; // 登录失败
    }
    
    // 保存当前登录用户
    public static void saveCurrentUser(Context context, String username) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_CURRENT_USER, username).apply();
    }
    
    // 获取当前登录用户
    public static String getCurrentUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_CURRENT_USER, null);
    }
    
    // 退出登录
    public static void logout(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_CURRENT_USER).apply();
    }
    
    // 检查是否已登录
    public static boolean isLoggedIn(Context context) {
        return getCurrentUser(context) != null;
    }
}

