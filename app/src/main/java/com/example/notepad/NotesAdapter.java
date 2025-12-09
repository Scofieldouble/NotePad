package com.example.notepad;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {
    private List<Note> notes;
    private OnNoteClickListener listener;
    private SimpleDateFormat dateFormat;

    public interface OnNoteClickListener {
        void onNoteClick(int position);
        void onNoteLongClick(int position);
    }

    public NotesAdapter(List<Note> notes, OnNoteClickListener listener) {
        this.notes = notes;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault());
    }
    
    public void updateNotes(List<Note> filteredNotes) {
        this.notes = filteredNotes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.titleTextView.setText(note.getTitle());
        holder.contentTextView.setText(note.getContent());
        holder.dateTextView.setText(dateFormat.format(note.getDate()));
        holder.categoryTextView.setText(note.getCategory());
        
        // 显示待办状态
        if (note.isTodo()) {
            holder.iconTodo.setVisibility(View.VISIBLE);
            if (note.isCompleted()) {
                holder.iconTodo.setText("✓");
                holder.iconTodo.setTextColor(0xFF4CAF50); // 绿色
            } else {
                holder.iconTodo.setText("○");
                holder.iconTodo.setTextColor(0xFFFF9800); // 橙色
            }
        } else {
            holder.iconTodo.setVisibility(View.GONE);
        }
        
        // 显示媒体类型图标
        holder.iconImage.setVisibility(note.getImagePath() != null && !note.getImagePath().isEmpty() 
            ? View.VISIBLE : View.GONE);
        holder.iconAudio.setVisibility(note.getAudioPath() != null && !note.getAudioPath().isEmpty() 
            ? View.VISIBLE : View.GONE);
        holder.iconVideo.setVisibility(note.getVideoPath() != null && !note.getVideoPath().isEmpty() 
            ? View.VISIBLE : View.GONE);
        holder.iconSticky.setVisibility(note.isStickyNote() ? View.VISIBLE : View.GONE);

        holder.cardView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                listener.onNoteClick(adapterPosition);
            }
        });
        holder.cardView.setOnLongClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                listener.onNoteLongClick(adapterPosition);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView titleTextView;
        TextView contentTextView;
        TextView dateTextView;
        TextView categoryTextView;
        TextView iconImage;
        TextView iconAudio;
        TextView iconVideo;
        TextView iconTodo;
        TextView iconSticky;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            titleTextView = itemView.findViewById(R.id.note_title);
            contentTextView = itemView.findViewById(R.id.note_content);
            dateTextView = itemView.findViewById(R.id.note_date);
            categoryTextView = itemView.findViewById(R.id.note_category);
            iconImage = itemView.findViewById(R.id.icon_image);
            iconAudio = itemView.findViewById(R.id.icon_audio);
            iconVideo = itemView.findViewById(R.id.icon_video);
            iconTodo = itemView.findViewById(R.id.icon_todo);
            iconSticky = itemView.findViewById(R.id.icon_sticky);
        }
    }
}
