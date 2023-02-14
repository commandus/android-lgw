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
            textView = view.findViewById(R.id.textViewListItemAddress);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            PayloadAdapter adapter = (PayloadAdapter) getBindingAdapter();
            if (adapter != null)
                adapter.mSelection.onSelect(id);
        }

        public void set(int position, long id, String name) {
            this.id = id;
            textView.setText(name);
        }
    }

    // Create new views (invoked by the layout manager)
    public PayloadAdapter(RecyclerView rv, ItemSelection addressSelection) {
        recyclerView = rv;
        mSelection = addressSelection;
        mCursor = recyclerView.getContext().getContentResolver().query(
                PayloadProvider.CONTENT_URI_PAYLOAD, PayloadProvider.PROJECTION, null, null, null);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.payload_list_item, viewGroup, false);
        if (mSelection != null) {
            final RecyclerView.ViewHolder holder = new PayloadAdapter.ViewHolder(view);
        }
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        mCursor.moveToPosition(position);
        viewHolder.set(position, mCursor.getLong(PayloadProvider.F_ID), mCursor.getString(PayloadProvider.F_PAYLOAD));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }
}
