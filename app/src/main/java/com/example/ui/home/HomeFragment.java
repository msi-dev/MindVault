package com.example.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
