package com.example.simplenote.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * 用 SharedPreferences + JSON 做一个非常简单的本地存储。
 */
object NoteStorage {

    private const val PREF_NAME = "simple_note_prefs"
    private const val KEY_NOTES = "notes_json"

    fun loadNotes(context: Context): MutableList<Note> {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = sp.getString(KEY_NOTES, null) ?: return mutableListOf()

        return try {
            val array = JSONArray(json)
            val list = mutableListOf<Note>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    Note(
                        id = obj.getLong("id"),
                        title = obj.getString("title"),
                        content = obj.getString("content"),
                        time = obj.getLong("time")
                    )
                )
            }
            list
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    fun saveNotes(context: Context, notes: List<Note>) {
        val array = JSONArray()
        notes.forEach { note ->
            val obj = JSONObject().apply {
                put("id", note.id)
                put("title", note.title)
                put("content", note.content)
                put("time", note.time)
            }
            array.put(obj)
        }
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sp.edit().putString(KEY_NOTES, array.toString()).apply()
    }

    fun generateId(notes: List<Note>): Long {
        val maxId = notes.maxOfOrNull { it.id } ?: 0L
        return maxId + 1
    }
}




