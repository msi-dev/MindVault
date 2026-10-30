package com.example.data.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class CategoryEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;

    public String color;

    public String icon;

    public long createdAt;

    public CategoryEntity() {
        this.name = "";
        this.color = "#465D9A";
        this.icon = "ic_category";
        this.createdAt = System.currentTimeMillis();
    }

    @androidx.room.Ignore
    public CategoryEntity(String name, String color, String icon) {
        this.name = name;
        this.color = color;
        this.icon = icon;
        this.createdAt = System.currentTimeMillis();
    }
}
