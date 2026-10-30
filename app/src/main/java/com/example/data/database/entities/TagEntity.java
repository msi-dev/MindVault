package com.example.data.database.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "tags",
    indices = {@Index(value = {"name"}, unique = true)}
)
public class TagEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;

    public long createdAt;

    public TagEntity() {
        this.name = "";
        this.createdAt = System.currentTimeMillis();
    }

    @androidx.room.Ignore
    public TagEntity(String name) {
        this.name = name;
        this.createdAt = System.currentTimeMillis();
    }
}
