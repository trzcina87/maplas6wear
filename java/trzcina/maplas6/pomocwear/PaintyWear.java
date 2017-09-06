package trzcina.maplas6.pomocwear;

import android.graphics.Color;
import android.graphics.Paint;

import trzcina.maplas6.MainActivityWear;

public class PaintyWear {

    public static float density;
    public static Paint paintczerwonysrodek;
    public static Paint paintzielonyokrag;
    public static Paint paintfioletowyokrag;
    public static Paint paintzielonyokragtrasa;
    public static Paint paintfioletowyokragtrasa;
    public static Paint paintbialytekst;
    public static Paint paintczerwonytekst;
    public static Paint paintczarnytekst;
    public static Paint paintczerwoneokregi;
    public static Paint paintczarnyprostokat;
    public static Paint[] painttekst;

    //Inicjuje painty uzywane przez watek rysuj
    public static void inicjujPainty() {
        density = MainActivityWear.activity.getResources().getDisplayMetrics().density;
        paintczerwonysrodek = new Paint();
        paintczerwonysrodek.setStyle(Paint.Style.STROKE);
        paintczerwonysrodek.setColor(Color.RED);
        paintczerwonysrodek.setStrokeWidth(density);
        paintczerwoneokregi = new Paint();
        paintczerwoneokregi.setStyle(Paint.Style.STROKE);
        paintczerwoneokregi.setColor(Color.RED);
        paintczerwoneokregi.setStrokeWidth(density / 2.0F);
        paintzielonyokrag = new Paint();
        paintzielonyokrag.setStyle(Paint.Style.STROKE);
        paintzielonyokrag.setColor(Color.rgb(0, 255, 0));
        paintzielonyokrag.setStrokeWidth(3 * density);
        paintzielonyokragtrasa = new Paint();
        paintzielonyokragtrasa.setStyle(Paint.Style.STROKE);
        paintzielonyokragtrasa.setColor(Color.rgb(0, 255, 0));
        paintzielonyokragtrasa.setStrokeWidth(2 * density);
        paintfioletowyokragtrasa = new Paint();
        paintfioletowyokragtrasa.setStyle(Paint.Style.STROKE);
        paintfioletowyokragtrasa.setColor(Color.rgb(243, 24, 190));
        paintfioletowyokragtrasa.setStrokeWidth(2 * density);
        paintczerwonytekst = new Paint();
        paintczerwonytekst.setColor(Color.RED);
        paintczerwonytekst.setStrokeWidth(1);
        paintczerwonytekst.setTextSize(11 * density);
        paintbialytekst = new Paint();
        paintbialytekst.setColor(Color.WHITE);
        paintbialytekst.setStrokeWidth(1);
        paintbialytekst.setTextSize(11 * density);
        paintczarnytekst = new Paint();
        paintczarnytekst.setColor(Color.BLACK);
        paintczarnytekst.setStrokeWidth(1);
        paintczarnytekst.setTextSize(11 * density);
        paintfioletowyokrag = new Paint();
        paintfioletowyokrag.setStyle(Paint.Style.STROKE);
        paintfioletowyokrag.setColor(Color.rgb(243, 24, 190));
        paintfioletowyokrag.setStrokeWidth(3 * density);
        painttekst = new Paint[4];
        painttekst[0] = paintbialytekst;
        painttekst[1] = paintczerwonytekst;
        painttekst[2] = paintczarnytekst;
        painttekst[3] = paintbialytekst;
        paintczarnyprostokat = new Paint();
        paintczarnyprostokat.setColor(Color.BLACK);
        paintczarnyprostokat.setStyle(Paint.Style.FILL);
    }

}
