package trzcina.maplas6.lokalizacjawear;

import android.location.Location;

public class PunktWTrasieWear {

    public float wspx;
    public float wspy;
    public int czassekundy;

    public PunktWTrasieWear(float wspx, float wspy, int czassekundy) {
        this.wspx = wspx;
        this.wspy = wspy;
        this.czassekundy = czassekundy;
    }

    public static float zmierzDystans(PunktWTrasieWear p1, PunktWTrasieWear p2) {
        Location lok1 = new Location("dummyprovider");
        Location lok2 = new Location("dummyprovider");
        lok1.setLongitude(p1.wspx);
        lok1.setLatitude(p1.wspy);
        lok2.setLongitude(p2.wspx);
        lok2.setLatitude(p2.wspy);
        return lok1.distanceTo(lok2);
    }
}
