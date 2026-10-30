package com.example;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.data.preferences.AppPreferences;
import com.example.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply persisted user theme (system, light, or dark)
        AppPreferences preferences = new AppPreferences(this);
        AppPreferences.applyTheme(preferences.getThemeMode());

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // Link bottom navigation with navController
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);

            // Link toolbar with navController
            NavigationUI.setupWithNavController(binding.toolbar, navController);

            // Hide toolbar and bottom nav in full-screen editor destination
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getId() == R.id.editorFragment) {
                    binding.toolbar.setVisibility(View.GONE);
                    binding.bottomNavigation.setVisibility(View.GONE);
                } else {
                    binding.toolbar.setVisibility(View.VISIBLE);
                    binding.bottomNavigation.setVisibility(View.VISIBLE);
                }
            });
        }
    }
}
