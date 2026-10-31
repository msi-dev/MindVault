package com.example.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.data.database.CategoryDao;
import com.example.data.database.MindVaultDatabase;
import com.example.data.database.NoteDao;
import com.example.data.database.TagDao;
import com.example.data.database.entities.CategoryEntity;
import com.example.data.database.entities.NoteEntity;
import com.example.data.database.entities.NoteTagEntity;
import com.example.data.database.entities.TagEntity;
import com.example.nativeengine.NativeBridge;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoteRepository {

    private final NoteDao noteDao;
    private final CategoryDao categoryDao;
    private final TagDao tagDao;
    private final NativeBridge nativeBridge;
    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    public interface NoteCallback {
        void onComplete(long id);
    }

    public NoteRepository(Context context) {
        MindVaultDatabase db = MindVaultDatabase.getInstance(context);
        this.noteDao = db.noteDao();
        this.categoryDao = db.categoryDao();
        this.tagDao = db.tagDao();
        this.nativeBridge = new NativeBridge();
    }

    public LiveData<List<NoteEntity>> getAllActiveNotes() {
        return noteDao.getAllActiveNotes();
    }

    public LiveData<List<NoteEntity>> getRecentNotes(int limit) {
        return noteDao.getRecentNotes(limit);
    }

    public LiveData<List<NoteEntity>> getPinnedNotes() {
        return noteDao.getPinnedNotes();
    }

    public LiveData<List<NoteEntity>> getNotesByCategory(long categoryId) {
        return noteDao.getNotesByCategory(categoryId);
    }

    public LiveData<NoteEntity> getNote(long id) {
        return noteDao.getNoteById(id);
    }

    public LiveData<Integer> getNoteCount() {
        return noteDao.getActiveNoteCount();
    }

    public LiveData<List<NoteEntity>> searchNotes(String query) {
        if (query == null || query.trim().isEmpty()) {
            return noteDao.getAllActiveNotes();
        }
        return noteDao.searchNotes(query.trim());
    }

    public void insertNote(NoteEntity note, NoteCallback callback) {
        executor.execute(() -> {
            long id = noteDao.insert(note);
            if (callback != null) {
                callback.onComplete(id);
            }
        });
    }

    public void updateNote(NoteEntity note) {
        executor.execute(() -> {
            note.updatedAt = System.currentTimeMillis();
            noteDao.update(note);
        });
    }

    public void softDelete(long id) {
        executor.execute(() -> noteDao.softDelete(id, System.currentTimeMillis()));
    }

    public void permanentDelete(long id) {
        executor.execute(() -> noteDao.permanentDelete(id));
    }

    public void deleteMultipleNotes(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        executor.execute(() -> noteDao.deleteNotesByIds(ids));
    }

    public void setPinned(long id, boolean pinned) {
        executor.execute(() -> noteDao.setPinned(id, pinned, System.currentTimeMillis()));
    }

    public void setPinnedMultiple(List<Long> ids, boolean pinned) {
        if (ids == null || ids.isEmpty()) return;
        executor.execute(() -> noteDao.setPinnedForNotes(ids, pinned, System.currentTimeMillis()));
    }

    public void setArchived(long id, boolean archived) {
        executor.execute(() -> noteDao.setArchived(id, archived, System.currentTimeMillis()));
    }

    // Categories
    public LiveData<List<CategoryEntity>> getAllCategories() {
        return categoryDao.getAllCategories();
    }

    public LiveData<Integer> getCategoryCount() {
        return categoryDao.getCategoryCount();
    }

    public void insertCategory(CategoryEntity category) {
        executor.execute(() -> categoryDao.insert(category));
    }

    // Tags
    public LiveData<List<TagEntity>> getAllTags() {
        return tagDao.getAllTags();
    }

    public LiveData<List<TagEntity>> getTagsForNote(long noteId) {
        return tagDao.getTagsForNote(noteId);
    }

    public void addTagToNote(long noteId, String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) return;
        final String cleanTag = tagName.trim();
        executor.execute(() -> {
            TagEntity existing = tagDao.getTagByName(cleanTag);
            long tagId;
            if (existing != null) {
                tagId = existing.id;
            } else {
                tagId = tagDao.insert(new TagEntity(cleanTag));
            }
            if (tagId > 0) {
                tagDao.insertNoteTag(new NoteTagEntity(noteId, tagId));
            }
        });
    }

    public void removeTagFromNote(long noteId, long tagId) {
        executor.execute(() -> tagDao.deleteNoteTag(noteId, tagId));
    }

    public NativeBridge getNativeBridge() {
        return nativeBridge;
    }
}
