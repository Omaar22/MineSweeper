package omar.theperfectapp;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
    // references to our numbers
    public static Integer[] numbers = {
            R.drawable.clear,
            R.drawable.number_1,
            R.drawable.number_2,
            R.drawable.number_3,
            R.drawable.number_4,
            R.drawable.number_5,
            R.drawable.number_6,
            R.drawable.number_7,
            R.drawable.number_8,
    };
    private Context mContext;

    public ImageAdapter(Context c) {
        mContext = c;
    }

    @Override
    public int getCount() {
        return MainActivity.GRID_SIZE;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
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

            imageView.setLayoutParams(new GridView.LayoutParams(width / (MainActivity.COLUMN_COUNT), height / (MainActivity.ROW_COUNT + 10)));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setImageResource(R.drawable.field);
        } else {
            imageView = (ImageView) convertView;
            int row = position / MainActivity.COLUMN_COUNT;
            int column = position % MainActivity.COLUMN_COUNT;

            if (MainActivity.isRevealed[row][column]) {
                if (MainActivity.hasMine[row][column])
                    imageView.setImageResource(R.drawable.mine);
                else
                    imageView.setImageResource(numbers[MainActivity.neighbors[row][column]]);
            } else {
                imageView.setImageResource(R.drawable.field);
            }
        }

        return imageView;
    }


}