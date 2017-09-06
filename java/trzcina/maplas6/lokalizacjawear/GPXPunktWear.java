package trzcina.maplas6.lokalizacjawear;

import java.util.ArrayList;
import java.util.List;

public class GPXPunktWear {

    public static List<PunktNaMapieWear> lista = new ArrayList<>(1000);

    public static boolean dodajPunkt(float wspx, float wspy, String nazwa, String komentarz, float dokladnosc) {
        lista.add(new PunktNaMapieWear(wspx, wspy, nazwa, komentarz));
        return true;
    }
}
