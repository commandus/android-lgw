package com.commandus.lgw;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.commandus.lgw.databinding.FragmentPayloadDetailBinding;

import java.util.Date;

public class PayloadDetailFragment extends Fragment {

    private FragmentPayloadDetailBinding binding;
    private TextView textViewPayloadItemReceived;
    private TextView textViewPayloadItemEui;
    private TextView textViewPayloadItemName;
    private TextView textViewPayloadItemPayload;
    private TextView textViewPayloadItemFrequency;
    private TextView textViewPayloadItemRssi;
    private TextView textViewPayloadItemLsnr;
    private PayloadItemViewModel payloadItemViewModel;
    private Payload selected;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentPayloadDetailBinding.inflate(inflater, container, false);
        textViewPayloadItemReceived = binding.textViewPayloadItemReceived;
        textViewPayloadItemEui = binding.textViewPayloadItemEui;
        textViewPayloadItemName = binding.textViewPayloadItemName;
        textViewPayloadItemPayload = binding.textViewPayloadItemPayload;
        textViewPayloadItemFrequency = binding.textViewPayloadItemFrequency;
        textViewPayloadItemRssi = binding.textViewPayloadItemRssi;
        textViewPayloadItemLsnr = binding.textViewPayloadItemLsnr;

        payloadItemViewModel = new ViewModelProvider(requireActivity()).get(PayloadItemViewModel.class);

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        selected = payloadItemViewModel.selectedPayload.getValue();
        if (selected != null) {
            if (selected.received != null) {
                textViewPayloadItemReceived.setText(selected.received.toString());
            }
            if (selected.devEui != null)
                textViewPayloadItemEui.setText(selected.devEui);
            if (selected.devName != null)
                textViewPayloadItemName.setText(selected.devName);
            if (selected.hexPayload != null)
                textViewPayloadItemPayload.setText(selected.hexPayload);
            textViewPayloadItemFrequency.setText(selected.frequency);
            textViewPayloadItemRssi.setText(selected.rssi);
            textViewPayloadItemLsnr.setText(Float.toString(selected.lsnr));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}