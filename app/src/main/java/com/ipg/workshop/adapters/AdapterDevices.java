package com.ipg.workshop.adapters;

import android.bluetooth.BluetoothDevice;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ipg.workshop.R;
import com.ipg.workshop.databinding.ItemDeviceBinding;

import java.util.ArrayList;

public class AdapterDevices extends RecyclerView.Adapter<AdapterDevices.ViewHolder> {
    private ArrayList<BluetoothDevice> devices;
    private OnDevicesClickListener listener;

    public AdapterDevices(ArrayList<BluetoothDevice> devices, OnDevicesClickListener listener) {
        this.devices = devices;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BluetoothDevice device = devices.get(position);
        holder.bind(device);
    }

    @Override
    public int getItemCount() {
        return devices == null ? 0 : devices.size();
    }

    public void addDevice(BluetoothDevice device){
        if (!devices.contains(device)){
            devices.add(device);
            notifyDataSetChanged();
        }
    }

    public void removeDevice(BluetoothDevice device){
        if (devices.contains(device)){
            devices.remove(device);
            notifyDataSetChanged();
        }
    }

    public void clear(){
        devices.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ItemDeviceBinding binding;

        ViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);

            if (binding != null && listener != null) {
                binding.getRoot().setOnClickListener(view -> listener.onDeviceClick(devices.get(getAdapterPosition())));
            }
        }

        void bind(BluetoothDevice device){
            binding.setDevice(device);
        }
    }

    public interface OnDevicesClickListener {
        void onDeviceClick(BluetoothDevice device);
    }
}
