package com.example.data.database.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
    tableName = "note_tags",
    primaryKeys = {"note_id", "tag_id"},
    foreignKeys = {
        @ForeignKey(
            entity = NoteEntity.class,
            parentColumns = "id",
            childColumns = "note_id",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = TagEntity.class,
            parentColumns = "id",
            childColumns = "tag_id",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {@Index("tag_id")}
)
public class NoteTagEntity {

    public long note_id;
    public long tag_id;

    public NoteTagEntity() {
    }

    @androidx.room.Ignore
    public NoteTagEntity(long note_id, long tag_id) {
        this.note_id = note_id;
        this.tag_id = tag_id;
    }
}
