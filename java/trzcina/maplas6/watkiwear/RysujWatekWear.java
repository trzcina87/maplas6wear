package trzcina.maplas6.watkiwear;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.Location;
import android.util.Log;

import java.util.List;

import trzcina.maplas6.AppServiceWear;
import trzcina.maplas6.MainActivityWear;
import trzcina.maplas6.atlasywear.AtlasWear;
import trzcina.maplas6.atlasywear.TmiParserWear;
import trzcina.maplas6.lokalizacjawear.GPXPunktWear;
import trzcina.maplas6.lokalizacjawear.GPXTrasaWear;
import trzcina.maplas6.lokalizacjawear.PlikiGPXWear;
import trzcina.maplas6.lokalizacjawear.PunktNaMapieWear;
import trzcina.maplas6.lokalizacjawear.PunktWTrasieWear;
import trzcina.maplas6.pomocwear.BitmapyWear;
import trzcina.maplas6.pomocwear.PaintyWear;
import trzcina.maplas6.pomocwear.RozneWear;
import trzcina.maplas6.pomocwear.StaleWear;

import static trzcina.maplas6.MainActivityWear.activity;

public class RysujWatekWear extends Thread {

    public volatile boolean zakoncz;        //Info czy zakonczyc watek
    public volatile boolean odswiez;        //Info czy odswiezyc obraz
    public volatile boolean przeladujkonfiguracje;
    public AtlasWear atlas;
    public TmiParserWear tmiparser;
    private float density;
    public float zoom;
    private Bitmap bitmapa;

    String[] opisykol = {"1m", "2m", "5m", "10m", "20m", "50m", "100m", "200m", "500m", "1km", "2km", "5km", "10km", "20km", "50km", "100km", "200km"};
    int[] metrykol = {1, 2, 5, 10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000, 100000, 200000};
    boolean[] rysujkola;
    double[] promieniekol;

    public RysujWatekWear() {
        zakoncz = false;
        odswiez = true;
        przeladujkonfiguracje = false;
        atlas = null;
        density = activity.getResources().getDisplayMetrics().density;
        rysujkola = new boolean[opisykol.length];
        promieniekol = new double[metrykol.length];
        zoom = 1;
        bitmapa = Bitmap.createBitmap(320, 320, Bitmap.Config.ARGB_8888);
    }

    private void przeliczKola() {
        for(int kolo = 0; kolo < opisykol.length; kolo++) {
            promieniekol[kolo] = ((double)metrykol[kolo] / (double)tmiparser.rozpietoscxwmetrach) * (double)tmiparser.rozmiarmapy.x;
            if((promieniekol[kolo] * zoom >= 30 * PaintyWear.density) && (promieniekol[kolo] * zoom <= Math.max(AppServiceWear.service.srodekekranu.x, AppServiceWear.service.srodekekranu.y) * 1.1)) {
                rysujkola[kolo] = true;
            } else {
                rysujkola[kolo] = false;
            }
        }
    }

    public void przeladujKonfiguracje() {
        przeladujkonfiguracje = false;
        atlas = AppServiceWear.service.atlas;
        odswiez = true;
        tmiparser = null;
        if(atlas != null) {
            tmiparser = AppServiceWear.service.tmiparser;
            przeliczKola();
        }
    }

    //Uwalniamy sufrace w razie bledu
    private void zwolnijCanvas(Canvas canvas) {
        MainActivityWear.activity.umiescBitmape(bitmapa);
    }

    //Pobieramy surface
    private Canvas pobierzCanvas() {
        return new Canvas(bitmapa);
    }

    //Rysujemy czarne tlo
    private void rysujTlo(Canvas canvas) {
        canvas.drawColor(Color.GRAY);
    }

