package com.commandus.lgw;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.commandus.lgw.databinding.FragmentPayloadListBinding;

import java.util.Date;

public class PayloadListFragment extends Fragment
    implements MenuProvider, ItemSelection, ConfirmationListener {

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
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(this, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
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

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_payload_list, menu);
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_share_payload:
                sharePayload();
                return true;
            case R.id.action_clear_payload:
                confirmDelete();
                return true;
        }
        return false;
    }

    private void sharePayload() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, PayloadProvider.toJson(getContext()));
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.msg_share_payload) + new Date().toString());
        sendIntent.setType("text/plain");
        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    private void confirmDelete() {
        ConfirmDeleteDialog d = new ConfirmDeleteDialog(this);
        d.show(this.getChildFragmentManager(), "");
    }

    @Override
    public void confirmed() {
        PayloadProvider.clear(getContext());
        refreshAdapter();
    }
}