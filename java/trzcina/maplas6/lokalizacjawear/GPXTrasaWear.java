package trzcina.maplas6.lokalizacjawear;

public class GPXTrasaWear {

    public PunktWTrasieWear[] lista;
    public int iloscpunktow;
    public float dlugosctrasy;
    public float odlegloscodpoczatku;
    public long czasstart;

    public GPXTrasaWear() {
        lista = new PunktWTrasieWear[50000];
        iloscpunktow = 0;
        dlugosctrasy = 0;
        odlegloscodpoczatku = 0;
        czasstart = System.currentTimeMillis();
    }

    public synchronized boolean dodajPunkt(float wspx, float wspy) {
        try {
            lista[iloscpunktow] = new PunktWTrasieWear(wspx, wspy, 0);
            iloscpunktow = iloscpunktow + 1;
            if(iloscpunktow > 1) {
                dlugosctrasy = dlugosctrasy + PunktWTrasieWear.zmierzDystans(lista[iloscpunktow -1], lista[iloscpunktow -2]);
            }
            odlegloscodpoczatku = PunktWTrasieWear.zmierzDystans(lista[iloscpunktow - 1], lista[0]);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
