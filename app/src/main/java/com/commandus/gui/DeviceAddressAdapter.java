package com.commandus.gui;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.commandus.lgw.AddressSelection;
import com.commandus.lgw.R;

public class DeviceAddressAdapter extends RecyclerView.Adapter<DeviceAddressAdapter.ViewHolder> {

    private final RecyclerView recyclerView;
    private final Cursor mCursor;
    private final AddressSelection mAddressSelection;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            textView = view.findViewById(R.id.textViewGatewayEvent);
        }

        public TextView getTextView() {
            return textView;
        }
    }

    // Create new views (invoked by the layout manager)

    public DeviceAddressAdapter(RecyclerView rv, AddressSelection addressSelection) {
        recyclerView = rv;
        mAddressSelection = addressSelection;
        mCursor = recyclerView.getContext().getContentResolver().query(
                DeviceAddressProvider.CONTENT_URI, DeviceAddressProvider.PROJECTION, null, null, null);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.gateway_event_list_item, viewGroup, false);
        if (mAddressSelection != null) {
            final RecyclerView.ViewHolder holder = new GatewayEventAdapter.ViewHolder(view);
            view.setOnClickListener(view1 -> {
                final int position = holder.getBindingAdapterPosition();
                mAddressSelection.onSelect(position);
            });
        }
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        mCursor.moveToPosition(position);
        viewHolder.getTextView().setText(mCursor.getString(DeviceAddressProvider.F_NAME));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }
}
