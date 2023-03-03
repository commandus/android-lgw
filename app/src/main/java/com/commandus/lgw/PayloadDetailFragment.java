package com.commandus.lgw;

import android.annotation.SuppressLint;
import android.content.Context;
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

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
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

        Context context = getContext();
        if (context != null) {
            textViewPayloadItemReceived.setOnClickListener(view ->
                    LgwHelper.copy2clipboard(context, getString(R.string.label_payload_item_payload),
                            textViewPayloadItemPayload.getText().toString(), getString(R.string.msg_payload_copied)));
            textViewPayloadItemEui.setOnClickListener(view ->
                    LgwHelper.copy2clipboard(context, getString(R.string.label_device_eui),
                            textViewPayloadItemEui.getText().toString(), getString(R.string.msg_eui_copied)));
            textViewPayloadItemPayload.setOnClickListener(view ->
                    LgwHelper.copy2clipboard(context, getString(R.string.label_payload_item_payload),
                    textViewPayloadItemPayload.getText().toString(), getString(R.string.msg_payload_copied)));
        }
        payloadItemViewModel = new ViewModelProvider(requireActivity()).get(PayloadItemViewModel.class);
        return binding.getRoot();
    }

    @SuppressLint("DefaultLocale")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Payload selected = payloadItemViewModel.selectedPayload.getValue();
        if (selected != null) {
            Context ctx = getContext();
            if (selected.received != null) {
                java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(ctx);
                java.text.DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(ctx);
                textViewPayloadItemReceived.setText(
                        dateFormat.format(selected.received) + " " + timeFormat.format(selected.received));
            }
            if (selected.devEui != null)
                textViewPayloadItemEui.setText(selected.devEui);
            if (selected.devName != null)
                textViewPayloadItemName.setText(selected.devName);
            if (selected.hexPayload != null)
                textViewPayloadItemPayload.setText(selected.hexPayload);
            textViewPayloadItemFrequency.setText(String.format("%.1f", selected.frequency / 1000000f)
                    + " " + getString(R.string.label_mhz));
            textViewPayloadItemRssi.setText(selected.rssi + " " + getString(R.string.label_dbm));
            textViewPayloadItemLsnr.setText(selected.lsnr + " " + getString(R.string.label_db));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}