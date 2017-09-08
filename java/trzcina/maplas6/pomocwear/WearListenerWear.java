package trzcina.maplas6.pomocwear;

import android.util.Log;

import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.util.Arrays;

import trzcina.maplas6.AppServiceWear;
import trzcina.maplas6.MainActivityWear;
import trzcina.maplas6.lokalizacjawear.GPXPunktWear;
import trzcina.maplas6.lokalizacjawear.PlikiGPXWear;

@SuppressWarnings("PointlessBooleanExpression")
public class WearListenerWear implements MessageApi.MessageListener {

    private static int sufitZInta(int a, int dzielnik) {
        if(a % dzielnik == 0) {
            return a / dzielnik;
        } else {
            return (a / dzielnik) + 1;
        }
    }

    private void obsluzOdpowiedz(MessageEvent messageEvent) {
        String tab[] = messageEvent.getPath().split(":");
        if(tab.length == 2) {
            String nazwa = tab[0];
            int wiadomoscid = Integer.parseInt(tab[1]);
            WiadomoscWear wiadomosc = new WiadomoscWear(wiadomoscid, nazwa, messageEvent.getData());
            WearWear.wiadomosci[wiadomoscid].odpowiedz = wiadomosc;
        }
        if(tab.length == 4) {
            String nazwa = tab[0];
            int wiadomoscid = Integer.parseInt(tab[1]);
            int dlugoscwiadomsci = Integer.parseInt(tab[2]);
            int segment = Integer.parseInt(tab[3]);
            synchronized (MainActivityWear.activity) {
                if(WearWear.wiadomosci[wiadomoscid].tmpodp == null) {
                    WearWear.wiadomosci[wiadomoscid].tmpodp = new byte[dlugoscwiadomsci];
                }
                System.arraycopy(messageEvent.getData(), 0, WearWear.wiadomosci[wiadomoscid].tmpodp, segment * StaleWear.GACDATALIIT, messageEvent.getData().length);
                WearWear.wiadomosci[wiadomoscid].ilesegmentowskopiowanych = WearWear.wiadomosci[wiadomoscid].ilesegmentowskopiowanych + 1;
                if(WearWear.wiadomosci[wiadomoscid].ilesegmentowskopiowanych == sufitZInta(dlugoscwiadomsci, StaleWear.GACDATALIIT)) {
                    WiadomoscWear wiadomosc = new WiadomoscWear(wiadomoscid, nazwa, WearWear.wiadomosci[wiadomoscid].tmpodp);
                    WearWear.wiadomosci[wiadomoscid].odpowiedz = wiadomosc;
                }
            }
        }
    }

    private void obsluzGPS(MessageEvent messageEvent) {
        if(messageEvent.getPath().equals("GPS")) {
            String tab[] = new String(messageEvent.getData()).split(":");
            WearWear.location.setLongitude(Float.parseFloat(tab[0]));
            WearWear.location.setLatitude(Float.parseFloat(tab[1]));
            WearWear.location.setTime(Long.parseLong(tab[2]));
            AppServiceWear.service.przesunMapeZGPS(WearWear.location);
            MainActivityWear.activity.wypelnijPodsumowanie(false);
            MainActivityWear.activity.wypelnijPodsumowanieNaWidokuMapy();
        }
    }

    private void obsluzGPSTRASA(MessageEvent messageEvent) {
        if(messageEvent.getPath().equals("GPSTRASA")) {
            if(AppServiceWear.service.obecnatrasapobrana == true) {
                String tab[] = new String(messageEvent.getData()).split(":");
                WearWear.location.setLongitude(Float.parseFloat(tab[0]));
                WearWear.location.setLatitude(Float.parseFloat(tab[1]));
                WearWear.location.setTime(Long.parseLong(tab[2]));
                if (AppServiceWear.service.obecnatrasa != null) {
                    AppServiceWear.service.obecnatrasa.dodajPunkt(Float.parseFloat(tab[0]), Float.parseFloat(tab[1]));
                }
                AppServiceWear.service.przesunMapeZGPS(WearWear.location);
                MainActivityWear.activity.wypelnijPodsumowanie(false);
                MainActivityWear.activity.wypelnijPodsumowanieNaWidokuMapy();
            }
        }
    }

    private void obsluzGPSPUNKT(MessageEvent messageEvent) {
        if(messageEvent.getPath().equals("GPSPUNKT")) {
            String tab[] = new String(messageEvent.getData()).split(":");
            GPXPunktWear.dodajPunkt(Float.parseFloat(tab[2]), Float.parseFloat(tab[3]), tab[0], tab[1], 0);
            AppServiceWear.service.rysujwatek.odswiez = true;
        }
    }

    private void obsluzNOWATRASA(MessageEvent messageEvent) {
        if(messageEvent.getPath().equals("NOWATRASA")) {
            AppServiceWear.service.zacznijNowaTrase();
            MainActivityWear.activity.wypelnijPodsumowanie(false);
            MainActivityWear.activity.wypelnijPodsumowanieNaWidokuMapy();
        }
    }

    private void obsluzNOWEZAZNACZONE(MessageEvent messageEvent) {
        if(messageEvent.getPath().equals("NOWEZAZNACZONE")) {
            String filelist = new String(messageEvent.getData());
            String[] pliki = filelist.split(":");
            PlikiGPXWear.usunZaznaczenieZeWszystkich();
            for(int i = 0; i < pliki.length; i++) {
                if(pliki[i].length() > 0) {
                    PlikiGPXWear.znajdzIZaznaczPlik(pliki[i], true);
                }
            }
            AppServiceWear.service.rysujwatek.odswiez = true;
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        obsluzOdpowiedz(messageEvent);
        obsluzGPS(messageEvent);
        obsluzGPSTRASA(messageEvent);
        obsluzGPSPUNKT(messageEvent);
        obsluzNOWATRASA(messageEvent);
        obsluzNOWEZAZNACZONE(messageEvent);
    }
}
