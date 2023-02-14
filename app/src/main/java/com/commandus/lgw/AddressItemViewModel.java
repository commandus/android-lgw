package com.commandus.lgw;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.commandus.lgw.LoraDeviceAddress;

public class AddressItemViewModel extends ViewModel {
    public MutableLiveData<LoraDeviceAddress> selectedAddress = new MutableLiveData<LoraDeviceAddress>();
    public void selectAddress(LoraDeviceAddress item) {
        selectedAddress.setValue(item);
    }
}
