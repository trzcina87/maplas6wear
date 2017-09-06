package trzcina.maplas6.atlasywear;

import android.location.Location;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import trzcina.maplas6.MainActivityWear;
import trzcina.maplas6.pomocwear.StaleWear;

public class AtlasyWear {

    //Globalna lista atlasow
    public static List<AtlasWear> atlasy;

    //Szukamy wszystkich atlasow w katalogu z mapami
    public static void szukajAtlasow() {
        atlasy = new ArrayList<>(50);
        File[] katalogi = new File(StaleWear.FOLDERMAPYWEAR).listFiles();
        if(katalogi != null) {

            //Sortujemy atlasy alfabetycznie
            Arrays.sort(katalogi, new Comparator<File>() {
                @Override
                public int compare(File file, File t1) {
                    return file.getName().toLowerCase().compareTo(t1.getName().toLowerCase());
                }
            });

            //Dla kazdego katalogu w katalogu z mapami tworzymy atlas
            for(int i = 0; i < katalogi.length; i++) {
                if(katalogi[i].isDirectory()) {

                    //Parsowanie atlasu i dodawnie do listy jesli sparsowal sie dobrze
                    AtlasWear atlas = new AtlasWear(katalogi[i].getName());
                    MainActivityWear.activity.ustawInfoPrzygotowanie("Parsuje: " + katalogi[i].getName());
                    try {
                        atlas.parsuj();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(atlas.stan == StaleWear.ATLASGOTOWY) {
                        atlasy.add(atlas);
                    }
                }
            }
        }
    }

    public static AtlasWear znajdzAtlasPoNazwie(String nazwa) {
        for(int i = 0; i < atlasy.size(); i++) {
            if(atlasy.get(i).nazwa.equals(nazwa)) {
                return atlasy.get(i);
            }
        }
        return null;
    }

    public static AtlasWear szukajNajlepszejMapy(Location location) {
        AtlasWear propozcyja = null;
        double dokladnosc = 1000000;
        for(int i = 0; i < atlasy.size(); i++) {
            if(atlasy.get(i).czyWspolrzedneWewnatrz((float)location.getLongitude(), (float)location.getLatitude())) {
                if(atlasy.get(i).pobierzDokladnosc() < dokladnosc) {
                    propozcyja = atlasy.get(i);
                    dokladnosc = atlasy.get(i).pobierzDokladnosc();
                }
            }
        }
        return propozcyja;
    }
}
