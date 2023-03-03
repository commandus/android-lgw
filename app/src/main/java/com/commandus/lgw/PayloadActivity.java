package com.commandus.lgw;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.commandus.lgw.databinding.ActivityPayloadBinding;

public class PayloadActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private PayloadItemViewModel payloadItemViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.commandus.lgw.databinding.ActivityPayloadBinding binding = ActivityPayloadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_payload);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        payloadItemViewModel = new ViewModelProvider(this).get(PayloadItemViewModel.class);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            if (b.containsKey(PayloadProvider.FN_ID)) {
                long id = b.getLong(PayloadProvider.FN_ID);
                if (id > 0) {
                    // navigate to id
                    showPayload(id);
                }
            }
        }
    }

    private void showPayload(long id) {
        if (PayloadProvider.getById(this, id) != null) {
            payloadItemViewModel.selectPayload(PayloadProvider.getById(this, id));
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_payload);
            navController.navigate(R.id.action_PayloadListFragment_to_PayloadItemFragment);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_payload);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        Fragment fragmentList = getSupportFragmentManager().findFragmentById(R.id.PayloadListFragment);
        if (fragmentList != null && fragmentList.isVisible()) {
            finish();
        } else {
            super.onBackPressed();
        }
    }
}