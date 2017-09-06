package trzcina.maplas6;

import android.app.Application;

public class MainAppWear extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        final Thread.UncaughtExceptionHandler obsluga = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
                if(AppServiceWear.service != null) {
                    AppServiceWear.service.zakonczUsluge();
                }
                obsluga.uncaughtException(t, e);
            }
        });
    }

}
