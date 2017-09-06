package trzcina.maplas6.pomocwear;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import trzcina.maplas6.AppServiceWear;
import trzcina.maplas6.MainActivityWear;
import trzcina.maplas6.lokalizacjawear.PlikiGPXWear;

@SuppressWarnings({"PointlessBooleanExpression", "ForLoopReplaceableByForEach"})
public class WearWear {

    public static String telefon = null;
    private static WearListenerWear wearlistener = null;
    public static GoogleApiClient gac = null;
    public static volatile Location location = null;
    public static int wiadomoscid = 0;
    public static volatile WiadomoscWear[] wiadomosci = new WiadomoscWear[100000];

    public static synchronized WiadomoscWear wyslijWiadomoscICzekajNaOdpowiedz(String nazwa, byte[] dane, int proby, int timeout) {
        WiadomoscWear wiadomosc = new WiadomoscWear(wiadomoscid, nazwa, dane);
        wiadomosci[wiadomoscid] = wiadomosc;
        wiadomoscid = wiadomoscid + 1;
        for(int i = 0; i < proby; i++) {
            wiadomosc.wyslij();
            long start = System.currentTimeMillis();
            while(System.currentTimeMillis() <= start + timeout) {
                RozneWear.czekaj(10);
                if(wiadomosc.odpowiedz != null) {
                    return wiadomosc.odpowiedz;
                }
            }
        }
        return null;
    }

    public static boolean sprawdzCzyPobracPlik(String nazwa, int wielkosc) {
        File plik = new File(StaleWear.SCIEZKAMAPLAS + nazwa);
        if(plik.isFile()) {
            if(plik.length() == wielkosc) {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    private static void pobierzPlikZTelefonu(String nazwa) {
        WiadomoscWear odpowiedz = wyslijWiadomoscICzekajNaOdpowiedz("FILEGET_" + nazwa, null, 3, 1000);
        if(odpowiedz != null) {
            File plik = new File(StaleWear.SCIEZKAMAPLAS + nazwa + ".gpx");
            try {
                OutputStream outputstream = new FileOutputStream(plik);
                outputstream.write(odpowiedz.dane, 0, odpowiedz.dane.length);
                outputstream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void pobierzPlik(String nazwa, int wielkosc) {
        boolean pobrac = sprawdzCzyPobracPlik(nazwa, wielkosc);
        if(pobrac == true) {
            MainActivityWear.activity.ustawInfoPrzygotowanie("Pobieram: " + nazwa);
            pobierzPlikZTelefonu(nazwa);
        }
    }

    public static void pobierzPliki() {
        WiadomoscWear odpowiedz = wyslijWiadomoscICzekajNaOdpowiedz("FILELIST", null, 3, 1000);
        if(odpowiedz != null) {
            String filelist = new String(odpowiedz.dane);
            String[] pliki = filelist.split(":");
            for (int i = 0; i < pliki.length; i++) {
                String[] plik = pliki[i].split("\\^");
                if(plik.length == 2) {
                    pobierzPlik(plik[0], Integer.valueOf(plik[1]));
                }
            }
        }
    }

    public static void zaznaczPliki() {
        WiadomoscWear odpowiedz = wyslijWiadomoscICzekajNaOdpowiedz("ZAZNACZONE", null, 3, 1000);
        if(odpowiedz != null) {
            String filelist = new String(odpowiedz.dane);
            String[] pliki = filelist.split(":");
            PlikiGPXWear.usunZaznaczenieZeWszystkich();
            for(int i = 0; i < pliki.length; i++) {
                if(pliki[i].length() > 0) {
                    PlikiGPXWear.znajdzIZaznaczPlik(pliki[i], true);
                }
            }
        }
    }

    private static String pobierzNode(GoogleApiClient gac) {
        NodeApi.GetConnectedNodesResult result = Wearable.NodeApi.getConnectedNodes(gac).await(StaleWear.GACCONNTIMEOUT, TimeUnit.SECONDS);
        List<Node> nodes = result.getNodes();
        for(int i = 0; i < nodes.size(); i++) {
            return nodes.get(i).getId();
        }
        return null;
    }

    public static GoogleApiClient ustawApi() {
        telefon = null;
        location = new Location("dummyprovider");
        location.setTime(0);
        gac = new GoogleApiClient.Builder(MainActivityWear.activity).addApi(Wearable.API).build();
        gac.blockingConnect(StaleWear.GACCONNTIMEOUT, TimeUnit.SECONDS);
        if(gac.isConnected()) {
            telefon = pobierzNode(gac);
            if (telefon != null) {
                wearlistener = new WearListenerWear();
                Wearable.MessageApi.addListener(gac, wearlistener);
                WiadomoscWear ping = wyslijWiadomoscICzekajNaOdpowiedz("PING", null, 3, 1000);
                if(ping == null) {
                    return null;
                } else {
                    return gac;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static void wylaczAPI() {
        if(wearlistener != null) {
            Wearable.MessageApi.removeListener(gac, wearlistener);
        }
        if(gac != null) {
            gac.disconnect();
        }
        wearlistener = null;
        gac = null;
        telefon = null;
        wiadomoscid = 0;
    }
}
