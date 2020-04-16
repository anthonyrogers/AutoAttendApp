package com.example.autoattendapp;

import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Comparator;
import java.util.PriorityQueue;

public class CourseRecyclerViewAdapter extends RecyclerView.Adapter<CourseRecyclerViewAdapter.ViewHolder>{

    PriorityQueue<ClassInfo> mDeviceQueue;
    private Context context;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    public CourseRecyclerViewAdapter(Context context, PriorityQueue<ClassInfo> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mDeviceQueue = data;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Object[] arr = mDeviceQueue.toArray();
        for (int j = 0; j < arr.length; j++) {
            if (position != j)
                continue;
            holder.textViewName.setText(((ClassInfo) arr[j]).className);
            break;
        }

        //if (mClickListener != null)


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickListener.onItemClick(v, position);
            }
        });

        holder.textViewInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                PopupMenu popup = new PopupMenu(context, holder.textViewInfo);
                popup.inflate(R.menu.options_menu);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menuDelete:
                                mClickListener.onRemoveClick(view, position);
                                break;
                            case R.id.menuModify:
                                mClickListener.onModifyClick(view, position);
                                break;
                            case R.id.menuViewCode:
                                mClickListener.onViewCodeClick(view, position);
                        }
                        return false;
                    }
                });
                popup.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDeviceQueue.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivDeviceIcon;
        public TextView textViewName;
        public TextView textViewInfo;

        ViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.tvDeviceName);
            textViewInfo = itemView.findViewById(R.id.tvDeviceInfo);
        }
    }


    public ClassInfo getClass(int position) {
        return (ClassInfo) mDeviceQueue.toArray()[position];
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);

        void onRemoveClick(View view, int position);

        void onModifyClick(View view, int position);

        void onViewCodeClick(View view, int position);
    }

    public static class ClassInfo{
        public  String className;
        public String classID;
        public ClassInfo(String name, String id){
            className = name;
            classID = id;
        }
    }

    public static class ClassComparator implements Comparator<ClassInfo> {
        @Override
        public int compare(ClassInfo d1, ClassInfo d2) {
            if (d1.className.compareTo(d2.className)>0) {
                return 1;
            } else if (d1.className.compareTo(d2.className)<0) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
