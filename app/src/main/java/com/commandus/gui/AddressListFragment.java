package com.commandus.gui;

import android.content.ContentValues;
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
import androidx.recyclerview.widget.RecyclerView;

import com.commandus.lgw.AddressListResult;
import com.commandus.lgw.AddressLoader;
import com.commandus.lgw.AddressSelection;
import com.commandus.lgw.DeviceAddressProvider;
import com.commandus.lgw.LgwSettings;
import com.commandus.lgw.LoraDeviceAddress;
import com.commandus.lgw.R;
import com.commandus.lgw.databinding.FragmentAddressListBinding;

public class AddressListFragment extends Fragment
        implements MenuProvider, EnterUriListener,
        AddressSelection, AddressListResult, ConfirmationListener {

    private FragmentAddressListBinding binding;
    DeviceAddressAdapter deviceAddressAdapter;
    private RecyclerView recyclerViewDeviceAddress;
    private AddressItemViewModel addressItemViewModel;
    private LgwSettings lgwSettings;

    @Override
    public void onSelect(long id) {
        addressItemViewModel.selectAddress(DeviceAddressProvider.getById(getContext(), id));
        NavHostFragment.findNavController(AddressListFragment.this)
                .navigate(R.id.action_FirstFragment_to_SecondFragment);
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
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
                return true;
            case R.id.action_load_devices:
                enterUriToLoad();
                return true;
            case R.id.action_share_devices:
                enterUriToShare();
                return true;
            case R.id.action_clear_devices:
                confirmDelete();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmDelete() {
        ConfirmDeleteDialog d = new ConfirmDeleteDialog(this);
        d.show(this.getChildFragmentManager(), "");
    }

    @Override
    public void confirmed() {
        removeAll();
    }

    private void removeAll() {
        DeviceAddressProvider.clear(getContext());
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
        lgwSettings = LgwSettings.getSettings(getContext());
        recyclerViewDeviceAddress = binding.recyclerViewDeviceAddress;

        deviceAddressAdapter = new DeviceAddressAdapter(recyclerViewDeviceAddress, (AddressSelection) this);
        //recyclerViewDeviceAddress.setAdapter(deviceAddressAdapter);

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(this, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        addressItemViewModel = new ViewModelProvider(this).get(AddressItemViewModel.class);

        PropertiesAdapter testAdapter = new PropertiesAdapter();
        recyclerViewDeviceAddress.setAdapter(testAdapter);

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

    private void enterUriToShare() {
        mAction = AddressAction.ACTION_SHARE;
        EnterUriDialog d = new EnterUriDialog(this, lgwSettings.getShareLastUri());
        d.show(this.getChildFragmentManager(), "");
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
            case ACTION_SHARE:
                // save
                lgwSettings.setShareLastUri(value);
                lgwSettings.save();
                break;
        }
    }

    @Override
    public void onDone(int status) {
        // load or save completed
    }

    @Override
    public void onAddress(LoraDeviceAddress value) {
        ContentValues cv = value.getContentValues();
        Uri uri = getContext().getContentResolver().insert(DeviceAddressProvider.CONTENT_URI_ABP, cv);
    }

    @Override
    public void onError(String message) {
        getActivity().runOnUiThread(()->{
            Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_LONG);
            toast.show();
        });
    }
}
