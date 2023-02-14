package com.commandus.lgw;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.commandus.lgw.databinding.FragmentAddressItemBinding;

public class AddressItemFragment extends Fragment
    implements MenuProvider {

    private FragmentAddressItemBinding binding;

    private AddressItemViewModel addressItemViewModel;
    private LoraDeviceAddress selected;
    private EditText editTextAddress;
    private EditText editTextEUI;
    private EditText editTextNwkSKey;
    private EditText editTextAppSKey;
    private EditText editTextName;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentAddressItemBinding.inflate(inflater, container, false);
        editTextAddress = binding.editTextAddressItemAddress;
        editTextEUI = binding.editTextItemEui;
        editTextNwkSKey = binding.editTextAddressItemNwkskey;
        editTextAppSKey = binding.editTextAddressAppSKey;
        editTextName = binding.editTextAddressItemName;
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addressItemViewModel = new ViewModelProvider(requireActivity()).get(AddressItemViewModel.class);

        selected = addressItemViewModel.selectedAddress.getValue();
        if (selected != null) {
            if (selected.addr != null)
                editTextAddress.setText(selected.addr.toString());
            if (selected.devEui != null)
                editTextEUI.setText(selected.devEui.toString());
            if (selected.nwkSKey != null)
                editTextNwkSKey.setText(selected.nwkSKey.toString());
            if (selected.appSKey != null)
                editTextAppSKey.setText(selected.appSKey.toString());
            if (selected.name != null)
                editTextName.setText(selected.name);
        }

        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(this, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void save() {
        if (selected != null)
            DeviceAddressProvider.update(getContext(), selected);
        else
            DeviceAddressProvider.add(getContext(), new LoraDeviceAddress(
                0L,
                editTextAddress.getText().toString(),
                editTextEUI.getText().toString(),
                editTextNwkSKey.getText().toString(),
                editTextAppSKey.getText().toString(),
                editTextName.getText().toString()
            ));
        NavHostFragment.findNavController(AddressItemFragment.this)
                .navigate(R.id.action_DeviceItemFragment_to_DeviceListFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_device_item, menu);
        if (selected == null || selected.id == 0L) {
            MenuItem mi = menu.findItem(R.id.action_delete_device);
            if (mi != null)
                mi.setEnabled(false);
        }
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_save_device:

                selected.addr = editTextAddress.getText().toString();
                selected.devEui = new DevEUI(editTextEUI.getText().toString());
                selected.nwkSKey = new KEY128(editTextNwkSKey.getText().toString());
                selected.appSKey = new KEY128(editTextAppSKey.getText().toString());
                selected.name = editTextName.getText().toString();

                DeviceAddressProvider.update(getContext(), selected);

                back();
                return true;
            case R.id.action_delete_device:
                if (selected != null && selected.id != 0L) {
                    DeviceAddressProvider.rm(getContext(), selected.id);
                }
                back();
                return true;
        }
        return false;
    }

    private void back() {
        NavHostFragment.findNavController(AddressItemFragment.this)
                .navigate(R.id.action_DeviceItemFragment_to_DeviceListFragment);
    }
}
