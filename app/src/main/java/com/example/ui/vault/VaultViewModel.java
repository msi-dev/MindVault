package com.example.ui.vault;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.data.database.entities.CategoryEntity;
import com.example.data.database.entities.NoteEntity;
import com.example.data.repository.NoteRepository;

import java.util.ArrayList;
import java.util.List;

public class VaultViewModel extends AndroidViewModel {

    private final NoteRepository repository;
    private final LiveData<List<CategoryEntity>> categories;
    private final MutableLiveData<Long> selectedCategoryFilter = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> pinnedOnlyFilter = new MutableLiveData<>(false);
    private final LiveData<List<NoteEntity>> filteredNotes;

    public VaultViewModel(@NonNull Application application) {
        super(application);
        repository = new NoteRepository(application);
        categories = repository.getAllCategories();

        LiveData<List<NoteEntity>> rawNotes = Transformations.switchMap(selectedCategoryFilter, categoryId -> {
            if (categoryId == null || categoryId <= 0) {
                return repository.getAllActiveNotes();
            } else {
                return repository.getNotesByCategory(categoryId);
            }
        });

        filteredNotes = Transformations.switchMap(pinnedOnlyFilter, isPinnedOnly ->
            Transformations.map(rawNotes, notes -> {
                if (notes == null) return new ArrayList<>();
                if (!Boolean.TRUE.equals(isPinnedOnly)) return notes;
                List<NoteEntity> pinnedList = new ArrayList<>();
                for (NoteEntity note : notes) {
                    if (note.isPinned) {
                        pinnedList.add(note);
                    }
                }
                return pinnedList;
            })
        );
    }

    public LiveData<List<NoteEntity>> getFilteredNotes() {
        return filteredNotes;
    }

    public LiveData<List<CategoryEntity>> getCategories() {
        return categories;
    }

    public void setCategoryFilter(Long categoryId) {
        selectedCategoryFilter.setValue(categoryId);
    }

    public void setPinnedOnly(boolean pinnedOnly) {
        pinnedOnlyFilter.setValue(pinnedOnly);
    }

    public boolean isPinnedOnly() {
        return Boolean.TRUE.equals(pinnedOnlyFilter.getValue());
    }

    public void togglePin(NoteEntity note) {
        repository.setPinned(note.id, !note.isPinned);
    }

    public void archiveNote(long id) {
        repository.setArchived(id, true);
    }

    public void deleteNote(long id) {
        repository.softDelete(id);
    }
}
