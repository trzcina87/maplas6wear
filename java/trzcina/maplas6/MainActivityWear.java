package trzcina.maplas6;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.icu.text.TimeZoneNames;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import trzcina.maplas6.lokalizacjawear.GPXTrasaWear;
import trzcina.maplas6.pomocwear.KomunikatyWear;
import trzcina.maplas6.pomocwear.PagerAdapterWear;
import trzcina.maplas6.pomocwear.RozneWear;
import trzcina.maplas6.pomocwear.UprawnieniaWear;
import trzcina.maplas6.pomocwear.WearWear;

@SuppressWarnings("PointlessBooleanExpression")
public class MainActivityWear extends WearableActivity {

    //Activity
    public static volatile MainActivityWear activity;
    public volatile boolean activitywidoczne;

    //Layouty
    private LayoutInflater inflater;
    public RelativeLayout mapapage;
    public LinearLayout dodaj1page;
    public RelativeLayout podsumowaniepage;
    public LinearLayout ustawieniapage;

    //Widoki
    private ImageView mapimageview;
    private Button zakonczbutton;
    private ViewPager pager;
    private ProgressBar przygotowanieprogressbar;
    private TextView przygotowanieinfo;
    private TextView textgora;
    private ImageView powieksz;
    private ImageView pomniejsz;
    private ImageView infomale;
    private TextView mapaczas;
    private TextView podczas;
    private TextView poddlugosc;
    private TextView podstart;
    private TextView podczastrasy;
    private TextView podgps;
    private LinearLayout czaslinearlayout;
    private TextView pamiectextview;
    private TextView trasainfo;
    private TextView bateriatextview;

    //Zmienne pomocniczne do wypelniania widokow
    private DateFormat formatgodziny;
    private DateFormat formatgodzinysekundy;
    private DateFormat formatgodzinydoinfo;
    private int wrocdowidoku;

    //Dla danego id zasobu (w res/layout) zwraca widok
    private LinearLayout znajdzLinearLayout(int zasob) {
        LinearLayout layouttmp = (LinearLayout) inflater.inflate(zasob, null);
        layouttmp.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        return layouttmp;
    }

    //Dla danego id zasobu (w res/layout) zwraca widok
    private RelativeLayout znajdzRelativeLayout(int zasob) {
        RelativeLayout layouttmp = (RelativeLayout) inflater.inflate(zasob, null);
        layouttmp.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        return layouttmp;
    }

    //Znajduje Layout z plikow XML
    private void znajdzLayouty() {
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mapapage = znajdzRelativeLayout(R.layout.mapapage);
        dodaj1page = znajdzLinearLayout(R.layout.dodaj1page);
        podsumowaniepage = znajdzRelativeLayout(R.layout.podsumowaniepage);
        ustawieniapage = znajdzLinearLayout(R.layout.ustawieniapage);
    }

    //Ustawia date budowania podczas startu
    private void ustawDateBudowania() {
        ustawTextWTextView(textgora, RozneWear.pobierzDateBudowania());
    }

    //Ustawia parametry ekranu
    private void ustawEkran() {
        setContentView(R.layout.contentviewlayout);
        setAmbientEnabled();
    }

    //Ustawia domyslne wartosci zmiennych w programie
    private void ustawZmienne() {
        formatgodziny = new SimpleDateFormat("HH:mm");
        formatgodzinysekundy = new SimpleDateFormat("HH:mm:ss");
        formatgodzinysekundy.setTimeZone(TimeZone.getTimeZone("UTC"));
        formatgodzinydoinfo = new SimpleDateFormat("H:mm");
        formatgodzinydoinfo.setTimeZone(TimeZone.getTimeZone("UTC"));
        activitywidoczne = true;
        wrocdowidoku = 0;
    }

