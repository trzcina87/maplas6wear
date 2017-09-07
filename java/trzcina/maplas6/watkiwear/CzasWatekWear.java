package trzcina.maplas6.watkiwear;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import trzcina.maplas6.MainActivityWear;
import trzcina.maplas6.pomocwear.RozneWear;

@SuppressWarnings("PointlessBooleanExpression")
public class CzasWatekWear extends Thread {

    public volatile boolean zakoncz;
    public DateFormat format;

    public CzasWatekWear() {
        zakoncz = false;
        format = new SimpleDateFormat("HH:mm");

    }

    public void run() {
        Date obecnyczas = new Date(System.currentTimeMillis());
        while(zakoncz == false) {
            if(MainActivityWear.activity.activitywidoczne) {
                obecnyczas.setTime(System.currentTimeMillis());
                MainActivityWear.activity.ustawCzasNaPodsumowaniu();
                MainActivityWear.activity.ustawCzasNaMapie();
            }
            RozneWear.czekaj(500);
        }
    }

}
