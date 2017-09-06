package trzcina.maplas6.watkiwear;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import trzcina.maplas6.MainActivityWear;
import trzcina.maplas6.pomocwear.RozneWear;

public class CzasWatekWear extends Thread {

    public volatile boolean zakoncz;

    public CzasWatekWear() {
        zakoncz = false;
    }

    public void run() {
        while(zakoncz == false) {
            final Date obecnyczas = new Date(System.currentTimeMillis());
            final DateFormat format = new SimpleDateFormat("HH:mm");
            MainActivityWear.activity.uzupelnijCzas(format.format(obecnyczas));
            if(MainActivityWear.activity.isAmbient() == false) {
                MainActivityWear.activity.ustawCzasNaPodsumowaniu();
            }
            RozneWear.czekaj(500);
        }
    }

}