    //Znajduje wszystkie potrzebne widoki
    private void znajdzWidoki() {
        pager = (ViewPager)findViewById(R.id.pager);
        przygotowanieprogressbar = (ProgressBar)mapapage.findViewById(R.id.progress);
        przygotowanieinfo = (TextView)mapapage.findViewById(R.id.text);
        pomniejsz = (ImageView)mapapage.findViewById(R.id.minusimage);
        powieksz = (ImageView)mapapage.findViewById(R.id.plusimage);
        mapimageview = (ImageView)mapapage.findViewById(R.id.mapimageview);
        textgora = (TextView)mapapage.findViewById(R.id.textgora);
        zakonczbutton = (Button)ustawieniapage.findViewById(R.id.zakonczbutton);
        mapaczas = (TextView)mapapage.findViewById(R.id.mapaczas);
        podczas = (TextView)podsumowaniepage.findViewById(R.id.podczas);
        poddlugosc = (TextView)podsumowaniepage.findViewById(R.id.poddlugosc);
        podstart = (TextView)podsumowaniepage.findViewById(R.id.podstart);
        podczastrasy = (TextView)podsumowaniepage.findViewById(R.id.podczastrasy);
        infomale = (ImageView)mapapage.findViewById(R.id.infomale);
        czaslinearlayout = (LinearLayout)mapapage.findViewById(R.id.czaslinearlayout);
        pamiectextview = (TextView)ustawieniapage.findViewById(R.id.pamiectextview);
        podgps = (TextView)podsumowaniepage.findViewById(R.id.podgps);
        trasainfo = (TextView)mapapage.findViewById(R.id.trasainfo);
        bateriatextview = (TextView)ustawieniapage.findViewById(R.id.bateriatextview);
    }

