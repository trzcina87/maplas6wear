package trzcina.maplas6.pomocwear;

import android.util.Log;

import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;

import trzcina.maplas6.AppServiceWear;
import trzcina.maplas6.lokalizacjawear.GPXPunktWear;
import trzcina.maplas6.lokalizacjawear.PlikiGPXWear;

public class WearListenerWear implements MessageApi.MessageListener {

    private void obsluzOdpowiedz(MessageEvent messageEvent) {
        String tab[] = messageEvent.getPath().split(":");
        if(tab.length == 2) {
            String nazwa = tab[0];
            int wiadomoscid = Integer.parseInt(tab[1]);
            WiadomoscWear wiadomosc = new WiadomoscWear(wiadomoscid, nazwa, messageEvent.getData());
            WearWear.wiadomosci[wiadomoscid].odpowiedz = wiadomosc;
        }
    }

    private void obsluzGPS(MessageEvent messageEvent) {
        if(messageEvent.getPath().equals("GPS")) {
            String tab[] = new String(messageEvent.getData()).split(":");
            WearWear.location.setLongitude(Float.parseFloat(tab[0]));
            WearWear.location.setLatitude(Float.parseFloat(tab[1]));
            WearWear.location.setTime(Long.parseLong(tab[2]));
            AppServiceWear.service.przesunMapeZGPS(WearWear.location);
        }
    }

    private void obsluzGPSTRASA(MessageEvent messageEvent) {
        if(messageEvent.getPath().equals("GPSTRASA")) {
            String tab[] = new String(messageEvent.getData()).split(":");
            WearWear.location.setLongitude(Float.parseFloat(tab[0]));
            WearWear.location.setLatitude(Float.parseFloat(tab[1]));
            WearWear.location.setTime(Long.parseLong(tab[2]));
            if(AppServiceWear.service.obecnatrasa != null) {
                AppServiceWear.service.obecnatrasa.dodajPunkt(Float.parseFloat(tab[0]), Float.parseFloat(tab[1]));
            }
            AppServiceWear.service.przesunMapeZGPS(WearWear.location);
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
