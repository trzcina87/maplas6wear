package trzcina.maplas6;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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

import trzcina.maplas6.pomocwear.KomunikatyWear;
import trzcina.maplas6.pomocwear.PagerAdapterWear;
import trzcina.maplas6.pomocwear.PaintyWear;
import trzcina.maplas6.pomocwear.UprawnieniaWear;
import trzcina.maplas6.pomocwear.WearWear;
import trzcina.maplas6.pomocwear.WiadomoscWear;

public class MainActivityWear extends WearableActivity {

    public static volatile MainActivityWear activity;

    private LayoutInflater inflater;

    private LinearLayout contentviewlayout;
    public RelativeLayout mapapage;
    public LinearLayout dodaj1page;
    public RelativeLayout podsumowaniepage;
    public LinearLayout ustawieniapage;
    public ImageView mapimageview;
    public Button zakonczbutton;

    private ViewPager pager;
    private ProgressBar przygotowanieprogressbar;
    private TextView przygotowanieinfo;
    private TextView textgora;
    private ImageView powieksz;
    private ImageView pomniejsz;
    private ImageView infomale;
    public TextView mapaczas;
    private TextView podczas;
    private TextView poddlugosc;
    private TextView podstart;
    private TextView podczastrasy;

    public boolean activitywidoczne = true;

    //Dla danego id zasobu (w res/layout) zwraca widok
    private LinearLayout znajdzLinearLayout(int zasob) {
        LinearLayout layouttmp = (LinearLayout) inflater.inflate(zasob, null);
        layouttmp.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        return layouttmp;
    }

    private RelativeLayout znajdzRelativeLayout(int zasob) {
        RelativeLayout layouttmp = (RelativeLayout) inflater.inflate(zasob, null);
        layouttmp.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        return layouttmp;
    }

    private void znajdzLayouty() {
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentviewlayout = znajdzLinearLayout(R.layout.contentviewlayout);
        mapapage = znajdzRelativeLayout(R.layout.mapapage);
        dodaj1page = znajdzLinearLayout(R.layout.dodaj1page);
        podsumowaniepage = znajdzRelativeLayout(R.layout.podsumowaniepage);
        ustawieniapage = znajdzLinearLayout(R.layout.ustawieniapage);
    }

    private void ustawDateBudowania() {
        Date buildDate = new Date(BuildConfig.TIMESTAMP);
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        textgora.setText(dateFormat.format(buildDate));
    }

    private void ustawEkran() {
        setContentView(R.layout.contentviewlayout);
        setAmbientEnabled();
    }

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
    }

    public void uzupelnijCzas(final String czas) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mapaczas.setText(czas);
            }
        });
    }

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
                        WiadomoscWear wiadomosc = WearWear.wyslijWiadomoscICzekajNaOdpowiedz("POINT", ((String)(view.getTag())).getBytes(), 3, 1000);
                        if(wiadomosc == null) {
                            pokazToast("Brak połączenia z telefonem!");
                        } else {
                            String daneodpowiedz = new String(wiadomosc.dane);
                            if(daneodpowiedz.startsWith("TRUE")) {
                                pokazToast("Zapisano: " + ((String)(view.getTag())).getBytes());
                            } else {
                                pokazToast("Błąd zapisu na telefonie!");
                            }
                        }
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
        znajdzWidoki();
        ustawDateBudowania();
        przypiszAkcjeDoWidokow();
        ustawPager();
        PaintyWear.inicjujPainty();
        UprawnieniaWear.zainicjujUprawnienia();

        //Jesli sa uprawniania bez pytania usera to uruchamiamy aplikacje dalej
        if(UprawnieniaWear.czyNadane() == true) {
            wysartujService();
        }
    }

    public void ustawProgressPrzygotowanie(final int progress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                przygotowanieprogressbar.setProgress(progress);
            }
        });
    }

    public void ustawInfoPrzygotowanie(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                przygotowanieinfo.setText(text);
            }
        });
    }

    public void zakonczPrzygotowanie() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                przygotowanieinfo.setVisibility(View.INVISIBLE);
                przygotowanieprogressbar.setVisibility(View.INVISIBLE);
                textgora.setVisibility(View.INVISIBLE);
                pomniejsz.setVisibility(View.VISIBLE);
                powieksz.setVisibility(View.VISIBLE);
                infomale.setVisibility(View.VISIBLE);
            }
        });
    }



    //Pokazuje konunikat w watku UI
    public void pokazToast(final String komunikat) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), komunikat, Toast.LENGTH_SHORT).show();
            }
        });
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

    public void ustawCzasNaPodsumowaniu() {
        final Date obecnyczas = new Date(System.currentTimeMillis());
        final DateFormat format = new SimpleDateFormat("HH:mm");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                podczas.setText(format.format(obecnyczas));
            }
        });
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        ustawCzasNaPodsumowaniu();
        podczas.getPaint().setAntiAlias(false);
        podstart.getPaint().setAntiAlias(false);
        poddlugosc.getPaint().setAntiAlias(false);
        podczastrasy.getPaint().setAntiAlias(false);
        pager.setCurrentItem(2);
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        ustawCzasNaPodsumowaniu();
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
        ustawCzasNaPodsumowaniu();
        podczas.getPaint().setAntiAlias(true);
        podstart.getPaint().setAntiAlias(true);
        poddlugosc.getPaint().setAntiAlias(true);
        podczastrasy.getPaint().setAntiAlias(true);
        pager.setCurrentItem(0);
    }
}
