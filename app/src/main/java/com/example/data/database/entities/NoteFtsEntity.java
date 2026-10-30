package com.example.data.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Fts4;

@Fts4(contentEntity = NoteEntity.class)
@Entity(tableName = "note_fts")
public class NoteFtsEntity {

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "content")
    public String content;

    public NoteFtsEntity(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
