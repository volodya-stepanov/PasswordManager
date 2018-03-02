package com.razrabotkin.android.passwordmanager.data;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.razrabotkin.android.passwordmanager.R;
import com.razrabotkin.android.passwordmanager.ViewerActivity;

import java.util.List;

/**
 * Created by Володя on 02.03.2018.
 */

public class IconAdapter extends ArrayAdapter<ViewerActivity.Icon> {
    private final List<ViewerActivity.Icon> mList;
    private final Activity mContext;

    static class ViewHolder {
        protected ImageView mIcon;
    }

    public IconAdapter(@NonNull Activity context, @NonNull List<ViewerActivity.Icon> list) {
        super(context, R.layout.color_grid_item, list);

        mList = list;
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;

        if (convertView == null){
            LayoutInflater inflater = mContext.getLayoutInflater();
            view = inflater.inflate(R.layout.color_grid_item, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.mIcon = (ImageView) view.findViewById(R.id.image_view_icon);
            view.setTag(viewHolder);
        }else{
            view = convertView;
        }

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.mIcon.setImageResource(mList.get(position).getImageResourceId());
        return view;
    }
}
