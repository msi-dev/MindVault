package com.example.ui.explore;

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
import com.example.data.database.entities.TagEntity;
import com.example.databinding.FragmentExploreBinding;
import com.example.ui.adapters.CategoryAdapter;
import com.google.android.material.chip.Chip;

public class ExploreFragment extends Fragment implements CategoryAdapter.OnCategoryClickListener {

    private FragmentExploreBinding binding;
    private ExploreViewModel viewModel;
    private CategoryAdapter categoryAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentExploreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ExploreViewModel.class);

        categoryAdapter = new CategoryAdapter(this);
        binding.recyclerExploreCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerExploreCategories.setAdapter(categoryAdapter);

        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            categoryAdapter.setCategories(categories);
        });

        viewModel.getTags().observe(getViewLifecycleOwner(), tags -> {
            binding.chipGroupExploreTags.removeAllViews();
            if (tags != null) {
                for (TagEntity tag : tags) {
                    Chip chip = new Chip(requireContext());
                    chip.setText("#" + tag.name);
                    chip.setCheckable(false);
                    chip.setClickable(true);
                    chip.setOnClickListener(v -> {
                        Navigation.findNavController(requireView()).navigate(R.id.action_explore_to_vault);
                    });
                    binding.chipGroupExploreTags.addView(chip);
                }
            }
        });
    }

    @Override
    public void onCategoryClick(CategoryEntity category) {
        Navigation.findNavController(requireView()).navigate(R.id.action_explore_to_vault);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
