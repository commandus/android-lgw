package com.commandus.lgw;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.commandus.lgw.databinding.FragmentPayloadDetailBinding;

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
        textViewPayloadItemReceived.setOnClickListener(view ->
                LgwHelper.copy2clipboard(getContext(), getString(R.string.label_payload_item_payload),
                        payloadItemViewModel.selectedPayload.getValue().toString(), getString(R.string.msg_payload_copied)));
        textViewPayloadItemEui = binding.textViewPayloadItemEui;
        textViewPayloadItemEui.setOnClickListener(view ->
                LgwHelper.copy2clipboard(getContext(), getString(R.string.label_device_eui),
                        textViewPayloadItemEui.getText().toString(), getString(R.string.msg_eui_copied)));

        textViewPayloadItemName = binding.textViewPayloadItemName;
        textViewPayloadItemPayload = binding.textViewPayloadItemPayload;
        textViewPayloadItemPayload.setOnClickListener(view ->
                LgwHelper.copy2clipboard(getContext(), getString(R.string.label_payload_item_payload),
                textViewPayloadItemPayload.getText().toString(), getString(R.string.msg_payload_copied)));
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
                java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getContext());
                java.text.DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getContext());
                textViewPayloadItemReceived.setText(
                        dateFormat.format(selected.received) + " " + timeFormat.format(selected.received));
            }
            if (selected.devEui != null)
                textViewPayloadItemEui.setText(selected.devEui);
            if (selected.devName != null)
                textViewPayloadItemName.setText(selected.devName);
            if (selected.hexPayload != null)
                textViewPayloadItemPayload.setText(selected.hexPayload);
            textViewPayloadItemFrequency.setText(String.format("%.1f", selected.frequency / 1000000f) + " MHz");
            textViewPayloadItemRssi.setText(Integer.toString(selected.rssi) + " dBm");
            textViewPayloadItemLsnr.setText(Float.toString(selected.lsnr)+ " dB");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}