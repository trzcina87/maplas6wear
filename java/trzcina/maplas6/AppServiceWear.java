package trzcina.maplas6;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import trzcina.maplas6.atlasywear.AtlasWear;
import trzcina.maplas6.atlasywear.AtlasyWear;
import trzcina.maplas6.atlasywear.TmiParserWear;
import trzcina.maplas6.lokalizacjawear.GPXPunktWear;
import trzcina.maplas6.lokalizacjawear.GPXTrasaWear;
import trzcina.maplas6.lokalizacjawear.PlikiGPXWear;
import trzcina.maplas6.pomocwear.BitmapyWear;
import trzcina.maplas6.pomocwear.KomunikatyWear;
import trzcina.maplas6.pomocwear.PaintyWear;
import trzcina.maplas6.pomocwear.PrzygotowanieWear;
import trzcina.maplas6.pomocwear.RozneWear;
import trzcina.maplas6.pomocwear.StaleWear;
import trzcina.maplas6.pomocwear.WearWear;
import trzcina.maplas6.pomocwear.WiadomoscWear;
import trzcina.maplas6.watkiwear.CzasWatekWear;
import trzcina.maplas6.watkiwear.KompasWatekWear;
import trzcina.maplas6.watkiwear.RysujWatekWear;
import trzcina.maplas6.watkiwear.WczytajWatekWear;

@SuppressWarnings("PointlessBooleanExpression")
public class AppServiceWear extends Service {

    public static volatile AppServiceWear service;
    private boolean wystartowany;                           //Czy serwis juz dziala
    public static volatile int widok = StaleWear.WIDOKBRAK;
    public List<String> listaplikowaplikacji;
    public long[] plikmerkatora;

    public KompasWatekWear kompaswatek;
    public RysujWatekWear rysujwatek;
    public WczytajWatekWear wczytajwatek;
    public CzasWatekWear czaswatek;

    public int zoom;
    public int kolorinfo;
    public Point srodekekranu;
    public Point pixelnamapienadsrodkiem;
    public AtlasWear atlas;
    public TmiParserWear tmiparser;
    public volatile GPXTrasaWear obecnatrasa;
    public volatile List<GPXTrasaWear> obecnetrasy;
    public int poziominfo;
    public volatile boolean obecnatrasapobrana;

