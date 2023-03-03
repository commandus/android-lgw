package com.commandus.lgw;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AddressItemViewModel extends ViewModel {
    public MutableLiveData<LoraDeviceAddress> selectedAddress = new MutableLiveData<>();
    public void selectAddress(LoraDeviceAddress item) {
        selectedAddress.setValue(item);
    }
}
