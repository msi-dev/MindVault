package com.example.ui.vault;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.R;
import com.example.data.database.entities.CategoryEntity;
import com.example.data.database.entities.NoteEntity;
import com.example.databinding.FragmentVaultBinding;
import com.example.ui.adapters.NoteAdapter;
import com.google.android.material.chip.Chip;

import java.util.List;

public class VaultFragment extends Fragment implements NoteAdapter.OnNoteClickListener {

    private FragmentVaultBinding binding;
    private VaultViewModel viewModel;
    private NoteAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentVaultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(VaultViewModel.class);

        adapter = new NoteAdapter(this);
        binding.recyclerVaultNotes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerVaultNotes.setAdapter(adapter);

        // Observers
        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            adapter.setCategories(categories);
            populateCategoryChips(categories);
        });

        viewModel.getFilteredNotes().observe(getViewLifecycleOwner(), notes -> {
            adapter.submitList(notes);
            boolean isEmpty = (notes == null || notes.isEmpty());
            binding.layoutEmptyVault.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.recyclerVaultNotes.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });

        // Pinned button toggle
        binding.btnFilterPinned.setOnClickListener(v -> {
            boolean nextState = !viewModel.isPinnedOnly();
            viewModel.setPinnedOnly(nextState);
            binding.btnFilterPinned.setIconResource(nextState ? R.drawable.ic_check : R.drawable.ic_pin);
        });

        // FAB create note
        binding.fabAddNote.setOnClickListener(v -> navigateToEditor(-1L));
        binding.btnEmptyCreateNote.setOnClickListener(v -> navigateToEditor(-1L));
    }

    private void populateCategoryChips(List<CategoryEntity> categories) {
        if (categories == null || binding == null) return;

        // Keep the "All" chip, remove previously generated chips
        int childCount = binding.chipGroupCategories.getChildCount();
        if (childCount > 1) {
            binding.chipGroupCategories.removeViews(1, childCount - 1);
        }

        binding.chipCatAll.setOnClickListener(v -> viewModel.setCategoryFilter(null));

        for (CategoryEntity cat : categories) {
            Chip chip = new Chip(requireContext());
            chip.setText(cat.name);
            chip.setCheckable(true);
            chip.setClickable(true);
            chip.setOnClickListener(v -> {
                if (chip.isChecked()) {
                    viewModel.setCategoryFilter(cat.id);
                } else {
                    binding.chipCatAll.setChecked(true);
                    viewModel.setCategoryFilter(null);
                }
            });
            binding.chipGroupCategories.addView(chip);
        }
    }

    private void navigateToEditor(long noteId) {
        Bundle args = new Bundle();
        args.putLong("noteId", noteId);
        Navigation.findNavController(requireView()).navigate(R.id.action_vault_to_editor, args);
    }

    @Override
    public void onNoteClick(NoteEntity note) {
        navigateToEditor(note.id);
    }

    @Override
    public void onNotePinToggle(NoteEntity note) {
        viewModel.togglePin(note);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