    //Ustawia zadany tekst w TextView
    private void ustawTextWTextView(final TextView tv, final String tekst) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv.setText(tekst);
            }
        });
    }

    //Ustawia obecny czas na widoku mapy
    public void ustawCzasNaMapie() {
        Date obecnyczas = new Date(System.currentTimeMillis());
        ustawTextWTextView(mapaczas, formatgodziny.format(obecnyczas));
    }

    //Ustawia obecny czas na widoku podsumowania
    public void ustawCzasNaPodsumowaniu() {
        Date obecnyczas = new Date(System.currentTimeMillis());
        ustawTextWTextView(podczas, formatgodziny.format(obecnyczas));
    }

    //Przypisuje akcje do widoków
    private void przypiszAkcjeDoWidokow() {
        pomniejsz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppServiceWear.service.pomniejszMape();
            }
        });
        powieksz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppServiceWear.service.powiekszMape();
            }
        });
        zakonczbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WearWear.wyslijSTOPGPS();
                stopService(new Intent(MainActivityWear.this, AppServiceWear.class));
                finish();
            }
        });
        mapimageview.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AppServiceWear.service.wczytajKolejnaMape();
                return true;
            }
        });
        View.OnClickListener zapisz = new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AppServiceWear.service.wyslijPunktDoTelefonu((String) view.getTag());
                    }
                }).start();
            }
        };
        int[] ikony = {R.id.im1, R.id.im2, R.id.im3, R.id.im4, R.id.im5, R.id.im6, R.id.im7, R.id.im8, R.id.im9};
        for(int i = 0; i < ikony.length; i++) {
            dodaj1page.findViewById(ikony[i]).setOnClickListener(zapisz);
        }
        infomale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppServiceWear.service.zmienPoziomInfo();
            }
        });
        czaslinearlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppServiceWear.service.zmienKolorInfo();
                zmienStylTextView();
            }
        });
        mapaczas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppServiceWear.service.zmienKolorInfo();
                zmienStylTextView();
            }
        });
    }

    private void zmienStylTextView() {
        zmienStylJednegoTextView(mapaczas);
        zmienStylJednegoTextView(trasainfo);
    }

    private String zwrotRozniceWCzasieGPS() {
        if (WearWear.location.getTime() > 0) {
            int roznicaint = Math.round((System.currentTimeMillis() - WearWear.location.getTime()) / 1000);
            return " (" + roznicaint + "s)";
        } else {
            return "";
        }
    }

    public void wypelnijPodsumowanie(boolean wymus) {
        if((activitywidoczne == true) || (wymus == true)){
            GPXTrasaWear obecna = AppServiceWear.service.obecnatrasa;
            if (obecna != null) {
                ustawTextWTextView(poddlugosc, "Długość: " + RozneWear.formatujDystans(Math.round(obecna.dlugosctrasy)));
                ustawTextWTextView(podstart, "Od startu: " + RozneWear.formatujDystans(Math.round(obecna.odlegloscodpoczatku)));
                ustawTextWTextView(podczastrasy, "Czas: " + formatgodzinysekundy.format(System.currentTimeMillis() - obecna.czasstart));
                ustawTextWTextView(podgps, RozneWear.zaokraglij4((float) WearWear.location.getLongitude()) + " " + RozneWear.zaokraglij4((float) WearWear.location.getLatitude()) + zwrotRozniceWCzasieGPS());
            }
        }
    }

    public void wypelnijPodsumowanieNaWidokuMapy() {
        if(activitywidoczne == true) {
            GPXTrasaWear obecna = AppServiceWear.service.obecnatrasa;
            if (obecna != null) {
                String info = RozneWear.formatujDystans(Math.round(obecna.dlugosctrasy)) + " " + RozneWear.formatujDystans(Math.round(obecna.odlegloscodpoczatku)) + " " + formatgodzinydoinfo.format(System.currentTimeMillis() - obecna.czasstart);
                ustawTextWTextView(trasainfo, info);
            }
        }
    }

    private void ustawKolorITloTextView(final TextView tv, final int kolor, final int tlo) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv.setTextColor(kolor);
                tv.setBackgroundColor(tlo);
            }
        });
    }

    private void zmienStylJednegoTextView(TextView tv) {
        if(AppServiceWear.service != null) {
            int styl = AppServiceWear.service.kolorinfo;
            if (styl == 0) {
                ustawKolorITloTextView(tv, Color.WHITE, Color.TRANSPARENT);
            }
            if (styl == 1) {
                ustawKolorITloTextView(tv, Color.RED, Color.TRANSPARENT);
            }
            if (styl == 2) {
                ustawKolorITloTextView(tv, Color.BLACK, Color.TRANSPARENT);
            }
            if (styl == 3) {
                ustawKolorITloTextView(tv, Color.WHITE, Color.BLACK);
            }
        }
    }

    private void ustawImageView(final ImageView im, final int zasob) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                im.setImageResource(zasob);
            }
        });
    }

    public void pokazIkoneOpisow() {
        ustawImageView(infomale, R.mipmap.infomale);
    }

    public void pokazIkoneOpisowWylaczonych() {
        ustawImageView(infomale, R.mipmap.infomalekrzyz);
    }

    private void ustawPager() {
        PagerAdapterWear adapter = new PagerAdapterWear();
        pager.setAdapter(adapter);
        pager.setCurrentItem(0);
        pager.setOnPageChangeListener(adapter);
    }

    public void umiescBitmape(final Bitmap bitmapa) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mapimageview.setImageBitmap(bitmapa);
            }
        });
    }

    //Uruchamia glowny service aplikacji
    private void wysartujService() {
        startService(new Intent(this, AppServiceWear.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;

        znajdzLayouty();
        ustawEkran();
        ustawZmienne();
        znajdzWidoki();
        ustawDateBudowania();
        przypiszAkcjeDoWidokow();
        ustawPager();
        zmienStylTextView();
        UprawnieniaWear.zainicjujUprawnienia();

        //Jesli sa uprawniania bez pytania usera to uruchamiamy aplikacje dalej
        if(UprawnieniaWear.czyNadane() == true) {
            wysartujService();
        }
    }

    private void odswiezWatekRysujJesliNieNull() {
        if(AppServiceWear.service != null) {
            if(AppServiceWear.service.rysujwatek != null) {
                AppServiceWear.service.rysujwatek.odswiez = true;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        activitywidoczne = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        activitywidoczne = true;
        odswiezWatekRysujJesliNieNull();
    }

    @Override
    public void onStart() {
        super.onStart();
        activitywidoczne = true;
        WearWear.wyslijSTARTGPS();
        odswiezWatekRysujJesliNieNull();
    }

    @Override
    public void onStop() {
        super.onStop();
        WearWear.wyslijSTOPGPS();
        activitywidoczne = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activitywidoczne = false;
    }

    private void ustawPostepProgessBaru(final ProgressBar bar, final int postep) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bar.setProgress(postep);
            }
        });
    }

    public void ustawProgressPrzygotowanie(int postep) {
        ustawPostepProgessBaru(przygotowanieprogressbar, postep);
    }

    public void ustawInfoPrzygotowanie(final String text) {
        ustawTextWTextView(przygotowanieinfo, text);
    }

    private void ustawWidocznoscWidoku(final View view, final int widocznosc) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(widocznosc);
            }
        });
    }

    public void zakonczPrzygotowanie() {
        ustawWidocznoscWidoku(przygotowanieinfo, View.INVISIBLE);
        ustawWidocznoscWidoku(przygotowanieprogressbar, View.INVISIBLE);
        ustawWidocznoscWidoku(textgora, View.INVISIBLE);
        ustawWidocznoscWidoku(pomniejsz, View.VISIBLE);
        ustawWidocznoscWidoku(powieksz, View.VISIBLE);
        ustawWidocznoscWidoku(infomale, View.VISIBLE);
        ustawWidocznoscWidoku(trasainfo, View.VISIBLE);
        ustawWidocznoscWidoku(mapaczas, View.VISIBLE);
    }



    //Pokazuje konunikat w watku UI
    public void pokazToast(final String komunikat) {
        if(activitywidoczne) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), komunikat, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    //Obsluga uprawnien, aplikcji nie ma sensu uruchamiac jesli uzytkownik nie dal uprawnien
    @Override
    public void onRequestPermissionsResult(int kod, String[] uprawnienia, int[] odpowiedzi) {
        super.onRequestPermissionsResult(kod, uprawnienia, odpowiedzi);

        //Sprawdzamy kazde uprawnienie czy zostalo nadane
        for(int i = 0; i < uprawnienia.length; i++) {
            if(uprawnienia[i].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                if(odpowiedzi[i] == PackageManager.PERMISSION_GRANTED) {
                    UprawnieniaWear.odczyt = true;
                }
            }
            if(uprawnienia[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                if(odpowiedzi[i] == PackageManager.PERMISSION_GRANTED) {
                    UprawnieniaWear.zapis = true;
                }
            }
            if(uprawnienia[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                if(odpowiedzi[i] == PackageManager.PERMISSION_GRANTED) {
                    UprawnieniaWear.lokalizacja = true;
                }
            }
        }

        //Jesli sa uprawniania do aplikacja dziala dalej, jesli nie to koniec
        if(UprawnieniaWear.czyNadane() == true) {
            wysartujService();
        } else {
            pokazToast(KomunikatyWear.BRAKUPRAWNINEN);
            finish();
        }
    }

    //Konczy dzialanie programu
    public void zakonczCalaAplikacje() {
        stopService(new Intent(MainActivityWear.this, AppServiceWear.class));
        finish();
    }

    public void wypelnijPamiec() {
        ustawTextWTextView(pamiectextview, RozneWear.pobierzPamiec());
    }

    public void wypelnijBaterie() {
        ustawTextWTextView(bateriatextview, RozneWear.pobierzBaterie());
    }

    private void ustawAntyAliasNaPodsumowaniu(boolean alias) {
        podczas.getPaint().setAntiAlias(alias);
        podstart.getPaint().setAntiAlias(alias);
        poddlugosc.getPaint().setAntiAlias(alias);
        podczastrasy.getPaint().setAntiAlias(alias);
        podgps.getPaint().setAntiAlias(alias);
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        WearWear.wyslijSTOPGPS();
        wrocdowidoku = pager.getCurrentItem();
        if(wrocdowidoku == 3) {
            wrocdowidoku = 0;
        }
        ustawCzasNaPodsumowaniu();
        wypelnijPodsumowanie(true);
        ustawAntyAliasNaPodsumowaniu(false);
        pager.setCurrentItem(2);
        activitywidoczne = false;
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        ustawCzasNaPodsumowaniu();
        wypelnijPodsumowanie(true);
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
        WearWear.wyslijSTARTGPS();
        activitywidoczne = true;
        ustawCzasNaPodsumowaniu();
        wypelnijPodsumowanie(true);
        wypelnijPodsumowanieNaWidokuMapy();
        ustawAntyAliasNaPodsumowaniu(true);
        pager.setCurrentItem(wrocdowidoku);
    }
}
