package com.commandus.lgw;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PayloadItemViewModel extends ViewModel {
    public MutableLiveData<Payload> selectedPayload = new MutableLiveData<>();
    public void selectPayload(Payload item) {
        selectedPayload.setValue(item);
    }
}
