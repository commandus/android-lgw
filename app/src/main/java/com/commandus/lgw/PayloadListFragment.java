package com.commandus.lgw;

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

import com.commandus.lgw.databinding.FragmentPayloadListBinding;

public class PayloadListFragment extends Fragment
    implements ItemSelection {

    private FragmentPayloadListBinding binding;
    private RecyclerView recyclerViewPayloadList;
    private PayloadItemViewModel payloadItemViewModel;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentPayloadListBinding.inflate(inflater, container, false);
        recyclerViewPayloadList = binding.recyclerViewPayload;
        refreshAdapter();
        return binding.getRoot();
    }

    private void refreshAdapter() {
        PayloadAdapter payloadAdapter = new PayloadAdapter(recyclerViewPayloadList, this);
        recyclerViewPayloadList.setAdapter(payloadAdapter);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        payloadItemViewModel = new ViewModelProvider(requireActivity()).get(PayloadItemViewModel.class);
        recyclerViewPayloadList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(PayloadListFragment.this)
                        .navigate(R.id.action_PayloadListFragment_to_PayloadItemFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onSelect(long id) {
        payloadItemViewModel.selectPayload(PayloadProvider.getById(getContext(), id));
        NavHostFragment.findNavController(PayloadListFragment.this)
            .navigate(R.id.action_PayloadListFragment_to_PayloadItemFragment);

    }
}