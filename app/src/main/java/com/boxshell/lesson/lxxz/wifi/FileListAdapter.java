package com.boxshell.lesson.lxxz.wifi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.boxshell.lesson.lxxz.R;
import com.boxshell.lesson.lxxz.online.FirmwareListItem;

import java.util.List;

/**
 * Created by yangjun on 16/3/30.
 */
public class FileListAdapter extends ArrayAdapter<FileListItem> {
    public FileListAdapter(Context context, List<FileListItem> items){
        super(context, R.layout.file_list_item, items);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.file_list_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.fileIcon = (ImageView)convertView.findViewById(R.id.fileIcon);
            viewHolder.fileName = (TextView)convertView.findViewById(R.id.fileName);

            convertView.setTag(viewHolder);

        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        // update the item view
        FileListItem  item = getItem(position);
        viewHolder.fileIcon.setImageDrawable(item.fileIcon);
        viewHolder.fileName.setText(item.fileName);

        //return super.getView(position, convertView, parent);
        return convertView;
    }

    private static class ViewHolder {
        ImageView fileIcon;
        TextView fileName;
         }

}