    //Rysujemy srodkowe kolo
    private void rysujKola(Canvas canvas) {
        canvas.drawCircle(AppServiceWear.service.srodekekranu.x, AppServiceWear.service.srodekekranu.y, StaleWear.SZEROKOSCSRODKOWEGOKOLA * density, PaintyWear.paintczerwonysrodek);
        for (int kolo = 0; kolo < opisykol.length; kolo++) {
            if (rysujkola[kolo]) {
                canvas.drawCircle(AppServiceWear.service.srodekekranu.x, AppServiceWear.service.srodekekranu.y, (float) (promieniekol[kolo] * zoom), PaintyWear.paintczerwoneokregi);
                if(AppServiceWear.service.kolorinfo == 3) {
                    Rect zarys = new Rect();
                    PaintyWear.painttekst[AppServiceWear.service.kolorinfo].getTextBounds(opisykol[kolo], 0, opisykol[kolo].length(), zarys);
                    rysujProstokat(canvas, (float)AppServiceWear.service.srodekekranu.x - 20 - 3, (float) (AppServiceWear.service.srodekekranu.y + zoom * promieniekol[kolo] - 9) - zarys.height() - 4, zarys.width() + 11, zarys.height() + 10, PaintyWear.paintczarnyprostokat);
                }
                canvas.drawText(opisykol[kolo], AppServiceWear.service.srodekekranu.x - 20, (float) (AppServiceWear.service.srodekekranu.y + zoom * promieniekol[kolo] - 9), PaintyWear.painttekst[AppServiceWear.service.kolorinfo]);
            }
        }
    }

