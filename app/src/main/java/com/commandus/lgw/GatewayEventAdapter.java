package com.commandus.lgw;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class GatewayEventAdapter extends RecyclerView.Adapter<GatewayEventAdapter.ViewHolder> {
    public ArrayList<GatewayEvent> logData;

    private void addItem(GatewayEvent value) {
        logData.add(value);
        if (logData.size() > 256)
            logData.remove(0);
        notifyDataSetChanged();
    }

    public void push(String message) {
        GatewayEvent e = new GatewayEvent();
        e.message = message;
        addItem(e);
    }

    public void pushPayLoad(Payload payload) {
        GatewayEvent e = new GatewayEvent();
        e.payload = payload;
        addItem(e);
    }

    public void pushReceived(Payload payload) {
        GatewayEvent e = new GatewayEvent();
        e.rawPayload = payload;
        addItem(e);
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView textView;
        private GatewayEvent event;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            textView = view.findViewById(R.id.textViewGatewayEvent);
            view.setOnClickListener(this);
        }

        public void putEvent(GatewayEvent event) {
            this.event = event;
            if (event.hasPayload())
                textView.setTypeface(null, Typeface.BOLD);
            textView.setText(event.toString());
        }

        @Override
        public void onClick(View view) {
            GatewayEventAdapter adapter = (GatewayEventAdapter) getBindingAdapter();
            if (adapter != null) {
                if (event.hasPayload()) {
                    // navigate to payload
                    Context context = view.getContext();
                    Intent intent = new Intent(context, PayloadActivity.class);
                    // add identifier
                    Bundle b = new Bundle();
                    b.putLong(PayloadProvider.FN_ID, event.payload.id);
                    intent.putExtras(b);
                    context.startActivity(intent);
                }
            }
        }
    }

    // Create new views (invoked by the layout manager)

    public GatewayEventAdapter() {
        logData = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.gateway_event_list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        GatewayEvent event = logData.get(position);
        viewHolder.putEvent(event);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return logData.size();
    }
}
