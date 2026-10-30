package com.example.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.R;
import com.example.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        // Native engine version
        binding.textNativeVersion.setText(viewModel.getNativeEngineVersion());

        // Initial theme radio check
        String currentMode = viewModel.getThemeMode();
        switch (currentMode) {
            case "light":
                binding.radioThemeLight.setChecked(true);
                break;
            case "dark":
                binding.radioThemeDark.setChecked(true);
                break;
            default:
                binding.radioThemeSystem.setChecked(true);
                break;
        }

        binding.radioGroupTheme.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_theme_light) {
                viewModel.setThemeMode("light");
            } else if (checkedId == R.id.radio_theme_dark) {
                viewModel.setThemeMode("dark");
            } else {
                viewModel.setThemeMode("system");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
