package com.example.ui.editor;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.R;
import com.example.data.database.entities.CategoryEntity;
import com.example.data.database.entities.NoteEntity;
import com.example.data.database.entities.TagEntity;
import com.example.databinding.FragmentEditorBinding;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class EditorFragment extends Fragment {

    private FragmentEditorBinding binding;
    private EditorViewModel viewModel;
    private boolean isUpdatingFromModel = false;
    private final List<CategoryEntity> currentCategories = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(EditorViewModel.class);

        long noteId = -1L;
        if (getArguments() != null) {
            noteId = getArguments().getLong("noteId", -1L);
        }

        viewModel.initNote(noteId);

        // Native engine status
        binding.textEngineStatus.setText(viewModel.getEngineVersion());

        // Back button and physical back handling
        binding.btnBack.setOnClickListener(v -> handleBack());
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBack();
            }
        });

        // Save button
        binding.btnSave.setOnClickListener(v -> {
            viewModel.saveNoteImmediately();
            Snackbar.make(binding.getRoot(), "Note saved to vault", Snackbar.LENGTH_SHORT).show();
        });

        // Pin toggle
        binding.btnPin.setOnClickListener(v -> viewModel.togglePin());

        // Archive note
        binding.btnArchive.setOnClickListener(v -> {
            viewModel.archiveNote();
            Snackbar.make(binding.getRoot(), "Note archived", Snackbar.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).navigateUp();
        });

        // Delete note
        binding.btnDelete.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete note?")
                .setMessage("This note will be moved to deleted notes.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteNote();
                    Navigation.findNavController(requireView()).navigateUp();
                })
                .setNegativeButton("Cancel", null)
                .show();
        });

        // Add Tag
        binding.chipAddTag.setOnClickListener(v -> showAddTagDialog());

        // Setup TextWatchers
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isUpdatingFromModel) {
                    String title = binding.editNoteTitle.getText().toString();
                    String content = binding.editNoteContent.getText().toString();
                    viewModel.onContentOrTitleChanged(title, content);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        binding.editNoteTitle.addTextChangedListener(textWatcher);
        binding.editNoteContent.addTextChangedListener(textWatcher);

        // Observers
        observeViewModel();
    }

    private void observeViewModel() {
        if (viewModel.getNote() != null) {
            viewModel.getNote().observe(getViewLifecycleOwner(), note -> {
                if (note != null && !isUpdatingFromModel) {
                    viewModel.setCurrentNote(note);
                    isUpdatingFromModel = true;
                    binding.editNoteTitle.setText(note.title);
                    binding.editNoteContent.setText(note.content);
                    isUpdatingFromModel = false;
                    selectCategoryInSpinner(note.categoryId);
                }
            });
        }

        viewModel.getSaveStatus().observe(getViewLifecycleOwner(), status -> {
            binding.textSaveStatus.setText(status);
        });

        viewModel.getIsPinned().observe(getViewLifecycleOwner(), pinned -> {
            binding.btnPin.setColorFilter(pinned ? getResources().getColor(R.color.primary, null) : getResources().getColor(R.color.textSecondary, null));
        });

        viewModel.getTextMetrics().observe(getViewLifecycleOwner(), metrics -> {
            binding.textFooterMetrics.setText(metrics);
        });

        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            currentCategories.clear();
            List<String> names = new ArrayList<>();
            names.add("Uncategorized");
            if (categories != null) {
                currentCategories.addAll(categories);
                for (CategoryEntity cat : categories) {
                    names.add(cat.name);
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                names
            );
            binding.spinnerCategory.setAdapter(adapter);

            Long selectedCatId = viewModel.getSelectedCategoryId().getValue();
            if (selectedCatId != null) {
                selectCategoryInSpinner(selectedCatId);
            }

            binding.spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) {
                        viewModel.setCategory(null);
                    } else if (position - 1 < currentCategories.size()) {
                        viewModel.setCategory(currentCategories.get(position - 1).id);
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        });

        viewModel.getTagsForNote().observe(getViewLifecycleOwner(), tags -> {
            updateTagChips(tags);
        });
    }

    private void selectCategoryInSpinner(Long categoryId) {
        if (categoryId == null) {
            binding.spinnerCategory.setSelection(0);
            return;
        }
        for (int i = 0; i < currentCategories.size(); i++) {
            if (currentCategories.get(i).id == categoryId) {
                binding.spinnerCategory.setSelection(i + 1);
                break;
            }
        }
    }

    private void updateTagChips(List<TagEntity> tags) {
        if (binding == null) return;
        // Keep the add tag chip (first child)
        int childCount = binding.chipGroupTags.getChildCount();
        if (childCount > 1) {
            binding.chipGroupTags.removeViews(1, childCount - 1);
        }

        if (tags != null) {
            for (TagEntity tag : tags) {
                Chip chip = new Chip(requireContext());
                chip.setText("#" + tag.name);
                chip.setCloseIconVisible(true);
                chip.setOnCloseIconClickListener(v -> viewModel.removeTag(tag.id));
                binding.chipGroupTags.addView(chip);
            }
        }
    }

    private void showAddTagDialog() {
        final EditText input = new EditText(requireContext());
        input.setHint("e.g. project, research, idea");
        input.setSingleLine(true);

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Tag")
            .setView(input)
            .setPositiveButton("Add", (dialog, which) -> {
                String text = input.getText().toString().trim();
                if (!text.isEmpty()) {
                    viewModel.addTag(text);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void handleBack() {
        viewModel.saveNoteImmediately();
        Navigation.findNavController(requireView()).navigateUp();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
