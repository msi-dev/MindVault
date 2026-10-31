package com.example.ui.vault;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
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
import com.example.util.NoteFileHelper;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
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

        // Intercept back press when in multi-select mode
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (adapter != null && adapter.isSelectionMode()) {
                    adapter.setSelectionMode(false);
                } else {
                    setEnabled(false);
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

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

        // FAB create note
        binding.fabAddNote.setOnClickListener(v -> navigateToEditor(-1L));
        binding.btnEmptyCreateNote.setOnClickListener(v -> navigateToEditor(-1L));

        // Setup Selection Bar actions
        setupSelectionBar();
    }

    private void setupSelectionBar() {
        binding.btnSelectionClose.setOnClickListener(v -> adapter.setSelectionMode(false));

        binding.btnSelectionSelectAll.setOnClickListener(v -> adapter.selectAll());

        binding.btnSelectionPin.setOnClickListener(v -> {
            viewModel.setPinnedMultiple(adapter.getSelectedIds(), true);
            Snackbar.make(binding.getRoot(), "Pinned " + adapter.getSelectedIds().size() + " notes", Snackbar.LENGTH_SHORT).show();
            adapter.setSelectionMode(false);
        });

        binding.btnSelectionShare.setOnClickListener(v -> {
            List<NoteEntity> selectedNotes = adapter.getSelectedNotes();
            NoteFileHelper.shareMultipleNotes(requireContext(), selectedNotes);
            adapter.setSelectionMode(false);
        });

        binding.btnSelectionExport.setOnClickListener(v -> {
            List<NoteEntity> selectedNotes = adapter.getSelectedNotes();
            int count = NoteFileHelper.exportMultipleNotes(requireContext(), selectedNotes);
            File dir = NoteFileHelper.getMindVaultFilesDirectory(requireContext());
            String path = dir != null ? dir.getPath() : "MindVault/files";
            Snackbar.make(binding.getRoot(), "Exported " + count + " notes to " + path, Snackbar.LENGTH_LONG).show();
            adapter.setSelectionMode(false);
        });

        binding.btnSelectionDelete.setOnClickListener(v -> {
            int count = adapter.getSelectedIds().size();
            new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete " + count + " notes?")
                .setMessage("These notes will be completely removed from your vault.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteMultipleNotes(adapter.getSelectedIds());
                    Snackbar.make(binding.getRoot(), "Deleted " + count + " notes", Snackbar.LENGTH_SHORT).show();
                    adapter.setSelectionMode(false);
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
    }

    private void populateCategoryChips(List<CategoryEntity> categories) {
        if (categories == null || binding == null) return;

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
    public void onNoteHold(NoteEntity note, View view) {
        showHoldActionsBottomSheet(note);
    }

    @Override
    public void onSelectionChanged(int selectedCount) {
        if (binding == null) return;
        if (selectedCount > 0) {
            binding.cardSelectionBar.setVisibility(View.VISIBLE);
            binding.fabAddNote.hide();
            binding.textSelectionCount.setText(selectedCount + " selected");
        } else if (adapter.isSelectionMode()) {
            binding.cardSelectionBar.setVisibility(View.VISIBLE);
            binding.fabAddNote.hide();
            binding.textSelectionCount.setText("Select notes");
        } else {
            binding.cardSelectionBar.setVisibility(View.GONE);
            binding.fabAddNote.show();
        }
    }

    private void showHoldActionsBottomSheet(NoteEntity note) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheetView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_note_hold_actions, null);
        dialog.setContentView(sheetView);

        TextView titleView = sheetView.findViewById(R.id.text_action_title);
        TextView pinLabel = sheetView.findViewById(R.id.label_pin);
        ImageView pinIcon = sheetView.findViewById(R.id.icon_pin);

        titleView.setText(note.title.isEmpty() ? "Untitled Note" : note.title);
        pinLabel.setText(note.isPinned ? "Unpin note" : "Pin note on top");
        pinIcon.setImageResource(note.isPinned ? R.drawable.ic_pin : R.drawable.ic_pin);

        // 1. Pin
        sheetView.findViewById(R.id.action_pin).setOnClickListener(v -> {
            dialog.dismiss();
            viewModel.togglePin(note);
            Snackbar.make(binding.getRoot(), note.isPinned ? "Unpinned note" : "Pinned note on top", Snackbar.LENGTH_SHORT).show();
        });

        // 2. Share
        sheetView.findViewById(R.id.action_share).setOnClickListener(v -> {
            dialog.dismiss();
            NoteFileHelper.shareNote(requireContext(), note);
        });

        // 3. Export to MindVault/files
        sheetView.findViewById(R.id.action_export).setOnClickListener(v -> {
            dialog.dismiss();
            File exportedFile = NoteFileHelper.exportNote(requireContext(), note);
            if (exportedFile != null) {
                Snackbar.make(binding.getRoot(), "Exported to: " + exportedFile.getAbsolutePath(), Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(binding.getRoot(), "Failed to export note", Snackbar.LENGTH_SHORT).show();
            }
        });

        // 4. Select / Multi-Select
        sheetView.findViewById(R.id.action_select).setOnClickListener(v -> {
            dialog.dismiss();
            adapter.setSelectionMode(true);
            adapter.toggleSelection(note.id);
        });

        // 5. Delete
        sheetView.findViewById(R.id.action_delete).setOnClickListener(v -> {
            dialog.dismiss();
            new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete note?")
                .setMessage("This will completely remove '" + (note.title.isEmpty() ? "Untitled Note" : note.title) + "' from your vault.")
                .setPositiveButton("Delete", (d, which) -> {
                    viewModel.permanentDelete(note.id);
                    Snackbar.make(binding.getRoot(), "Note deleted", Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
