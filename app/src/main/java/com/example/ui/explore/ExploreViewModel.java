package com.example.ui.explore;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.data.database.entities.CategoryEntity;
import com.example.data.database.entities.TagEntity;
import com.example.data.repository.NoteRepository;

import java.util.List;

public class ExploreViewModel extends AndroidViewModel {

    private final LiveData<List<CategoryEntity>> categories;
    private final LiveData<List<TagEntity>> tags;

    public ExploreViewModel(@NonNull Application application) {
        super(application);
        NoteRepository repository = new NoteRepository(application);
        categories = repository.getAllCategories();
        tags = repository.getAllTags();
    }

    public LiveData<List<CategoryEntity>> getCategories() {
        return categories;
    }

    public LiveData<List<TagEntity>> getTags() {
        return tags;
    }
}
