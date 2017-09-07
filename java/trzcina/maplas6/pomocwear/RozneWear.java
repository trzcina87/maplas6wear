package trzcina.maplas6.pomocwear;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import trzcina.maplas6.BuildConfig;

public class RozneWear {

    //Czeka podana ilosc czasu
    public static void czekaj(int milisekundy) {
        try {
            Thread.sleep(milisekundy);
        } catch (InterruptedException e) {
        }
    }

    public static String pobierzPamiec() {
        Runtime runtime = Runtime.getRuntime();
        long uzyte = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
        long heapm = runtime.maxMemory() / 1048576L;
        int procent = (int) ((uzyte / (double)heapm) * 100);
        return new String("Pamięc: " + uzyte + "MB/" + heapm + "MB" + " (" + procent + "%)");
    }

    //Tworzy katalog
    public static boolean utworzKatalog(String nazwa) {
        File katalog = new File(nazwa);
        if(katalog.isDirectory()) {
            return true;
        } else {
            try {
                katalog.mkdir();
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    //Sortuje liste Integer
    public static void sortujListe(List<Integer> lista) {
        Collections.sort(lista, new Comparator<Integer>() {
            @Override
            public int compare(Integer integer, Integer t1) {
                return integer.compareTo(t1);
            }
        });
    }

    public static byte[] intNaBajty(int i) {
        ByteBuffer bajty = ByteBuffer.allocate(4);
        bajty.order(ByteOrder.LITTLE_ENDIAN);
        bajty.putInt(i);
        return bajty.array();
    }

    public static int bajtyNaInt(byte[] b) {
        final ByteBuffer bajty = ByteBuffer.wrap(b);
        bajty.order(ByteOrder.LITTLE_ENDIAN);
        return bajty.getInt();
    }

    public static byte[] odczytajZPliku(InputStream plik, int ilosc) throws IOException {
        byte[] bajty = new byte[ilosc];
        int przeczytano = 0;
        while(przeczytano < ilosc) {
            int przeczytanowprzebiegu = plik.read(bajty, przeczytano, ilosc - przeczytano);
            if(przeczytanowprzebiegu == -1) {
                break;
            }
            przeczytano = przeczytano + przeczytanowprzebiegu;
        }
        return bajty;
    }

    public static byte[] odczytajZPliku(FileInputStream plik, int ilosc) throws IOException {
        byte[] bajty = new byte[ilosc];
        int przeczytano = 0;
        while(przeczytano < ilosc) {
            int przeczytanowprzebiegu = plik.read(bajty, przeczytano, ilosc - przeczytano);
            if(przeczytanowprzebiegu == -1) {
                break;
            }
            przeczytano = przeczytano + przeczytanowprzebiegu;
        }
        return bajty;
    }

    public static void odczytajZPliku(FileInputStream plik, int ilosc, byte[] bajty) throws IOException {
        int przeczytano = 0;
        while(przeczytano < ilosc) {
            int przeczytanowprzebiegu = plik.read(bajty, przeczytano, ilosc - przeczytano);
            if(przeczytanowprzebiegu == -1) {
                break;
            }
            przeczytano = przeczytano + przeczytanowprzebiegu;
        }
    }

    public static int odczytajZeStrumienia(InputStream plik, int ilosc, byte[] bajty) throws IOException {
        int przeczytano = 0;
        while(przeczytano < ilosc) {
            int przeczytanowprzebiegu = plik.read(bajty, przeczytano, ilosc - przeczytano);
            if(przeczytanowprzebiegu == -1) {
                break;
            }
            przeczytano = przeczytano + przeczytanowprzebiegu;
        }
        return przeczytano;
    }

    public static byte[] odczytajPlikRAM(RandomAccessFile plik, long przesuniecie, int dlugosc) throws IOException {
        plik.seek(przesuniecie);
        byte[] bajty = new byte[dlugosc];
        int przeczytano = 0;
        while(przeczytano < dlugosc) {
            int przeczytanowprzebiegu = plik.read(bajty, przeczytano, dlugosc - przeczytano);
            if(przeczytanowprzebiegu == -1) {
                break;
            }
            przeczytano = przeczytano + przeczytanowprzebiegu;
        }
        return bajty;
    }

    public static String pobierzDateBudowania() {
        Date buildDate = new Date(BuildConfig.TIMESTAMP);
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        String data = dateFormat.format(buildDate);
        return data;
    }

    public static String formatujDystans(int dystans) {
        if(dystans < 1000) {
            return String.format(Locale.getDefault(), "%d", dystans) + "m";
        } else if (dystans < 100000) {
            return String.format(Locale.getDefault(), "%.1f", dystans / 1000.0) + "km";
        } else {
            return String.format(Locale.getDefault(), "%.0f", dystans / 1000.0) + "km";
        }
    }

    public static float zaokraglij5(float liczba) {
        double val = liczba * 100000;
        long vall = Math.round(val);
        return vall / 100000F;
    }

    public static long pomnoz4(double liczba) {
        double val = liczba*10000;
        long vall = Math.round(val);
        return vall;
    }

    public static long pomnoz5(double liczba) {
        double val = liczba*100000;
        long vall = Math.round(val);
        return vall;
    }
}

