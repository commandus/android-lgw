package com.commandus.lgw;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PayloadAdapter extends RecyclerView.Adapter<PayloadAdapter.ViewHolder> {

    private final RecyclerView recyclerView;
    private final Cursor mCursor;
    protected final ItemSelection mSelection;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        private final TextView textView;
        public long id;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            textView = view.findViewById(R.id.textViewListItemPayload);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            PayloadAdapter adapter = (PayloadAdapter) getBindingAdapter();
            if (adapter != null)
                adapter.mSelection.onSelect(id);
        }

        public void set(long id, String payloadHex, String defaultValue) {
            this.id = id;
            if (payloadHex == null || payloadHex.isEmpty())
                payloadHex = defaultValue;
            textView.setText(payloadHex);
        }
    }

    // Create new views (invoked by the layout manager)
    public PayloadAdapter(RecyclerView rv, ItemSelection selection) {
        recyclerView = rv;
        mSelection = selection;
        mCursor = recyclerView.getContext().getContentResolver().query(
                PayloadProvider.CONTENT_URI_PAYLOAD, PayloadProvider.PROJECTION, null, null, null);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.payload_list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        mCursor.moveToPosition(position);
        viewHolder.set(mCursor.getLong(PayloadProvider.F_ID),
                mCursor.getString(PayloadProvider.F_PAYLOAD),
                recyclerView.getContext().getString(R.string.msg_no_payload));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }
}
