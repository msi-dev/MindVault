package com.example.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.data.database.entities.CategoryEntity;
import com.example.data.database.entities.NoteEntity;
import com.example.data.repository.NoteRepository;

import java.util.Calendar;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private final NoteRepository repository;
    private final LiveData<List<NoteEntity>> recentNotes;
    private final LiveData<List<NoteEntity>> pinnedNotes;
    private final LiveData<Integer> totalNotesCount;
    private final LiveData<Integer> collectionsCount;
    private final LiveData<List<CategoryEntity>> categories;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new NoteRepository(application);
        recentNotes = repository.getRecentNotes(5);
        pinnedNotes = repository.getPinnedNotes();
        totalNotesCount = repository.getNoteCount();
        collectionsCount = repository.getCategoryCount();
        categories = repository.getAllCategories();
    }

    public LiveData<List<NoteEntity>> getRecentNotes() {
        return recentNotes;
    }

    public LiveData<List<NoteEntity>> getPinnedNotes() {
        return pinnedNotes;
    }

    public LiveData<Integer> getTotalNotesCount() {
        return totalNotesCount;
    }

    public LiveData<Integer> getCollectionsCount() {
        return collectionsCount;
    }

    public LiveData<List<CategoryEntity>> getCategories() {
        return categories;
    }

    public void togglePin(NoteEntity note) {
        repository.setPinned(note.id, !note.isPinned);
    }

    public void permanentDelete(long id) {
        repository.permanentDelete(id);
    }

    public void deleteMultipleNotes(java.util.Collection<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            repository.deleteMultipleNotes(new java.util.ArrayList<>(ids));
        }
    }

    public void setPinnedMultiple(java.util.Collection<Long> ids, boolean pinned) {
        if (ids != null && !ids.isEmpty()) {
            repository.setPinnedMultiple(new java.util.ArrayList<>(ids), pinned);
        }
    }

    public String getDynamicGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 12) {
            return "GOOD MORNING";
        } else if (hour >= 12 && hour < 17) {
            return "GOOD AFTERNOON";
        } else if (hour >= 17 && hour < 22) {
            return "GOOD EVENING";
        } else {
            return "GOOD NIGHT";
        }
    }
}
