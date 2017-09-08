package trzcina.maplas6.pomocwear;

import android.util.Log;

import com.google.android.gms.wearable.Wearable;

public class WiadomoscWear {

    public int id;
    public String nazwa;
    public byte[] dane;
    public WiadomoscWear odpowiedz;
    public byte[] tmpodp;
    public volatile int ilesegmentowskopiowanych;

    public WiadomoscWear(int id, String nazwa, byte[] dane) {
        this.id = id;
        this.nazwa = nazwa;
        this.dane = dane;
        odpowiedz = null;
        tmpodp = null;
        ilesegmentowskopiowanych = 0;
    }

    public void wyslij() {
        Wearable.MessageApi.sendMessage(WearWear.gac, WearWear.telefon, nazwa + ":" + id, dane);
    }
}
