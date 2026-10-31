package com.example.ui.home;

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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.R;
import com.example.data.database.entities.NoteEntity;
import com.example.databinding.FragmentHomeBinding;
import com.example.ui.adapters.NoteAdapter;
import com.example.util.NoteFileHelper;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeFragment extends Fragment implements NoteAdapter.OnNoteClickListener {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private NoteAdapter pinnedAdapter;
    private NoteAdapter recentAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding.textGreeting.setText(viewModel.getDynamicGreeting());

        // Adapters
        pinnedAdapter = new NoteAdapter(this);
        binding.recyclerPinnedNotes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerPinnedNotes.setAdapter(pinnedAdapter);

        recentAdapter = new NoteAdapter(this);
        binding.recyclerRecentNotes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerRecentNotes.setAdapter(recentAdapter);

        // Handle back press during multi-selection
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if ((pinnedAdapter != null && pinnedAdapter.isSelectionMode()) ||
                    (recentAdapter != null && recentAdapter.isSelectionMode())) {
                    clearAllSelection();
                } else {
                    setEnabled(false);
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        // Observers
        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            pinnedAdapter.setCategories(categories);
            recentAdapter.setCategories(categories);
        });

        viewModel.getPinnedNotes().observe(getViewLifecycleOwner(), pinned -> {
            pinnedAdapter.submitList(pinned);
            binding.layoutPinnedSection.setVisibility(pinned != null && !pinned.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getRecentNotes().observe(getViewLifecycleOwner(), recent -> {
            recentAdapter.submitList(recent);
        });

        viewModel.getTotalNotesCount().observe(getViewLifecycleOwner(), count -> {
            binding.textNotesCount.setText(String.valueOf(count != null ? count : 0));
        });

        viewModel.getCollectionsCount().observe(getViewLifecycleOwner(), count -> {
            binding.textCollectionsCount.setText(String.valueOf(count != null ? count : 0));
        });

        // Click listeners
        final NavController navController = Navigation.findNavController(view);

        binding.fabQuickCapture.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putLong("noteId", -1L);
            navController.navigate(R.id.action_home_to_editor, args);
        });

        binding.cardSearchShortcut.setOnClickListener(v -> {
            navController.navigate(R.id.action_home_to_search);
        });

        binding.textViewAllRecent.setOnClickListener(v -> {
            navController.navigate(R.id.action_home_to_vault);
        });

        binding.cardStatNotes.setOnClickListener(v -> {
            navController.navigate(R.id.action_home_to_vault);
        });

        binding.cardStatCollections.setOnClickListener(v -> {
            navController.navigate(R.id.exploreFragment);
        });

        // Setup Selection Bar
        setupSelectionBar();
    }

    private void setupSelectionBar() {
        binding.btnSelectionClose.setOnClickListener(v -> clearAllSelection());

        binding.btnSelectionSelectAll.setOnClickListener(v -> {
            pinnedAdapter.selectAll();
            recentAdapter.selectAll();
        });

        binding.btnSelectionPin.setOnClickListener(v -> {
            Set<Long> ids = getCombinedSelectedIds();
            viewModel.setPinnedMultiple(ids, true);
            Snackbar.make(binding.getRoot(), "Pinned " + ids.size() + " notes", Snackbar.LENGTH_SHORT).show();
            clearAllSelection();
        });

        binding.btnSelectionShare.setOnClickListener(v -> {
            List<NoteEntity> notes = getCombinedSelectedNotes();
            NoteFileHelper.shareMultipleNotes(requireContext(), notes);
            clearAllSelection();
        });

        binding.btnSelectionExport.setOnClickListener(v -> {
            List<NoteEntity> notes = getCombinedSelectedNotes();
            int count = NoteFileHelper.exportMultipleNotes(requireContext(), notes);
            File dir = NoteFileHelper.getMindVaultFilesDirectory(requireContext());
            String path = dir != null ? dir.getPath() : "MindVault/files";
            Snackbar.make(binding.getRoot(), "Exported " + count + " notes to " + path, Snackbar.LENGTH_LONG).show();
            clearAllSelection();
        });

        binding.btnSelectionDelete.setOnClickListener(v -> {
            Set<Long> ids = getCombinedSelectedIds();
            new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete " + ids.size() + " notes?")
                .setMessage("These notes will be completely removed from your vault.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteMultipleNotes(ids);
                    Snackbar.make(binding.getRoot(), "Deleted " + ids.size() + " notes", Snackbar.LENGTH_SHORT).show();
                    clearAllSelection();
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
    }

    private Set<Long> getCombinedSelectedIds() {
        Set<Long> all = new HashSet<>(pinnedAdapter.getSelectedIds());
        all.addAll(recentAdapter.getSelectedIds());
        return all;
    }

    private List<NoteEntity> getCombinedSelectedNotes() {
        Set<Long> seen = new HashSet<>();
        List<NoteEntity> result = new ArrayList<>();
        for (NoteEntity n : pinnedAdapter.getSelectedNotes()) {
            if (seen.add(n.id)) result.add(n);
        }
        for (NoteEntity n : recentAdapter.getSelectedNotes()) {
            if (seen.add(n.id)) result.add(n);
        }
        return result;
    }

    private void clearAllSelection() {
        pinnedAdapter.setSelectionMode(false);
        recentAdapter.setSelectionMode(false);
    }

    @Override
    public void onNoteClick(NoteEntity note) {
        Bundle args = new Bundle();
        args.putLong("noteId", note.id);
        Navigation.findNavController(requireView()).navigate(R.id.action_home_to_editor, args);
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
        int totalSelected = getCombinedSelectedIds().size();
        boolean isAnySelecting = pinnedAdapter.isSelectionMode() || recentAdapter.isSelectionMode();

        if (totalSelected > 0) {
            binding.cardSelectionBar.setVisibility(View.VISIBLE);
            binding.fabQuickCapture.hide();
            binding.textSelectionCount.setText(totalSelected + " selected");
        } else if (isAnySelecting) {
            binding.cardSelectionBar.setVisibility(View.VISIBLE);
            binding.fabQuickCapture.hide();
            binding.textSelectionCount.setText("Select notes");
        } else {
            binding.cardSelectionBar.setVisibility(View.GONE);
            binding.fabQuickCapture.show();
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
            pinnedAdapter.setSelectionMode(true);
            recentAdapter.setSelectionMode(true);
            if (note.isPinned) {
                pinnedAdapter.toggleSelection(note.id);
            } else {
                recentAdapter.toggleSelection(note.id);
            }
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
