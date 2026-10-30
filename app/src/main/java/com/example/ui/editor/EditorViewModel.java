package com.example.ui.editor;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.data.database.entities.CategoryEntity;
import com.example.data.database.entities.NoteEntity;
import com.example.data.database.entities.TagEntity;
import com.example.data.repository.NoteRepository;
import com.example.nativeengine.NativeBridge;

import java.util.List;

public class EditorViewModel extends AndroidViewModel {

    private final NoteRepository repository;
    private final NativeBridge nativeBridge;
    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private static final long DEBOUNCE_DELAY_MS = 600;

    private long currentNoteId = -1L;
    private NoteEntity currentNote = null;

    private final MutableLiveData<String> saveStatus = new MutableLiveData<>("Saved");
    private final MutableLiveData<Boolean> isPinned = new MutableLiveData<>(false);
    private final MutableLiveData<Long> selectedCategoryId = new MutableLiveData<>(null);
    private final MutableLiveData<String> textMetrics = new MutableLiveData<>("0 words · 0 min read");

    private LiveData<NoteEntity> noteLiveData = null;
    private final LiveData<List<CategoryEntity>> categories;
    private MutableLiveData<List<TagEntity>> tagsLiveData = new MutableLiveData<>();

    public EditorViewModel(@NonNull Application application) {
        super(application);
        repository = new NoteRepository(application);
        nativeBridge = repository.getNativeBridge();
        categories = repository.getAllCategories();
    }

    public void initNote(long noteId) {
        if (noteId > 0 && currentNoteId != noteId) {
            this.currentNoteId = noteId;
            noteLiveData = repository.getNote(noteId);
        }
    }

    public LiveData<NoteEntity> getNote() {
        return noteLiveData;
    }

    public void setCurrentNote(NoteEntity note) {
        this.currentNote = note;
        if (note != null) {
            this.currentNoteId = note.id;
            isPinned.setValue(note.isPinned);
            selectedCategoryId.setValue(note.categoryId);
            updateMetrics(note.content);
        }
    }

    public LiveData<List<CategoryEntity>> getCategories() {
        return categories;
    }

    public LiveData<List<TagEntity>> getTagsForNote() {
        if (currentNoteId > 0) {
            return repository.getTagsForNote(currentNoteId);
        }
        return tagsLiveData;
    }

    public LiveData<String> getSaveStatus() {
        return saveStatus;
    }

    public LiveData<Boolean> getIsPinned() {
        return isPinned;
    }

    public LiveData<Long> getSelectedCategoryId() {
        return selectedCategoryId;
    }

    public LiveData<String> getTextMetrics() {
        return textMetrics;
    }

    public void setCategory(Long categoryId) {
        selectedCategoryId.setValue(categoryId);
        if (currentNote != null) {
            currentNote.categoryId = categoryId;
            scheduleAutosave();
        }
    }

    public void togglePin() {
        boolean newPin = !Boolean.TRUE.equals(isPinned.getValue());
        isPinned.setValue(newPin);
        if (currentNote != null) {
            currentNote.isPinned = newPin;
            scheduleAutosave();
        }
    }

    public void onContentOrTitleChanged(String title, String content) {
        saveStatus.setValue("Editing…");
        updateMetrics(content);

        if (currentNote == null) {
            currentNote = new NoteEntity();
            currentNote.isPinned = Boolean.TRUE.equals(isPinned.getValue());
            currentNote.categoryId = selectedCategoryId.getValue();
        }
        currentNote.title = title;
        currentNote.content = content;

        scheduleAutosave();
    }

    private void updateMetrics(String content) {
        int words = nativeBridge.calculateWordCount(content);
        int chars = nativeBridge.calculateTextLength(content);
        int readingTime = nativeBridge.estimateReadingTimeMinutes(content);
        textMetrics.setValue(words + " words · " + chars + " chars · " + readingTime + " min read");
    }

    private void scheduleAutosave() {
        debounceHandler.removeCallbacksAndMessages(null);
        debounceHandler.postDelayed(this::saveNoteInternal, DEBOUNCE_DELAY_MS);
    }

    public void saveNoteImmediately() {
        debounceHandler.removeCallbacksAndMessages(null);
        saveNoteInternal();
    }

    private void saveNoteInternal() {
        if (currentNote == null) return;
        if (currentNote.title.trim().isEmpty() && currentNote.content.trim().isEmpty()) {
            saveStatus.postValue("Empty");
            return;
        }

        saveStatus.postValue("Saving…");
        currentNote.isPinned = Boolean.TRUE.equals(isPinned.getValue());
        currentNote.categoryId = selectedCategoryId.getValue();

        if (currentNoteId <= 0) {
            repository.insertNote(currentNote, id -> {
                currentNoteId = id;
                currentNote.id = id;
                saveStatus.postValue("Saved");
            });
        } else {
            currentNote.id = currentNoteId;
            repository.updateNote(currentNote);
            saveStatus.postValue("Saved");
        }
    }

    public void addTag(String tagName) {
        if (currentNoteId > 0 && tagName != null && !tagName.trim().isEmpty()) {
            repository.addTagToNote(currentNoteId, tagName.trim());
        }
    }

    public void removeTag(long tagId) {
        if (currentNoteId > 0) {
            repository.removeTagFromNote(currentNoteId, tagId);
        }
    }

    public void archiveNote() {
        if (currentNoteId > 0) {
            repository.setArchived(currentNoteId, true);
        }
    }

    public void deleteNote() {
        if (currentNoteId > 0) {
            repository.softDelete(currentNoteId);
        }
    }

    public String getEngineVersion() {
        return nativeBridge.getEngineVersion();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        debounceHandler.removeCallbacksAndMessages(null);
    }
}