    private void rysujMape(Canvas canvas) {
        try {
            Point srodekekranu = AppServiceWear.service.srodekekranu;
            Point centralnykafel = new Point(AppServiceWear.service.wczytajwatek.wyznaczIndexBitmapyX(AppServiceWear.service.pixelnamapienadsrodkiem.x), AppServiceWear.service.wczytajwatek.wyznaczIndexBitmapyY(AppServiceWear.service.pixelnamapienadsrodkiem.y));
            int odlegloscodlewejkrawedzikafla = (int) (AppServiceWear.service.pixelnamapienadsrodkiem.x - centralnykafel.x * tmiparser.rozmiarkafla.x);
            int odlegloscodgornejkrawedzikafla = (int) (AppServiceWear.service.pixelnamapienadsrodkiem.y - centralnykafel.y * tmiparser.rozmiarkafla.y);
            int startcentralnegokaflax = (int) (srodekekranu.x - odlegloscodlewejkrawedzikafla * zoom);
            int startcentralnegokaflay = (int) (srodekekranu.y - odlegloscodgornejkrawedzikafla * zoom);
            int ilosckafliwlewo = startcentralnegokaflax / tmiparser.rozmiarkafla.x + 1;
            int ilosckafliwgore = startcentralnegokaflay / tmiparser.rozmiarkafla.y + 1;
            if(ilosckafliwlewo <= 0) {
                ilosckafliwlewo = 1;
            }
            if(ilosckafliwgore <= 0) {
                ilosckafliwgore = 1;
            }
            int dodatkowykafelx = 0;
            int dodatkowykafely = 0;
            ilosckafliwgore = 1;
            ilosckafliwlewo = 1;
            for(int i = -ilosckafliwlewo; i <= ilosckafliwlewo + dodatkowykafelx; i++) {
                for(int j = -ilosckafliwgore; j <= ilosckafliwgore + dodatkowykafely; j++) {
                    if((centralnykafel.x + i >= 0) && (centralnykafel.y + j >= 0) && (centralnykafel.x + i < tmiparser.ilosckafli.x) && (centralnykafel.y + j < tmiparser.ilosckafli.y)) {
                        if (AppServiceWear.service.wczytajwatek.czyBitmapaWczytana(centralnykafel.x + i, centralnykafel.y + j)) {
                            Bitmap kafel = AppServiceWear.service.wczytajwatek.bitmapy[centralnykafel.x + i][centralnykafel.y + j];
                            int rozmiarxpozoom = (int) (zoom * kafel.getWidth());
                            int rozmiarypozoom = (int) (zoom * kafel.getHeight());
                            int lewo = (int) (startcentralnegokaflax + i * tmiparser.rozmiarkafla.x * zoom);
                            int gora = (int) (startcentralnegokaflay + j * tmiparser.rozmiarkafla.y * zoom);
                            canvas.drawBitmap(kafel, new Rect(0, 0, kafel.getWidth(), kafel.getHeight()), new Rect(lewo, gora, lewo + rozmiarxpozoom, gora + rozmiarypozoom), null);
                        } else {
                            odswiez = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            zwolnijCanvas(canvas);
            odswiez = true;
        }
    }

    private void rysujTrasyGPX(Canvas canvas, Point pixelnadsrodkiem) {
        if(AppServiceWear.service.poziominfo >= StaleWear.OPISYPUNKTY) {
            float promienpunktu = 4 * PaintyWear.density;
            for (int i = 0; i < PlikiGPXWear.pliki.size(); i++) {
                if (PlikiGPXWear.pliki.get(i).zaznaczony) {
                    List<PunktWTrasieWear> lista = PlikiGPXWear.pliki.get(i).trasa;
                    if(lista.size() > 0) {
                        int popy = 0;
                        int popx = 0;
                        PunktWTrasieWear punkt = lista.get(0);
                        int x = tmiparser.obliczPixelXDlaWspolrzednej(punkt.wspx);
                        int y = tmiparser.obliczPixelYDlaWspolrzednej(punkt.wspy);
                        canvas.drawCircle(AppServiceWear.service.srodekekranu.x + (x - pixelnadsrodkiem.x) * zoom, AppServiceWear.service.srodekekranu.y + (y - pixelnadsrodkiem.y) * zoom, promienpunktu, PaintyWear.paintzielonyokragtrasa);
                        popx = x;
                        popy = y;
                        for (int j = 1; j < lista.size() - 1; j++) {
                            punkt = lista.get(j);
                            x = tmiparser.obliczPixelXDlaWspolrzednej(punkt.wspx);
                            y = tmiparser.obliczPixelYDlaWspolrzednej(punkt.wspy);
                            if((Math.abs(x - popx) >= 3) || (Math.abs(y - popy) >= 3)) {
                                canvas.drawLine(AppServiceWear.service.srodekekranu.x + (x - pixelnadsrodkiem.x) * zoom, AppServiceWear.service.srodekekranu.y + (y - pixelnadsrodkiem.y) * zoom, AppServiceWear.service.srodekekranu.x + (popx - pixelnadsrodkiem.x) * zoom, AppServiceWear.service.srodekekranu.y + (popy - pixelnadsrodkiem.y) * zoom, PaintyWear.paintzielonyokragtrasa);
                                popy = y;
                                popx = x;
                            }
                        }
                        punkt = lista.get(lista.size() - 1);
                        x = tmiparser.obliczPixelXDlaWspolrzednej(punkt.wspx);
                        y = tmiparser.obliczPixelYDlaWspolrzednej(punkt.wspy);
                        canvas.drawLine(AppServiceWear.service.srodekekranu.x + (x - pixelnadsrodkiem.x) * zoom, AppServiceWear.service.srodekekranu.y + (y - pixelnadsrodkiem.y) * zoom, AppServiceWear.service.srodekekranu.x + (popx - pixelnadsrodkiem.x) * zoom, AppServiceWear.service.srodekekranu.y + (popy - pixelnadsrodkiem.y) * zoom, PaintyWear.paintzielonyokragtrasa);
                        canvas.drawCircle(AppServiceWear.service.srodekekranu.x + (x - pixelnadsrodkiem.x) * zoom, AppServiceWear.service.srodekekranu.y + (y - pixelnadsrodkiem.y) * zoom, promienpunktu, PaintyWear.paintzielonyokragtrasa);
                    }
                }
            }
        }
    }

    private void rysujTraseObecna(Canvas canvas, Point pixelnadsrodkiem) {
        float promienpunktu = 4 * PaintyWear.density;
        for(int i = 0; i < AppServiceWear.service.obecnetrasy.size(); i++) {
            GPXTrasaWear obecnatrasa = AppServiceWear.service.obecnetrasy.get(i);
            if (obecnatrasa != null) {
                int iloscpunktow = obecnatrasa.iloscpunktow;
                if (iloscpunktow > 0) {
                    int popy = 0;
                    int popx = 0;
                    PunktWTrasieWear punkt = obecnatrasa.lista[0];
                    int x = tmiparser.obliczPixelXDlaWspolrzednej(punkt.wspx);
                    int y = tmiparser.obliczPixelYDlaWspolrzednej(punkt.wspy);
                    canvas.drawCircle(AppServiceWear.service.srodekekranu.x + (x - pixelnadsrodkiem.x) * zoom, AppServiceWear.service.srodekekranu.y + (y - pixelnadsrodkiem.y) * zoom, promienpunktu, PaintyWear.paintfioletowyokragtrasa);
                    popx = x;
                    popy = y;
                    for (int j = 1; j < iloscpunktow - 1; j++) {
                        punkt = obecnatrasa.lista[j];
                        x = tmiparser.obliczPixelXDlaWspolrzednej(punkt.wspx);
                        y = tmiparser.obliczPixelYDlaWspolrzednej(punkt.wspy);
                        if ((Math.abs(x - popx) >= 3) || (Math.abs(y - popy) >= 3)) {
                            canvas.drawLine(AppServiceWear.service.srodekekranu.x + (x - pixelnadsrodkiem.x) * zoom, AppServiceWear.service.srodekekranu.y + (y - pixelnadsrodkiem.y) * zoom, AppServiceWear.service.srodekekranu.x + (popx - pixelnadsrodkiem.x) * zoom, AppServiceWear.service.srodekekranu.y + (popy - pixelnadsrodkiem.y) * zoom, PaintyWear.paintfioletowyokragtrasa);
                            popy = y;
                            popx = x;
                        }
                    }
                    punkt = obecnatrasa.lista[iloscpunktow - 1];
                    x = tmiparser.obliczPixelXDlaWspolrzednej(punkt.wspx);
                    y = tmiparser.obliczPixelYDlaWspolrzednej(punkt.wspy);
                    canvas.drawLine(AppServiceWear.service.srodekekranu.x + (x - pixelnadsrodkiem.x) * zoom, AppServiceWear.service.srodekekranu.y + (y - pixelnadsrodkiem.y) * zoom, AppServiceWear.service.srodekekranu.x + (popx - pixelnadsrodkiem.x) * zoom, AppServiceWear.service.srodekekranu.y + (popy - pixelnadsrodkiem.y) * zoom, PaintyWear.paintfioletowyokragtrasa);
                    canvas.drawCircle(AppServiceWear.service.srodekekranu.x + (x - pixelnadsrodkiem.x) * zoom, AppServiceWear.service.srodekekranu.y + (y - pixelnadsrodkiem.y) * zoom, promienpunktu, PaintyWear.paintfioletowyokragtrasa);
                }
            }
        }
    }

    private void rysujProstokat(Canvas canvas, Float left, Float top, int width, int height, Paint paint) {
        canvas.drawRect(left, top, left + width, top + height, paint);
    }

    private void rysujPunktNaMapie(PunktNaMapieWear punkt, Canvas canvas, Paint paint, Point pixelnadsrodkiem, float promienpunktu, Location lokalizacja) {
        int x = tmiparser.obliczPixelXDlaWspolrzednej(punkt.wspx);
        int y = tmiparser.obliczPixelYDlaWspolrzednej(punkt.wspy);
        float wspolrzednepunktunaekraniex = AppServiceWear.service.srodekekranu.x + (x - pixelnadsrodkiem.x) * zoom;
        float wspolrzednepunktunaekraniey = AppServiceWear.service.srodekekranu.y + (y - pixelnadsrodkiem.y) * zoom;
        int margines = 30;
        if((wspolrzednepunktunaekraniex >= -margines) && (wspolrzednepunktunaekraniex <= AppServiceWear.service.srodekekranu.x * 2 + margines) && (wspolrzednepunktunaekraniey >= -margines) && (wspolrzednepunktunaekraniey <= AppServiceWear.service.srodekekranu.y * 2 + margines)) {
            canvas.drawCircle(wspolrzednepunktunaekraniex, wspolrzednepunktunaekraniey, promienpunktu, paint);
            if (AppServiceWear.service.poziominfo >= StaleWear.OPISYNAZWY) {
                if (punkt.nazwa != null) {
                    Rect zarys = punkt.rectnazwa;
                    if (AppServiceWear.service.kolorinfo == 3) {
                        rysujProstokat(canvas, (float) (AppServiceWear.service.srodekekranu.x + (x - pixelnadsrodkiem.x) * zoom - zarys.width() / 2 - 3 + zarys.left), AppServiceWear.service.srodekekranu.y + (y - pixelnadsrodkiem.y) * zoom - promienpunktu - 8 - zarys.height() - 3, zarys.width() + 6, zarys.height() + 6, PaintyWear.paintczarnyprostokat);
                    }
                    canvas.drawText(punkt.nazwa, AppServiceWear.service.srodekekranu.x + (x - pixelnadsrodkiem.x) * zoom - zarys.width() / 2, AppServiceWear.service.srodekekranu.y + (y - pixelnadsrodkiem.y) * zoom - promienpunktu - 8, PaintyWear.painttekst[AppServiceWear.service.kolorinfo]);
                }
                if ((AppServiceWear.service.poziominfo == StaleWear.OPISYKOMENTARZE) || (AppServiceWear.service.poziominfo == StaleWear.OPISYODLEGLOSCIKOMENTARZE)) {
                    if (punkt.opis != null) {
                        Rect zarys = punkt.rectopis;
                        if (AppServiceWear.service.kolorinfo == 3) {
                            rysujProstokat(canvas, (AppServiceWear.service.srodekekranu.x + (x - pixelnadsrodkiem.x) * zoom - zarys.width() / 2 - 3 + zarys.left), AppServiceWear.service.srodekekranu.y + (y - pixelnadsrodkiem.y) * zoom + promienpunktu + 6 - 3, zarys.width() + 6, zarys.height() + 6, PaintyWear.paintczarnyprostokat);
                        }
                        canvas.drawText(punkt.opis, AppServiceWear.service.srodekekranu.x + (x - pixelnadsrodkiem.x) * zoom - zarys.width() / 2, AppServiceWear.service.srodekekranu.y + (y - pixelnadsrodkiem.y) * zoom + promienpunktu + 6 - zarys.top, PaintyWear.painttekst[AppServiceWear.service.kolorinfo]);
                    }
                }
                if ((AppServiceWear.service.poziominfo == StaleWear.OPISYODLEGLOSCI) || (AppServiceWear.service.poziominfo == StaleWear.OPISYODLEGLOSCIKOMENTARZE)) {
                    if (lokalizacja != null) {
                        String dystans = RozneWear.formatujDystans(Math.round(punkt.zmierzDystans(lokalizacja.getLongitude(), lokalizacja.getLatitude())));
                        if (dystans != null) {
                            Rect zarys = new Rect();
                            PaintyWear.painttekst[AppServiceWear.service.kolorinfo].getTextBounds(dystans, 0, dystans.length(), zarys);
                            if (AppServiceWear.service.kolorinfo == 3) {
                                rysujProstokat(canvas, AppServiceWear.service.srodekekranu.x + (x - pixelnadsrodkiem.x) * zoom + promienpunktu + 4, AppServiceWear.service.srodekekranu.y + (y - pixelnadsrodkiem.y) * zoom - zarys.height() / 2 - 3, zarys.width() + 6, zarys.height() + 6, PaintyWear.paintczarnyprostokat);
                            }
                            canvas.drawText(dystans, AppServiceWear.service.srodekekranu.x + (x - pixelnadsrodkiem.x) * zoom + promienpunktu + 7, AppServiceWear.service.srodekekranu.y + (y - pixelnadsrodkiem.y) * zoom + zarys.height() / 2, PaintyWear.painttekst[AppServiceWear.service.kolorinfo]);
                        }
                    }
                    /*if (tmiparser != null) {
                        float gpsx = tmiparser.obliczWspolrzednaXDlaPixela(pixelnadsrodkiem.x);
                        float gpsy = tmiparser.obliczWspolrzednaYDlaPixela(pixelnadsrodkiem.y);
                        String dystans = RozneWear.formatujDystans(Math.round(punkt.zmierzDystans(gpsx, gpsy)));
                        if (dystans != null) {
                            Rect zarys = new Rect();
                            PaintyWear.painttekst[AppServiceWear.service.kolorinfo].getTextBounds(dystans, 0, dystans.length(), zarys);
                            if (AppServiceWear.service.kolorinfo == 3) {
                                rysujProstokat(canvas, AppServiceWear.service.srodekekranu.x + (x - pixelnadsrodkiem.x) * zoom - promienpunktu - 10 - zarys.width(), AppServiceWear.service.srodekekranu.y + (y - pixelnadsrodkiem.y) * zoom - zarys.height() / 2 - 3, zarys.width() + 6, zarys.height() + 6, PaintyWear.paintczarnyprostokat);
                            }
                            canvas.drawText(dystans, AppServiceWear.service.srodekekranu.x + (x - pixelnadsrodkiem.x) * zoom - promienpunktu - 7 - zarys.width(), AppServiceWear.service.srodekekranu.y + (y - pixelnadsrodkiem.y) * zoom + zarys.height() / 2, PaintyWear.painttekst[AppServiceWear.service.kolorinfo]);
                        }
                    }*/
                }
            }
        }
    }

    private void rysujPunktyObecne(Canvas canvas, Point pixelnadsrodkiem, Location lokalizacja) {
        float promienpunktu = 4 * PaintyWear.density;
        for(int i = 0; i < GPXPunktWear.lista.size(); i++) {
            rysujPunktNaMapie(GPXPunktWear.lista.get(i), canvas, PaintyWear.paintfioletowyokrag, pixelnadsrodkiem, promienpunktu, lokalizacja);
        }
    }

    private void rysujPunktyGPX(Canvas canvas, Point pixelnadsrodkiem, Location lokalizacja) {
        float promienpunktu = 4 * PaintyWear.density;
        for(int i = 0; i < PlikiGPXWear.pliki.size(); i++) {
            if(PlikiGPXWear.pliki.get(i).zaznaczony) {
                if(AppServiceWear.service.poziominfo >= StaleWear.OPISYPUNKTY) {
                    List<PunktNaMapieWear> lista = PlikiGPXWear.pliki.get(i).punkty;
                    for (int j = 0; j < lista.size(); j++) {
                        rysujPunktNaMapie(lista.get(j), canvas, PaintyWear.paintzielonyokrag, pixelnadsrodkiem, promienpunktu, lokalizacja);
                    }
                }
            }
        }
    }

    //Rysujemy kompas na srodku
    private void rysujKompas(Canvas canvas) {
        int katpolozenia = AppServiceWear.service.kompaswatek.kat;
        Matrix macierzobrotu = new Matrix();
        macierzobrotu.setRotate(katpolozenia, BitmapyWear.strzalka.getWidth() / 2, BitmapyWear.strzalka.getHeight() / 2);
        macierzobrotu.postTranslate(AppServiceWear.service.srodekekranu.x - BitmapyWear.strzalka.getWidth() / 2, AppServiceWear.service.srodekekranu.y - BitmapyWear.strzalka.getHeight() / 2);
        canvas.drawBitmap(BitmapyWear.strzalka, macierzobrotu, null);
        Rect zarys = new Rect();
        String dokladnosckompasu = String.valueOf(AppServiceWear.service.kompaswatek.dokladnosc);
        PaintyWear.painttekst[AppServiceWear.service.kolorinfo].getTextBounds(dokladnosckompasu, 0, dokladnosckompasu.length(), zarys);
        int poziom = AppServiceWear.service.srodekekranu.x;
        int pion = AppServiceWear.service.srodekekranu.y - zarys.height() - 10;
        if(katpolozenia <= 180) {
            poziom = poziom - 15 - zarys.width() - 5;
        } else {
            poziom = poziom + 15;
        }
        if(AppServiceWear.service.kolorinfo == 3) {
            rysujProstokat(canvas, (float)poziom - 5, (float)pion - zarys.height() - 5, zarys.width() + 11, zarys.height() + 11, PaintyWear.paintczarnyprostokat);
        }
        canvas.drawText(dokladnosckompasu, poziom, pion, PaintyWear.painttekst[AppServiceWear.service.kolorinfo]);
    }

    private void rysujBlad(Canvas canvas) {
        canvas.drawColor(Color.BLACK);
        canvas.drawBitmap(BitmapyWear.brakmapy, AppServiceWear.service.srodekekranu.x - BitmapyWear.brakmapy.getWidth() / 2, AppServiceWear.service.srodekekranu.y - BitmapyWear.brakmapy.getHeight() / 2, null);
    }

    private void rysujKursorGPS(Canvas canvas, Point pixelnadsrodkiem, Location lokalizacja) {
        if(lokalizacja != null) {
            int x = tmiparser.obliczPixelXDlaWspolrzednej((float) lokalizacja.getLongitude());
            int y = tmiparser.obliczPixelYDlaWspolrzednej((float) lokalizacja.getLatitude());
            canvas.drawBitmap(BitmapyWear.kursorgps, AppServiceWear.service.srodekekranu.x + (x - pixelnadsrodkiem.x) * zoom - BitmapyWear.kursorgps.getWidth() / 2, AppServiceWear.service.srodekekranu.y + (y - pixelnadsrodkiem.y) * zoom - BitmapyWear.kursorgps.getHeight() / 2, null);
        }
    }

    //Rysujemy zawartosc ekranu
    private void odswiezEkran() {
        Canvas canvas = null;
        try {
            canvas = pobierzCanvas();
            if(canvas != null) {
                if(atlas != null) {
                    Point pixelnadsrodkiem = new Point(AppServiceWear.service.pixelnamapienadsrodkiem);
                    Location location = AppServiceWear.service.czyJestFix();
                    rysujTlo(canvas);
                    rysujMape(canvas);
                    rysujTrasyGPX(canvas, pixelnadsrodkiem);
                    rysujPunktyGPX(canvas, pixelnadsrodkiem, location);
                    rysujKursorGPS(canvas, pixelnadsrodkiem, location);
                    rysujKola(canvas);
                    rysujTraseObecna(canvas, pixelnadsrodkiem);
                    rysujPunktyObecne(canvas, pixelnadsrodkiem, location);
                    rysujKompas(canvas);
                    zwolnijCanvas(canvas);
                } else {
                    rysujBlad(canvas);
                    zwolnijCanvas(canvas);
                }
            } else {
                odswiez = true;
            }
        } catch (Exception e) {

            //Jesli jakis blad to odswiez natychmiast
            odswiez = true;
            zwolnijCanvas(canvas);
            e.printStackTrace();
        }
    }

    //Glowna petla watku
    public void run() {
        while(zakoncz == false) {

            if(MainActivityWear.activity.activitywidoczne == true) {
                zoom = AppServiceWear.service.zoom / (float) 10;

                //Gdy mamy przeladowac atlas
                if (przeladujkonfiguracje == true) {
                    przeladujKonfiguracje();
                }

                //Gdy mamy cos odswiezyc
                if (odswiez == true) {
                    odswiez = false;
                    odswiezEkran();
                }

                //jesli nie mamy nic rysowac, krotka przerwa
                if (odswiez == false) {
                    RozneWear.czekaj(5);
                }
            } else {
                RozneWear.czekaj(20);
            }
        }
    }

}
