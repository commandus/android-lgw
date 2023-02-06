package com.commandus.gui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.commandus.lgw.DeviceAddressProvider;
import com.commandus.lgw.LoraDeviceAddress;
import com.commandus.lgw.R;
import com.commandus.lgw.databinding.FragmentAddressItemBinding;

public class AddressItemFragment extends Fragment {

    private FragmentAddressItemBinding binding;

    private AddressItemViewModel addressItemViewModel;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentAddressItemBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addressItemViewModel = new ViewModelProvider(this).get(AddressItemViewModel.class);
        LoraDeviceAddress selected = addressItemViewModel.getSelectedAddress().getValue();
        if (selected != null) {
            binding.editTextAddress.setText(selected.addr.toString());
            binding.editTextEUI.setText(selected.devEui.toString());
            binding.editTextNwkSKey.setText(selected.nwkSKey.toString());
            binding.editTextAppSKey.setText(selected.appSKey.toString());
            binding.editTextName.setText(selected.name);
        }

        binding.buttonAddressClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selected != null)
                    DeviceAddressProvider.update(getContext(), selected);
                else
                    DeviceAddressProvider.add(getContext(), new LoraDeviceAddress(
                    0L,
                        binding.editTextAddress.getText().toString(),
                        binding.editTextEUI.getText().toString(),
                        binding.editTextNwkSKey.getText().toString(),
                        binding.editTextAppSKey.getText().toString(),
                        binding.editTextName.getText().toString()
                    ));
                NavHostFragment.findNavController(AddressItemFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
