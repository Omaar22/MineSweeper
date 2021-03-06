package omar.Minesweeper;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
    // references to my numbers and clear field
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
    Game myGame;

    public ImageAdapter(Context c, Game myGame) {
        mContext = c;
        this.myGame = myGame;
    }

    @Override
    public int getCount() {
        return myGame.GRID_SIZE;
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

            final int screenWidth = mContext.getResources().getDisplayMetrics().widthPixels - 64; // padding 32
            final int screenHeight = mContext.getResources().getDisplayMetrics().heightPixels - 64; // padding 32

            imageView.setLayoutParams(new GridView.LayoutParams(screenWidth / (myGame.COLUMN_COUNT),
                    (screenHeight - (screenWidth / 5) * 2 - 64) / (myGame.ROW_COUNT))); // height - smiley size - grid padding
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setImageResource(R.drawable.field);
        } else {
            // recycled by scrolling or keyboard appearance
            imageView = (ImageView) convertView;
            int row = position / myGame.COLUMN_COUNT;
            int column = position % myGame.COLUMN_COUNT;

            if (myGame.isRevealed[row][column]) {
                if (myGame.hasMine[row][column])
                    imageView.setImageResource(R.drawable.mine);
                else
                    imageView.setImageResource(numbers[myGame.neighbors[row][column]]);
            } else {
                if (myGame.hasFlag[row][column])
                    imageView.setImageResource(R.drawable.flag);
                else if (myGame.hasQuestionMark[row][column])
                    imageView.setImageResource(R.drawable.question_mark);
                else
                    imageView.setImageResource(R.drawable.field);
            }
        }

        return imageView;
    }


}