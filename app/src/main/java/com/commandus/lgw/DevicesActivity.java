package com.commandus.lgw;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.commandus.lgw.R;
import com.commandus.lgw.databinding.ActivityDevicesBinding;

public class DevicesActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityDevicesBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDevicesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        Toolbar toolbar = binding.toolbar;
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_devices);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    }

    @Override
    public boolean onSupportNavigateUp() {
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_devices);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        Fragment fragmentList = getSupportFragmentManager().findFragmentById(R.id.DeviceListFragment);
        if (fragmentList != null && fragmentList.isVisible()) {
            finish();
        } else {
            super.onBackPressed();
        }
    }
}
