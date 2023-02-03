package com.commandus.gui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.commandus.lgw.LoraDeviceAddress;

public class AddressItemViewModel extends ViewModel {
    private final MutableLiveData<LoraDeviceAddress> selectedAddress = new MutableLiveData<LoraDeviceAddress>();

    public void selectAddress(LoraDeviceAddress item) {
        selectedAddress.setValue(item);
    }
    public LiveData<LoraDeviceAddress> getSelectedAddress() {
        return selectedAddress;
    }

}
