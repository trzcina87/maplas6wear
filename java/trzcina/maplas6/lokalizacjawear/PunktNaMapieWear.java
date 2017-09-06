package trzcina.maplas6.lokalizacjawear;

import android.graphics.Rect;
import android.location.Location;

import trzcina.maplas6.pomocwear.PaintyWear;

public class PunktNaMapieWear {

    public float wspx;
    public float wspy;
    public String nazwa;
    public String opis;
    public Rect rectnazwa;
    public Rect rectopis;
    public Location lokalizacja;
    public Location lokalizacjamierzona;

    public PunktNaMapieWear(float wspx, float wspy, String nazwa, String opis) {
        this.wspx = wspx;
        this.wspy = wspy;
        this.nazwa = nazwa;
        this.opis = opis;
        rectnazwa = new Rect();
        rectopis = new Rect();
        if(nazwa != null) {
            PaintyWear.paintbialytekst.getTextBounds(nazwa, 0, nazwa.length(), rectnazwa);
        }
        if(opis != null) {
            PaintyWear.paintbialytekst.getTextBounds(opis, 0, opis.length(), rectopis);
        }
        lokalizacja = new Location("dummyprovider");
        lokalizacja.setLongitude(wspx);
        lokalizacja.setLatitude(wspy);
        lokalizacjamierzona = new Location("dummyprovider");
    }

    public float zmierzDystans(double dowspx, double dowspy) {
        lokalizacjamierzona.setLongitude(dowspx);
        lokalizacjamierzona.setLatitude(dowspy);
        return lokalizacja.distanceTo(lokalizacjamierzona);
    }
}
