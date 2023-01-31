package com.commandus.gui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.commandus.lgw.AddressSelection;
import com.commandus.lgw.R;
import com.commandus.lgw.databinding.FragmentAddressListBinding;

public class AddressListFragment extends Fragment
        implements AddressSelection {

    private FragmentAddressListBinding binding;
    DeviceAddressAdapter deviceAddressAdapter;
    private RecyclerView recyclerViewDeviceAddress;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentAddressListBinding.inflate(inflater, container, false);

        recyclerViewDeviceAddress = binding.recyclerViewDeviceAddress;

        deviceAddressAdapter = new DeviceAddressAdapter(recyclerViewDeviceAddress, this);
        recyclerViewDeviceAddress.setAdapter(deviceAddressAdapter);

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(AddressListFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onSelect(int position) {
        NavHostFragment.findNavController(AddressListFragment.this)
                .navigate(R.id.action_FirstFragment_to_SecondFragment);
    }
}
