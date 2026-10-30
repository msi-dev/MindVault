package com.example.data.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "notes",
    foreignKeys = {
        @ForeignKey(
            entity = CategoryEntity.class,
            parentColumns = "id",
            childColumns = "category_id",
            onDelete = ForeignKey.SET_NULL
        )
    },
    indices = {
        @Index("category_id"),
        @Index("updated_at")
    }
)
public class NoteEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String title;

    public String content;

    @ColumnInfo(name = "category_id")
    public Long categoryId;

    @ColumnInfo(name = "is_pinned")
    public boolean isPinned;

    @ColumnInfo(name = "is_archived")
    public boolean isArchived;

    @ColumnInfo(name = "is_deleted")
    public boolean isDeleted;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    public NoteEntity() {
        this.title = "";
        this.content = "";
        this.categoryId = null;
        this.isPinned = false;
        this.isArchived = false;
        this.isDeleted = false;
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @androidx.room.Ignore
    public NoteEntity(String title, String content, Long categoryId, boolean isPinned) {
        this.title = title;
        this.content = content;
        this.categoryId = categoryId;
        this.isPinned = isPinned;
        this.isArchived = false;
        this.isDeleted = false;
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }
}