    public AppServiceWear() {
        service = this;
        wystartowany = false;
        listaplikowaplikacji = null;
        plikmerkatora = null;
        kompaswatek = null;
        rysujwatek = null;
        wczytajwatek = null;
        czaswatek = null;
        zoom = 10;
        kolorinfo = 0;
        srodekekranu = new Point();
        srodekekranu.x = 160;
        srodekekranu.y = 160;
        pixelnamapienadsrodkiem = new Point();
        atlas = null;
        tmiparser = null;
        obecnatrasa = new GPXTrasaWear();
        poziominfo = 1;
        obecnatrasapobrana = false;
        obecnetrasy = new ArrayList<>(20);
        obecnetrasy.add(obecnatrasa);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public Location czyJestFix() {
        if (WearWear.location.getTime() + 10000 >= System.currentTimeMillis()) {
            return new Location(WearWear.location);
        } else {
            return null;
        }
    }

    public void wyslijPunktDoTelefonu(String nazwa) {
        WiadomoscWear wiadomosc = WearWear.wyslijWiadomoscICzekajNaOdpowiedz("POINT", nazwa.getBytes(), 3, 1000);
        if(wiadomosc == null) {
            MainActivityWear.activity.pokazToast("Brak połączenia z telefonem!");
        } else {
            String daneodpowiedz = new String(wiadomosc.dane);
            if(daneodpowiedz.startsWith("TRUE")) {
                MainActivityWear.activity.pokazToast("Zapisano: " + nazwa);
            } else {
                MainActivityWear.activity.pokazToast("Błąd zapisu na telefonie!");
            }
        }
    }

    public void zmienKolorInfo() {
        if(kolorinfo == 0) {
            kolorinfo = 3;
        } else {
            kolorinfo = 0;
        }
        rysujwatek.odswiez = true;
    }

    public void zacznijNowaTrase() {
        obecnatrasa = new GPXTrasaWear();
        obecnetrasy.add(obecnatrasa);
        rysujwatek.odswiez = true;
    }

    private void wysrodkujDoLokalizacji(Location lokalizacja) {
        float gpsx = RozneWear.zaokraglij5((float) lokalizacja.getLongitude());
        float gpsy = RozneWear.zaokraglij5((float) lokalizacja.getLatitude());
        int x = tmiparser.obliczPixelXDlaWspolrzednej(gpsx);
        int y = tmiparser.obliczPixelYDlaWspolrzednej(gpsy);
        pixelnamapienadsrodkiem.set(x, y);
        poprawPixelNadSrodkiem();
        odswiezUI();
        rysujwatek.odswiez = true;
    }

    public void przesunMapeZGPS(Location lokalizacja) {
        if(tmiparser != null) {
            if(lokalizacja != null) {
                wysrodkujDoLokalizacji(lokalizacja);
            } else {
                MainActivityWear.activity.pokazToast("Sprawdz GPS!");
            }
        }
    }

    private void wystartujWatekPrzygotowania() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                PaintyWear.inicjujPainty();

                //Laczymy do API
                MainActivityWear.activity.ustawProgressPrzygotowanie(1);
                MainActivityWear.activity.ustawInfoPrzygotowanie("Lacze z telefonem...");
                GoogleApiClient gac = WearWear.ustawApi();
                if(gac != null) {

                    WearWear.wyslijSTARTGPS();

                    MainActivityWear.activity.ustawProgressPrzygotowanie(2);
                    MainActivityWear.activity.ustawInfoPrzygotowanie("Pobieram pliki...");
                    WearWear.pobierzPliki();

                    listaplikowaplikacji = Arrays.asList(MainActivityWear.activity.fileList());

                    //Tworzymy niezbene katalogi
                    MainActivityWear.activity.ustawProgressPrzygotowanie(3);
                    MainActivityWear.activity.ustawInfoPrzygotowanie("Tworze katalogi...");
                    PrzygotowanieWear.utworzKatalogi();

                    //Wczytujemy bitmapy do pamieci
                    MainActivityWear.activity.ustawProgressPrzygotowanie(4);
                    MainActivityWear.activity.ustawInfoPrzygotowanie("Wczytuje bitmapy...");
                    BitmapyWear.inicjujBitmapy();

                    MainActivityWear.activity.ustawProgressPrzygotowanie(5);
                    MainActivityWear.activity.ustawInfoPrzygotowanie("Wczytuje plik Merkatora...");
                    wczytajPlikMerkatora();

                    //Wczytujemy dostepne atlasy
                    MainActivityWear.activity.ustawProgressPrzygotowanie(6);
                    MainActivityWear.activity.ustawInfoPrzygotowanie("Wczytuje atlasy...");
                    AtlasyWear.szukajAtlasow();

                    //Wczytujemy dostepne pliki
                    MainActivityWear.activity.ustawProgressPrzygotowanie(7);
                    MainActivityWear.activity.ustawInfoPrzygotowanie("Wczytuje pliki...");
                    PlikiGPXWear.szukajPlikow();

                    MainActivityWear.activity.ustawProgressPrzygotowanie(8);
                    MainActivityWear.activity.ustawInfoPrzygotowanie("Pobieram konfigurację...");
                    WearWear.zaznaczPliki();

                    GPXPunktWear.inicjuj();
                    WearWear.pobierzObecnePunktyWSesji();
                    WearWear.pobierzObecnaTrase();

                    MainActivityWear.activity.zakonczPrzygotowanie();
                    wystartujWatkiProgramu();
                    zaczytajOpcjePierwszyRaz(false, 0, 0);
                    pokazPierwszyToast();
                } else {
                    MainActivityWear.activity.pokazToast("Brak polaczenia z telefonem...");
                    RozneWear.czekaj(1000);
                    MainActivityWear.activity.zakonczCalaAplikacje();
                }
            }
        }).start();
    }

    public void zmienPoziomInfo() {
        poziominfo = (poziominfo + 1) % 6;
        if(poziominfo == StaleWear.OPISYBRAK) {
            MainActivityWear.activity.pokazToast("Brak punktow");
        }
        if(poziominfo == StaleWear.OPISYPUNKTY) {
            MainActivityWear.activity.pokazToast("Tylko punkty");
        }
        if(poziominfo == StaleWear.OPISYNAZWY) {
            MainActivityWear.activity.pokazToast("Punkty i nazwa");
        }
        if(poziominfo == StaleWear.OPISYKOMENTARZE) {
            MainActivityWear.activity.pokazToast("Punkty nazwy i opisy");
        }
        if(poziominfo == StaleWear.OPISYODLEGLOSCI) {
            MainActivityWear.activity.pokazToast("Punkty nazwy i odległości");
        }
        if(poziominfo == StaleWear.OPISYODLEGLOSCIKOMENTARZE) {
            MainActivityWear.activity.pokazToast("Punkty nazwy opisy i odległości");
        }
        if(poziominfo == StaleWear.OPISYBRAK) {
            MainActivityWear.activity.pokazIkoneOpisowWylaczonych();
        } else {
            MainActivityWear.activity.pokazIkoneOpisow();
        }
        rysujwatek.odswiez = true;
    }

    public void zaczytajOpcjePierwszyRaz(boolean zachowajwspolrzene, float gpsx, float gpsy) {
        if(AtlasyWear.atlasy.size() > 0) {
            AtlasWear nowyatlas = AtlasyWear.atlasy.get(0);
            if ((nowyatlas != atlas) || (nowyatlas == null)) {
                atlas = nowyatlas;
                if (atlas != null) {
                    tmiparser = atlas.parserytmi.get(atlas.parserytmi.size() - 1);
                    if (zachowajwspolrzene == false) {
                        pixelnamapienadsrodkiem.set(tmiparser.rozmiarmapy.x / 2, tmiparser.rozmiarmapy.y / 2);
                    } else {
                        pixelnamapienadsrodkiem.set(tmiparser.obliczPixelXDlaWspolrzednej(gpsx), tmiparser.obliczPixelYDlaWspolrzednej(gpsy));
                    }
                }
                zoom = 10;
                wczytajwatek.przeladujkonfiguracje = true;
                rysujwatek.przeladujkonfiguracje = true;
            }
        }
        rysujwatek.odswiez = true;
    }

    public void zaczytajOpcje(boolean zachowajwspolrzene, float gpsx, float gpsy) {
        if (atlas != null) {
            tmiparser = atlas.parserytmi.get(atlas.parserytmi.size() - 1);
            if (zachowajwspolrzene == false) {
                pixelnamapienadsrodkiem.set(tmiparser.rozmiarmapy.x / 2, tmiparser.rozmiarmapy.y / 2);
            } else {
                pixelnamapienadsrodkiem.set(tmiparser.obliczPixelXDlaWspolrzednej(gpsx), tmiparser.obliczPixelYDlaWspolrzednej(gpsy));
            }
        }
        zoom = 10;
        wczytajwatek.przeladujkonfiguracje = true;
        rysujwatek.przeladujkonfiguracje = true;
        rysujwatek.odswiez = true;
    }

    private boolean sprawdzCacheMerkatora() {
        if(listaplikowaplikacji.contains(StaleWear.SUFFIXCACHEMERKATOR)) {
            return true;
        } else {
            return false;
        }
    }

    private void wystartujWatkiProgramu() {
        kompaswatek = new KompasWatekWear();
        rysujwatek = new RysujWatekWear();
        wczytajwatek = new WczytajWatekWear();
        czaswatek = new CzasWatekWear();
        kompaswatek.start();
        rysujwatek.start();
        wczytajwatek.start();
        czaswatek.start();
    }

    private void odswiezUI() {

    }

    public void poprawPixelNadSrodkiem() {
        if(pixelnamapienadsrodkiem.x < 0) {
            pixelnamapienadsrodkiem.x = 0;
        }
        if(pixelnamapienadsrodkiem.y < 0) {
            pixelnamapienadsrodkiem.y = 0;
        }
        if(atlas != null) {
            if(pixelnamapienadsrodkiem.x >= tmiparser.rozmiarmapy.x) {
                pixelnamapienadsrodkiem.x = tmiparser.rozmiarmapy.x - 1;
            }
            if(pixelnamapienadsrodkiem.y >= tmiparser.rozmiarmapy.y) {
                pixelnamapienadsrodkiem.y = tmiparser.rozmiarmapy.y - 1;
            }
        }
    }

    public void powiekszMape() {
        if(atlas == null) {
            MainActivityWear.activity.pokazToast(KomunikatyWear.BRAKATLASOW);
        } else {
            int index = atlas.parserytmi.indexOf(tmiparser);
            if(index == atlas.parserytmi.size() - 1) {
                if(zoom < 80) {
                    zoom = zoom + 5;
                    rysujwatek.przeladujkonfiguracje = true;
                    rysujwatek.odswiez = true;
                } else {
                    MainActivityWear.activity.pokazToast(KomunikatyWear.BLADPRZYBLIZANIA);
                }
            } else {
                float gpsx = tmiparser.obliczWspolrzednaXDlaPixela(pixelnamapienadsrodkiem.x);
                float gpsy = tmiparser.obliczWspolrzednaYDlaPixela(pixelnamapienadsrodkiem.y);
                tmiparser = atlas.parserytmi.get(index + 1);
                pixelnamapienadsrodkiem.set(tmiparser.obliczPixelXDlaWspolrzednej(gpsx), tmiparser.obliczPixelYDlaWspolrzednej(gpsy));
                poprawPixelNadSrodkiem();
                wczytajwatek.przeladujkonfiguracje = true;
                rysujwatek.przeladujkonfiguracje = true;
                rysujwatek.odswiez = true;
            }
        }
        odswiezUI();
    }

    public void pomniejszMape() {
        if(atlas == null) {
            MainActivityWear.activity.pokazToast(KomunikatyWear.BRAKATLASOW);
        } else {
            if(zoom > 10) {
                zoom = zoom - 5;
                rysujwatek.przeladujkonfiguracje = true;
                rysujwatek.odswiez = true;
            } else {
                int index = atlas.parserytmi.indexOf(tmiparser);
                if (index == 0) {
                    MainActivityWear.activity.pokazToast(KomunikatyWear.BLADODDALANIA);
                } else {
                    float gpsx = tmiparser.obliczWspolrzednaXDlaPixela(pixelnamapienadsrodkiem.x);
                    float gpsy = tmiparser.obliczWspolrzednaYDlaPixela(pixelnamapienadsrodkiem.y);
                    tmiparser = atlas.parserytmi.get(index - 1);
                    pixelnamapienadsrodkiem.set(tmiparser.obliczPixelXDlaWspolrzednej(gpsx), tmiparser.obliczPixelYDlaWspolrzednej(gpsy));
                    poprawPixelNadSrodkiem();
                    wczytajwatek.przeladujkonfiguracje = true;
                    rysujwatek.przeladujkonfiguracje = true;
                    rysujwatek.odswiez = true;
                }
            }
        }
        odswiezUI();
    }

    public void wczytajKolejnaMape() {
        if(AtlasyWear.atlasy.size() > 0) {
            int index = AtlasyWear.atlasy.indexOf(atlas);
            int kolejny = 0;
            if (index >= 0) {
                kolejny = index + 1;
            }
            if (kolejny == AtlasyWear.atlasy.size()) {
                kolejny = 0;
            }
            atlas = AtlasyWear.atlasy.get(kolejny);
            MainActivityWear.activity.pokazToast(atlas.nazwa);
            zaczytajOpcje(false, 0, 0);
            odswiezUI();
        } else {
            MainActivityWear.activity.pokazToast(KomunikatyWear.BRAKATLASOW);
            zaczytajOpcje(false, 0, 0);
        }
    }

    private void pokazPierwszyToast() {
        if(atlas == null) {
            MainActivityWear.activity.pokazToast(KomunikatyWear.BRAKATLASOW);
        } else {
            MainActivityWear.activity.pokazToast(atlas.nazwa);
        }
        odswiezUI();
    }

    //Czeka na zakonczenie watku
    private void zakonczWatek(Thread watek) {
        watek.interrupt();
        while(watek.isAlive()) {
            try {
                watek.join();
            } catch (InterruptedException e) {
            }
        }
    }

    private void zakonczWatekRysuj() {
        if(rysujwatek != null) {
            rysujwatek.zakoncz = true;
            zakonczWatek(rysujwatek);
        }
    }

    private void zakonczWatekCzas() {
        if(czaswatek != null) {
            czaswatek.zakoncz = true;
            zakonczWatek(czaswatek);
        }
    }

    private void zakonczWatekKompas() {
        if(kompaswatek != null) {
            kompaswatek.zakoncz = true;
            zakonczWatek(kompaswatek);
        }
    }

    private void zakonczWatekWczytaj() {
        if(wczytajwatek != null) {
            wczytajwatek.zakoncz = true;
            zakonczWatek(wczytajwatek);
        }
    }

    private void zakonczWatki() {
        zakonczWatekRysuj();
        zakonczWatekKompas();
        zakonczWatekWczytaj();
        zakonczWatekCzas();
    }

    private void zabijProces() {
        Timer timer = new Timer();
        timer.schedule( new TimerTask() {
            public void run() {
                int pid = android.os.Process.myPid();
                android.os.Process.killProcess(pid);
            }
        }, 1000);
    }

    public void zakonczUsluge() {
        try {
            wystartowany = false;
            widok = StaleWear.WIDOKBRAK;
            obecnatrasapobrana = false;
            zakonczWatki();
            WearWear.wylaczAPI();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(getApplicationContext(), KomunikatyWear.KONIECPROGRAMU, Toast.LENGTH_SHORT).show();
        zabijProces();
    }

    private void zapiszPlikMerkatoraDoCache(long[] plikmerkatora) {
        ByteBuffer bajtytablicy = ByteBuffer.allocate(8 * plikmerkatora.length);
        LongBuffer longbajtytablicy = bajtytablicy.asLongBuffer();
        longbajtytablicy.put(plikmerkatora);
        try {
            FileOutputStream plikcachetab = MainActivityWear.activity.openFileOutput(StaleWear.SUFFIXCACHEMERKATOR, Context.MODE_PRIVATE);
            plikcachetab.write(bajtytablicy.array(), 0, 8 * plikmerkatora.length);
            plikcachetab.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parsujPlikMerkatora() {
        plikmerkatora = new long[900002];
        InputStream raw = getResources().openRawResource(R.raw.merkator5);
        BufferedReader r = new BufferedReader(new InputStreamReader(raw));
        String line;
        int i = 0;
        try {
            while ((line = r.readLine()) != null) {
                String[] lines = line.split(",");
                plikmerkatora[i] = Long.valueOf(lines[1]);
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        zapiszPlikMerkatoraDoCache(plikmerkatora);
    }

    private void wczytajCacheMerkatora() {
        plikmerkatora = new long[900002];
        try {
            FileInputStream plikcachetab = MainActivityWear.activity.openFileInput(StaleWear.SUFFIXCACHEMERKATOR);
            ByteBuffer bajtytablicy = ByteBuffer.allocate(8 * plikmerkatora.length);
            RozneWear.odczytajZPliku(plikcachetab, 8 * plikmerkatora.length, bajtytablicy.array());
            plikcachetab.close();
            LongBuffer longbajtytablicy = bajtytablicy.asLongBuffer();
            longbajtytablicy.get(plikmerkatora, 0, plikmerkatora.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void wczytajPlikMerkatora() {
        if(sprawdzCacheMerkatora() == false) {
            parsujPlikMerkatora();
        } else {
            wczytajCacheMerkatora();
        }
    }

    //Serwis startuje (po starcie MainActivity)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        //Nie uruchmiamy jest juz jest uruchomiony
        if(wystartowany == false) {
            wystartowany = true;
            wystartujWatekPrzygotowania();
        } else {
            MainActivityWear.activity.zakonczPrzygotowanie();
            rysujwatek.odswiez = true;
        }

        //Nie wzniawiamy serwisu automatycznie
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        zakonczUsluge();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
    }

    @Override
    public void onLowMemory() {
    }

    @Override
    public void onTrimMemory(int level) {
    }
}
