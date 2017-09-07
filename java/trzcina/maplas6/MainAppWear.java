package trzcina.maplas6;

import android.app.Application;

import java.io.File;
import java.io.PrintStream;

import trzcina.maplas6.pomocwear.StaleWear;

public class MainAppWear extends Application {

    private static void zapiszDoPliku(Throwable wyjatek) {
        try {
            File file = new File(StaleWear.SCIEZKAMAPLAS + System.currentTimeMillis() + ".crash");
            PrintStream ps = new PrintStream(file);
            wyjatek.printStackTrace(ps);
            ps.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final Thread.UncaughtExceptionHandler obsluga = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
                zapiszDoPliku(e);
                if(AppServiceWear.service != null) {
                    AppServiceWear.service.zakonczUsluge();
                }
                obsluga.uncaughtException(t, e);
            }
        });
    }

}
