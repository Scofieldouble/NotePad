package com.example.notepad;

import java.io.Serializable;
import java.util.Date;

public class Note implements Serializable {
    private String title;
    private String content;
    private Date date;
    private Date modifiedDate;
    private String category;
    private String folder;
    private boolean isLocked;
    private String password;
    private boolean isTodo;
    private boolean isCompleted;
    private Date reminderDate;
    private int priority; // 0=低, 1=中, 2=高
    private String color; // 笔记颜色标记
    private String imagePath; // 图片路径
    private String audioPath; // 音频路径
    private String videoPath; // 视频路径
    private boolean isStickyNote; // 是否为便签

    public Note(String title, String content) {
        this.title = title;
        this.content = content;
        this.date = new Date();
        this.modifiedDate = new Date();
        this.category = "默认";
        this.folder = "";
        this.isLocked = false;
        this.isTodo = false;
        this.isCompleted = false;
        this.priority = 1;
        this.color = "";
        this.imagePath = "";
        this.audioPath = "";
        this.videoPath = "";
        this.isStickyNote = false;
    }
    
    public Note(String title, String content, String category) {
        this.title = title;
        this.content = content;
        this.date = new Date();
        this.modifiedDate = new Date();
        this.category = category != null ? category : "默认";
        this.folder = "";
        this.isLocked = false;
        this.isTodo = false;
        this.isCompleted = false;
        this.priority = 1;
        this.color = "";
        this.imagePath = "";
        this.audioPath = "";
        this.videoPath = "";
        this.isStickyNote = false;
    }
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.date = new Date();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        this.date = new Date();
    }

    public Date getDate() {
        return date;
    }
    
    public String getCategory() {
        return category != null ? category : "默认";
    }
    
    public void setCategory(String category) {
        this.category = category != null ? category : "默认";
    }
    
    public Date getModifiedDate() {
        return modifiedDate != null ? modifiedDate : date;
    }
    
    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
    
    public String getFolder() {
        return folder != null ? folder : "";
    }
    
    public void setFolder(String folder) {
        this.folder = folder != null ? folder : "";
    }
    
    public boolean isLocked() {
        return isLocked;
    }
    
    public void setLocked(boolean locked) {
        isLocked = locked;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public boolean isTodo() {
        return isTodo;
    }
    
    public void setTodo(boolean todo) {
        isTodo = todo;
    }
    
    public boolean isCompleted() {
        return isCompleted;
    }
    
    public void setCompleted(boolean completed) {
        isCompleted = completed;
        if (completed) {
            this.modifiedDate = new Date();
        }
    }
    
    public Date getReminderDate() {
        return reminderDate;
    }
    
    public void setReminderDate(Date reminderDate) {
        this.reminderDate = reminderDate;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    public String getColor() {
        return color != null ? color : "";
    }
    
    public void setColor(String color) {
        this.color = color != null ? color : "";
    }
    
    public boolean validatePassword(String inputPassword) {
        if (!isLocked || password == null) {
            return true;
        }
        return password.equals(inputPassword);
    }
    
    public String getImagePath() {
        return imagePath != null ? imagePath : "";
    }
    
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath != null ? imagePath : "";
    }
    
    public String getAudioPath() {
        return audioPath != null ? audioPath : "";
    }
    
    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath != null ? audioPath : "";
    }
    
    public String getVideoPath() {
        return videoPath != null ? videoPath : "";
    }
    
    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath != null ? videoPath : "";
    }
    
    public boolean isStickyNote() {
        return isStickyNote;
    }
    
    public void setStickyNote(boolean stickyNote) {
        isStickyNote = stickyNote;
    }
    
    public boolean hasMedia() {
        return (imagePath != null && !imagePath.isEmpty()) ||
               (audioPath != null && !audioPath.isEmpty()) ||
               (videoPath != null && !videoPath.isEmpty());
    }
}
