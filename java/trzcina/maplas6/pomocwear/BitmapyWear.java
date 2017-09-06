package trzcina.maplas6.pomocwear;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import trzcina.maplas6.MainActivityWear;
import trzcina.maplas6.R;

public class BitmapyWear {

    public static Bitmap strzalka;
    public static Bitmap brakmapy;
    public static Bitmap kursorgps;

    //Wczytujemy wszystkie bitmapy uzywane w programie do pamieci
    public static void inicjujBitmapy() {
        strzalka = BitmapFactory.decodeResource(MainActivityWear.activity.getResources(), R.mipmap.strzalka100);
        brakmapy = BitmapFactory.decodeResource(MainActivityWear.activity.getResources(), R.mipmap.brakmapy);
        kursorgps = BitmapFactory.decodeResource(MainActivityWear.activity.getResources(), R.mipmap.gps);
    }
}
