package com.example.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.data.database.entities.NoteEntity;

import java.util.List;

@Dao
public interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(NoteEntity note);

    @Update
    void update(NoteEntity note);

    @Delete
    void delete(NoteEntity note);

    @Query("SELECT * FROM notes WHERE is_deleted = 0 AND is_archived = 0 ORDER BY is_pinned DESC, updated_at DESC")
    LiveData<List<NoteEntity>> getAllActiveNotes();

    @Query("SELECT * FROM notes WHERE is_deleted = 0 AND is_archived = 0 ORDER BY updated_at DESC LIMIT :limit")
    LiveData<List<NoteEntity>> getRecentNotes(int limit);

    @Query("SELECT * FROM notes WHERE is_deleted = 0 AND is_archived = 0 AND is_pinned = 1 ORDER BY updated_at DESC")
    LiveData<List<NoteEntity>> getPinnedNotes();

    @Query("SELECT * FROM notes WHERE is_deleted = 0 AND is_archived = 0 AND category_id = :categoryId ORDER BY is_pinned DESC, updated_at DESC")
    LiveData<List<NoteEntity>> getNotesByCategory(long categoryId);

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    LiveData<NoteEntity> getNoteById(long id);

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    NoteEntity getNoteByIdSync(long id);

    @Query("SELECT COUNT(*) FROM notes WHERE is_deleted = 0 AND is_archived = 0")
    LiveData<Integer> getActiveNoteCount();

    @Query("SELECT COUNT(*) FROM notes WHERE is_deleted = 0 AND is_archived = 0")
    int getActiveNoteCountSync();

    @Query("SELECT * FROM notes WHERE is_deleted = 0 AND is_archived = 0 AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') ORDER BY is_pinned DESC, updated_at DESC")
    LiveData<List<NoteEntity>> searchNotes(String query);

    @Query("UPDATE notes SET is_deleted = 1, updated_at = :time WHERE id = :id")
    void softDelete(long id, long time);

    @Query("UPDATE notes SET is_pinned = :pinned, updated_at = :time WHERE id = :id")
    void setPinned(long id, boolean pinned, long time);

    @Query("UPDATE notes SET is_archived = :archived, updated_at = :time WHERE id = :id")
    void setArchived(long id, boolean archived, long time);

    @Query("DELETE FROM notes WHERE id = :id")
    void permanentDelete(long id);

    @Query("DELETE FROM notes WHERE id IN (:ids)")
    void deleteNotesByIds(List<Long> ids);

    @Query("UPDATE notes SET is_pinned = :pinned, updated_at = :time WHERE id IN (:ids)")
    void setPinnedForNotes(List<Long> ids, boolean pinned, long time);

    @Query("SELECT * FROM notes WHERE id IN (:ids)")
    List<NoteEntity> getNotesByIdsSync(List<Long> ids);
}
