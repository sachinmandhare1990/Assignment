package com.assignment.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.assignment.R;
import com.assignment.data.model.StopUiModel;
import com.assignment.databinding.ItemStopBinding;

import java.util.ArrayList;
import java.util.List;

public class StopsAdapter extends RecyclerView.Adapter<StopsAdapter.ViewHolder> {

    private final OnItemClickListener listener;

    private List<StopUiModel> stopList = new ArrayList<>();

    protected StopsAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<StopUiModel> stops) {
        this.stopList = stops != null ? stops : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStopBinding binding = ItemStopBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(stopList.get(position));
    }

    @Override
    public int getItemCount() {
        return stopList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(StopUiModel model);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemStopBinding binding;

        public ViewHolder(ItemStopBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(V -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(stopList.get(position));
                }
            });
        }

        public void bind(StopUiModel model) {
            binding.tvStopName.setText(model.stop.getName());
            binding.tvStopType.setText(model.stop.getType().toUpperCase());
            binding.tvStopDistance.setText(model.getDistanceText());

            if ("metro".equalsIgnoreCase(model.stop.getType())) {
                binding.ivStop.setImageResource(R.drawable.ic_metro);
            } else {
                binding.ivStop.setImageResource(R.drawable.ic_bus);
            }

        }
    }
}
