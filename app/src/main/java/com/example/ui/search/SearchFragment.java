package com.example.ui.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.data.database.entities.NoteEntity;
import com.example.databinding.FragmentSearchBinding;
import com.example.ui.adapters.NoteAdapter;

public class SearchFragment extends Fragment implements NoteAdapter.OnNoteClickListener {

    private FragmentSearchBinding binding;
    private SearchViewModel viewModel;
    private NoteAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        adapter = new NoteAdapter(this);
        binding.recyclerSearchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerSearchResults.setAdapter(adapter);

        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            adapter.setCategories(categories);
        });

        viewModel.getSearchResults().observe(getViewLifecycleOwner(), notes -> {
            adapter.submitList(notes);
            boolean isEmpty = (notes == null || notes.isEmpty());
            String query = binding.editSearchQuery.getText() != null ? binding.editSearchQuery.getText().toString().trim() : "";
            
            if (query.isEmpty()) {
                binding.textSearchResultsCount.setText("Showing all " + (notes != null ? notes.size() : 0) + " notes");
                binding.layoutEmptySearch.setVisibility(View.GONE);
                binding.recyclerSearchResults.setVisibility(View.VISIBLE);
            } else {
                binding.textSearchResultsCount.setText("Found " + (notes != null ? notes.size() : 0) + " matching notes");
                binding.layoutEmptySearch.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                binding.recyclerSearchResults.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            }
        });

        binding.editSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setQuery(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public void onNoteClick(NoteEntity note) {
        Bundle args = new Bundle();
        args.putLong("noteId", note.id);
        Navigation.findNavController(requireView()).navigate(R.id.action_search_to_editor, args);
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
