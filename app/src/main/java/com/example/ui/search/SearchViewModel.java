package com.example.ui.search;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.data.database.entities.CategoryEntity;
import com.example.data.database.entities.NoteEntity;
import com.example.data.repository.NoteRepository;

import java.util.List;

public class SearchViewModel extends AndroidViewModel {

    private final NoteRepository repository;
    private final MutableLiveData<String> queryLiveData = new MutableLiveData<>("");
    private final LiveData<List<NoteEntity>> searchResults;
    private final LiveData<List<CategoryEntity>> categories;

    public SearchViewModel(@NonNull Application application) {
        super(application);
        repository = new NoteRepository(application);
        categories = repository.getAllCategories();
        searchResults = Transformations.switchMap(queryLiveData, repository::searchNotes);
    }

    public void setQuery(String query) {
        queryLiveData.setValue(query);
    }

    public LiveData<List<NoteEntity>> getSearchResults() {
        return searchResults;
    }

    public LiveData<List<CategoryEntity>> getCategories() {
        return categories;
    }

    public void togglePin(NoteEntity note) {
        repository.setPinned(note.id, !note.isPinned);
    }
}
