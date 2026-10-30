package com.example.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.data.database.entities.NoteTagEntity;
import com.example.data.database.entities.TagEntity;

import java.util.List;

@Dao
public interface TagDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(TagEntity tag);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertNoteTag(NoteTagEntity noteTag);

    @Query("DELETE FROM note_tags WHERE note_id = :noteId AND tag_id = :tagId")
    void deleteNoteTag(long noteId, long tagId);

    @Query("DELETE FROM note_tags WHERE note_id = :noteId")
    void deleteTagsForNote(long noteId);

    @Query("SELECT * FROM tags ORDER BY name ASC")
    LiveData<List<TagEntity>> getAllTags();

    @Query("SELECT * FROM tags ORDER BY name ASC")
    List<TagEntity> getAllTagsSync();

    @Query("SELECT t.* FROM tags t INNER JOIN note_tags nt ON t.id = nt.tag_id WHERE nt.note_id = :noteId ORDER BY t.name ASC")
    LiveData<List<TagEntity>> getTagsForNote(long noteId);

    @Query("SELECT t.* FROM tags t INNER JOIN note_tags nt ON t.id = nt.tag_id WHERE nt.note_id = :noteId ORDER BY t.name ASC")
    List<TagEntity> getTagsForNoteSync(long noteId);

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    TagEntity getTagByName(String name);

    @Query("SELECT COUNT(*) FROM tags")
    LiveData<Integer> getTagCount();

    @Query("SELECT COUNT(*) FROM tags")
    int getTagCountSync();
}
