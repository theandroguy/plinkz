package com.tushar.plinkz.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tushar.plinkz.Modal.Link;
import com.tushar.plinkz.R;

import java.util.List;

public class LinkAdapter extends RecyclerView.Adapter<LinkAdapter.ViewHolder> {

    private List<Link> linkList;
    private OnLinkDeleteListener onLinkDeleteListener;

    public LinkAdapter(List<Link> linkList, OnLinkDeleteListener onLinkDeleteListener) {
        this.linkList = linkList;
        this.onLinkDeleteListener = onLinkDeleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.link_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Link link = linkList.get(position);
        holder.titleTextView.setText(link.getTitle());
        holder.urlTextView.setText(link.getUrl());

        Context context = holder.itemView.getContext();

        // Open the link in a browser when "Open" button is clicked
        holder.openButton.setOnClickListener(v -> {
            String url = link.getUrl();
            if (url == null || url.isEmpty()) {
                Toast.makeText(context, "URL is empty!", Toast.LENGTH_SHORT).show();
            } else {
                url = ensureValidUrl(url); // Add https:// if missing
                if (Patterns.WEB_URL.matcher(url).matches()) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    context.startActivity(browserIntent);
                } else {
                    Toast.makeText(context, "Invalid URL structure!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Share the link when "Share" button is clicked
        holder.shareButton.setOnClickListener(v -> {
            String url = link.getUrl();
            if (url == null || url.isEmpty()) {
                Toast.makeText(context, "URL is empty!", Toast.LENGTH_SHORT).show();
            } else {
                url = ensureValidUrl(url); // Add https:// if missing
                if (Patterns.WEB_URL.matcher(url).matches()) {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, url);
                    context.startActivity(Intent.createChooser(shareIntent, "Share link using"));
                } else {
                    Toast.makeText(context, "Invalid URL structure!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Delete the link when "Delete" button is clicked
        holder.deleteButton.setOnClickListener(v -> {
            if (onLinkDeleteListener != null) {
                onLinkDeleteListener.onDelete(link); // Trigger the delete action
            }
        });
    }

    @Override
    public int getItemCount() {
        return linkList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView urlTextView;
        public Button openButton;
        public Button shareButton;
        public Button deleteButton; // Link to the delete button

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tvTitle);
            urlTextView = itemView.findViewById(R.id.tvUrl);
            openButton = itemView.findViewById(R.id.btnOpen);
            shareButton = itemView.findViewById(R.id.btnShare);
            deleteButton = itemView.findViewById(R.id.btnDelete); // Link to the delete button
        }
    }

    public interface OnLinkDeleteListener {
        void onDelete(Link link);
    }

    // Method to ensure valid URL by adding "https://" if missing
    private String ensureValidUrl(String url) {
        if (url != null && !url.startsWith("http://") && !url.startsWith("https://")) {
            return "https://" + url; // Prepend https:// if missing
        }
        return url;
    }
}
