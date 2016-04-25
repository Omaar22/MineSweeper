package omar.theperfectapp;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;

    public ImageAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return MainActivity.GRID_SIZE;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);


            final int width = mContext.getResources().getDisplayMetrics().widthPixels;
            final int height = mContext.getResources().getDisplayMetrics().heightPixels;

            imageView.setLayoutParams(new GridView.LayoutParams(width / (MainActivity.COLUMN_COUNT + 1), height / (MainActivity.ROW_COUNT + 14)));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(R.drawable.field);
        return imageView;
    }

    // references to our images
    public static Integer[] images = {
            R.drawable.mine,
            R.drawable.number_1_outline,
            R.drawable.number_2_outline,
            R.drawable.number_3_outline,
            R.drawable.number_4_outline,
            R.drawable.number_5_outline,
            R.drawable.number_6_outline,
            R.drawable.number_7_outline,
            R.drawable.number_8_outline,
            R.drawable.field,
    };


}