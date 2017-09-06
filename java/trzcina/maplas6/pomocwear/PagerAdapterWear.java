package trzcina.maplas6.pomocwear;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import trzcina.maplas6.MainActivityWear;

public class PagerAdapterWear extends PagerAdapter {

    public PagerAdapterWear() {
        super();
    }

    public int getCount() {
        return 4;
    }

    public Object instantiateItem(View collection, int position) {

        View view = null;
        switch (position) {
            case 0:
                view = MainActivityWear.activity.mapapage;
                break;
            case 1:
                view = MainActivityWear.activity.dodaj1page;
                break;
            case 2:
                view = MainActivityWear.activity.podsumowaniepage;
                break;
            case 3:
                view = MainActivityWear.activity.ustawieniapage;
                break;
        }
        ((ViewPager) collection).addView(view, 0);
        return view;
    }

    @Override
    public void destroyItem(View arg0, int arg1, Object arg2) {
        ((ViewPager) arg0).removeView((View) arg2);

    }


    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == ((View) arg1);

    }

    @Override
    public Parcelable saveState() {
        return null;
    }
}
