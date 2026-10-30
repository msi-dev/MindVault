package com.example.ui.settings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.data.preferences.AppPreferences;
import com.example.data.repository.NoteRepository;

public class SettingsViewModel extends AndroidViewModel {

    private final AppPreferences appPreferences;
    private final NoteRepository repository;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        appPreferences = new AppPreferences(application);
        repository = new NoteRepository(application);
    }

    public String getThemeMode() {
        return appPreferences.getThemeMode();
    }

    public void setThemeMode(String mode) {
        appPreferences.setThemeMode(mode);
    }

    public LiveData<Integer> getNoteCount() {
        return repository.getNoteCount();
    }

    public String getNativeEngineVersion() {
        return repository.getNativeBridge().getEngineVersion();
    }
}
