package trzcina.maplas6.watkiwear;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Surface;
import android.view.WindowManager;

import trzcina.maplas6.AppServiceWear;
import trzcina.maplas6.MainActivityWear;
import trzcina.maplas6.pomocwear.RozneWear;

@SuppressWarnings("PointlessBooleanExpression")
public class KompasWatekWear extends Thread implements SensorEventListener {

    public volatile boolean zakoncz;    //Info czy zakonczyc
    public volatile int kat;            //Odczyt katu
    public volatile int dokladnosc;     //Dokladnosc kompasu
    private WindowManager windowmanager;


    private int zwrocKatUrzadzenia() {
        int rotation = windowmanager.getDefaultDisplay().getRotation();
        if (Surface.ROTATION_0 == rotation) {
            return 0;
        }
        if(Surface.ROTATION_180 == rotation) {
            return 180;
        }
        if(Surface.ROTATION_90 == rotation) {
            return 90;
        }
        if(Surface.ROTATION_270 == rotation) {
            return 270;
        }
        return 0;
    }


    public KompasWatekWear() {
        kat = 0;
        dokladnosc = -1;
        zakoncz = false;
        windowmanager = (WindowManager) MainActivityWear.activity.getSystemService(Context.WINDOW_SERVICE);
    }

    //Odczyt wartosci kata, jesli nowy to odswiezamy rysunek
    @Override
    public void onSensorChanged(SensorEvent event) {
        int kattmp = (Math.round(event.values[0]) + zwrocKatUrzadzenia()) % 360;
        if(kattmp != kat) {
            kat = kattmp;
            AppServiceWear.service.rysujwatek.odswiez = true;
        }
    }

    //Zmiana wartosci dokladnosci
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        dokladnosc = i;
        AppServiceWear.service.rysujwatek.odswiez = true;
    }

    //Rejestracja kompasu i wyrejestrowanie po zakonczeniu
    public void run() {
        SensorManager sensory = (SensorManager)MainActivityWear.activity.getSystemService(MainActivityWear.SENSOR_SERVICE);
        sensory.registerListener(this, sensory.getDefaultSensor(Sensor.TYPE_ORIENTATION), 100000);
        while(zakoncz == false) {
            RozneWear.czekaj(1000);
        }
        sensory.unregisterListener(this);
    }
}
