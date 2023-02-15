package com.commandus.lgw;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.commandus.lgw.databinding.FragmentAddressListBinding;

import java.util.Date;

public class AddressListFragment extends Fragment
        implements MenuProvider, EnterUriListener,
        ItemSelection, AddressListResult, ConfirmationListener {

    private FragmentAddressListBinding binding;
    private RecyclerView recyclerViewDeviceAddress;
    private AddressItemViewModel addressItemViewModel;
    private LgwSettings lgwSettings;

    @Override
    public void onSelect(long id) {
        addressItemViewModel.selectAddress(DeviceAddressProvider.getById(getContext(), id));
        NavHostFragment.findNavController(AddressListFragment.this)
                .navigate(R.id.action_DeviceListFragment_to_DeviceItemFragment);
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_device_list, menu);
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_add_device:
                NavHostFragment.findNavController(AddressListFragment.this)
                        .navigate(R.id.action_DeviceListFragment_to_DeviceItemFragment);
                return true;
            case R.id.action_load_devices:
                enterUriToLoad();
                return true;
            case R.id.action_share_devices:
                shareAddresses();
                return true;
            case R.id.action_clear_devices:
                confirmDelete();
                return true;
        }
        return false;
    }

    private void confirmDelete() {
        ConfirmDeleteDialog d = new ConfirmDeleteDialog(this);
        d.show(this.getChildFragmentManager(), "");
    }

    @Override
    public void confirmed() {
        DeviceAddressProvider.clear(getContext());
        refreshAdapter();
    }

    private enum AddressAction {
        ACTION_LOAD,
        ACTION_SAVE
    }

    private AddressAction mAction;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentAddressListBinding.inflate(inflater, container, false);
        lgwSettings = LgwSettings.getSettings(getContext());
        recyclerViewDeviceAddress = binding.recyclerViewPayload;
        recyclerViewDeviceAddress.setLayoutManager(new LinearLayoutManager(getContext()));
        refreshAdapter();
        return binding.getRoot();
    }

    private void refreshAdapter() {
        DeviceAddressAdapter deviceAddressAdapter = new DeviceAddressAdapter(recyclerViewDeviceAddress, this);
        recyclerViewDeviceAddress.setAdapter(deviceAddressAdapter);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addressItemViewModel = new ViewModelProvider(requireActivity()).get(AddressItemViewModel.class);
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(this, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void enterUriToLoad() {
        mAction = AddressAction.ACTION_LOAD;
        EnterUriDialog d = new EnterUriDialog(this, lgwSettings.getLoadLastUri());
        d.show(this.getChildFragmentManager(), "");
    }

    private void shareAddresses() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, DeviceAddressProvider.toJson(getContext()));
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.msg_abp_addresses) + new Date().toString());
        sendIntent.setType("text/plain");
        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    @Override
    public void onSetUri(String value) {
        switch (mAction) {
            case ACTION_LOAD:
                // load
                lgwSettings.setLoadLastUri(value);
                lgwSettings.save();
                AddressLoader loader = new AddressLoader(value, this);
                break;
            case ACTION_SAVE:
                // save
                lgwSettings.setSaveLastUri(value);
                lgwSettings.save();
                break;
        }
    }

    @Override
    public void onDone(int status) {
        // load or save completed
        getActivity().runOnUiThread(()-> {
            refreshAdapter();
        });
    }

    @Override
    public void onAddress(LoraDeviceAddress value) {
        ContentValues cv = value.getContentValues();
        Uri uri = getContext().getContentResolver().insert(DeviceAddressProvider.CONTENT_URI_ABP, cv);
    }

    @Override
    public void onError(String message) {
        getActivity().runOnUiThread(()->{
            refreshAdapter();
            Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_LONG);
            toast.show();
        });
    }

}
