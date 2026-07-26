package com.fishr.app;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.*;
import android.widget.*;
import androidx.recyclerview.widget.RecyclerView;
import com.fishr.app.utils.CatchEntry;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class CatchAdapter extends RecyclerView.Adapter<CatchAdapter.CatchView> {

    List<CatchEntry> catches;
    Context context;

    public CatchAdapter(List<CatchEntry> catches, Context context) {
        this.catches = catches;
        this.context = context;
    }

    @Override
    public CatchView onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_catch, parent, false);
        return new CatchView(v);
    }

    @Override
    public void onBindViewHolder(CatchView holder, int position) {
        CatchEntry entry = catches.get(position);
        
        holder.species.setText(entry.species);
        
        String details = "";
        if (entry.weight > 0) details += String.format("%.1f kg", entry.weight);
        if (entry.length > 0) details += (details.isEmpty() ? "" : " · ") + String.format("%.0f cm", entry.length);
        holder.details.setText(details);
        
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat out = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            holder.date.setText(out.format(in.parse(entry.timestamp)));
        } catch (Exception e) {
            holder.date.setText(entry.timestamp);
        }

        if (entry.photoPath != null && new File(entry.photoPath).exists()) {
            holder.photo.setImageBitmap(BitmapFactory.decodeFile(entry.photoPath));
        } else {
            holder.photo.setImageResource(R.drawable.ic_fish);
        }

        holder.itemView.setOnClickListener(v -> {
            // TODO: Show detail view
            Toast.makeText(context, entry.species + " - Tap to view", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return catches.size();
    }

    static class CatchView extends RecyclerView.ViewHolder {
        ImageView photo;
        TextView species, details, date;

        CatchView(View itemView) {
            super(itemView);
            photo = itemView.findViewById(R.id.item_photo);
            species = itemView.findViewById(R.id.item_species);
            details = itemView.findViewById(R.id.item_details);
            date = itemView.findViewById(R.id.item_date);
        }
    }
}
