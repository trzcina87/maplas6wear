package trzcina.maplas6.pomocwear;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import trzcina.maplas6.MainActivityWear;

@SuppressWarnings({"PointlessBooleanExpression", "RedundantIfStatement"})
public class UprawnieniaWear {

    //Do dzialania apliacji niezbedne sa 3 uprawnienia
    public static volatile boolean odczyt;
    public static volatile boolean zapis;
    public static volatile boolean lokalizacja;

    //Sprawdza czy uprawnienia sa nadane
    public static boolean czyNadane() {
        if((odczyt == true) && (zapis == true) && (lokalizacja == true)) {
            return true;
        } else {
            return false;
        }
    }

    public static void zainicjujUprawnienia() {
        odczyt = false;
        zapis = false;
        lokalizacja = false;
        List<String> lista = new ArrayList<>(3);

        //W Android >=6 musimy prosic o uprawnienia
        if (Build.VERSION.SDK_INT >= 23) {

            //Sprawdzamy odczyt
            if (sprawdzUprawnienie(Manifest.permission.READ_EXTERNAL_STORAGE) == false) {
                lista.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                odczyt = true;
            }

            //Sprawdzamy zapis
            if (sprawdzUprawnienie(Manifest.permission.WRITE_EXTERNAL_STORAGE) == false) {
                lista.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                zapis = true;
            }

            //Sprawdzamy lokalizacje
            if (sprawdzUprawnienie(Manifest.permission.ACCESS_FINE_LOCATION) == false) {
                lista.add(Manifest.permission.ACCESS_FINE_LOCATION);
            } else {
                lokalizacja = true;
            }

            //Prosimy o brakuje upraweniania
            if(lista.size() > 0) {
                String[] listanowa = new String[lista.size()];
                for(int i = 0; i < lista.size(); i++) {
                    listanowa[i] = lista.get(i);
                }
                poprosUprawnienia(listanowa);
            }
        } else {

            //Dla Android ponizej 6 nie trzeba prosic o uprawnienia
            odczyt = true;
            zapis = true;
            lokalizacja = true;
        }
    }

    //Sprawdzamy czy posiadamy uprawnienie bez pytania
    private static boolean sprawdzUprawnienie(String uprawnienie) {
        int rezultat = ContextCompat.checkSelfPermission(MainActivityWear.activity, uprawnienie);
        if (rezultat == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    //Pokazuje okno userowni zeby zaakceptowal uprawnienia, obsluga w MainActivity
    private static void poprosUprawnienia(String[] uprawnienia) {
        ActivityCompat.requestPermissions(MainActivityWear.activity, uprawnienia, 1);
    }

}

