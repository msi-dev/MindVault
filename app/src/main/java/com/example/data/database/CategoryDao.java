package com.example.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.data.database.entities.CategoryEntity;

import java.util.List;

@Dao
public interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CategoryEntity category);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<CategoryEntity> categories);

    @Update
    void update(CategoryEntity category);

    @Delete
    void delete(CategoryEntity category);

    @Query("SELECT * FROM categories ORDER BY name ASC")
    LiveData<List<CategoryEntity>> getAllCategories();

    @Query("SELECT * FROM categories ORDER BY name ASC")
    List<CategoryEntity> getAllCategoriesSync();

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    LiveData<CategoryEntity> getCategoryById(long id);

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    CategoryEntity getCategoryByIdSync(long id);

    @Query("SELECT COUNT(*) FROM categories")
    LiveData<Integer> getCategoryCount();

    @Query("SELECT COUNT(*) FROM categories")
    int getCategoryCountSync();
}
