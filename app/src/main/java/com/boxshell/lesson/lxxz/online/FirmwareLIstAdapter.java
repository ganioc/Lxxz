package com.boxshell.lesson.lxxz.online;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.boxshell.lesson.lxxz.R;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by yangjun on 16/3/6.
 */
public class FirmwareLIstAdapter extends ArrayAdapter<FirmwareListItem> {

    public FirmwareLIstAdapter(Context context, List<FirmwareListItem> items){
        super(context, R.layout.firmware_list_item, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.firmware_list_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.firmwareIcon = (ImageView)convertView.findViewById(R.id.firmwareIcon);
            viewHolder.firmwareName = (TextView)convertView.findViewById(R.id.firmwareName);
            viewHolder.firmwareDescrip = (TextView)convertView.findViewById(R.id.firmwareDescrip);
            convertView.setTag(viewHolder);

        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        // update the item view
        FirmwareListItem  item = getItem(position);
        viewHolder.firmwareIcon.setImageDrawable(item.firmwareIcon);
        viewHolder.firmwareName.setText(item.firmwareName);
        viewHolder.firmwareDescrip.setText(item.firmwareDescrip);

        //return super.getView(position, convertView, parent);
        return convertView;
    }

    private static class ViewHolder {
        ImageView firmwareIcon;
        TextView firmwareName;
        TextView firmwareDescrip;
    }
}
