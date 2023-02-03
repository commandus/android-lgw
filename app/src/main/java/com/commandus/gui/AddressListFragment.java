package com.commandus.gui;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.commandus.lgw.AddressListResult;
import com.commandus.lgw.AddressLoader;
import com.commandus.lgw.AddressSelection;
import com.commandus.lgw.LoraDeviceAddress;
import com.commandus.lgw.R;
import com.commandus.lgw.databinding.FragmentAddressListBinding;

import java.util.List;

public class AddressListFragment extends Fragment
        implements EnterUriDialog.EnterUriListener, AddressSelection, AddressListResult {

    private FragmentAddressListBinding binding;
    DeviceAddressAdapter deviceAddressAdapter;
    private RecyclerView recyclerViewDeviceAddress;
    private AddressItemViewModel addressItemViewModel;

    @Override
    public void onSelect(long id) {
        addressItemViewModel.selectAddress(DeviceAddressProvider.getById(getContext(), id));
        NavHostFragment.findNavController(AddressListFragment.this)
                .navigate(R.id.action_FirstFragment_to_SecondFragment);
    }

    private enum AddressAction {
        ACTION_LOAD,
        ACTION_SHARE
    }

    private AddressAction mAction;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentAddressListBinding.inflate(inflater, container, false);

        recyclerViewDeviceAddress = binding.recyclerViewDeviceAddress;
        deviceAddressAdapter = new DeviceAddressAdapter(recyclerViewDeviceAddress, (AddressSelection) this);
        recyclerViewDeviceAddress.setAdapter(deviceAddressAdapter);

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addressItemViewModel = new ViewModelProvider(this).get(AddressItemViewModel.class);

        binding.buttonAdd.setOnClickListener(view1 -> NavHostFragment.findNavController(AddressListFragment.this)
                .navigate(R.id.action_FirstFragment_to_SecondFragment));
        binding.buttonLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enterUriToLoad();
            }
        });

        binding.buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enterUriToShare();
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void enterUriToLoad() {
        mAction = AddressAction.ACTION_LOAD;
        EnterUriDialog d = new EnterUriDialog();
        d.show(this.getChildFragmentManager(), "");
    }

    private void enterUriToShare() {
        mAction = AddressAction.ACTION_SHARE;
        EnterUriDialog d = new EnterUriDialog();
        d.show(this.getChildFragmentManager(), "");
    }

    @Override
    public void onSetUri(String value) {
        switch (mAction) {
            case ACTION_LOAD:
                // load
                AddressLoader loader = new AddressLoader(value, this);
                break;
            case ACTION_SHARE:
                // save
                break;
        }
    }

    @Override
    public void onDone(int status, List<LoraDeviceAddress> values) {
        for (LoraDeviceAddress v: values) {
            ContentValues cv = v.getContentValues();
            Uri uri = getContext().getContentResolver().insert(DeviceAddressProvider.CONTENT_URI, cv);
        };
    }
}
