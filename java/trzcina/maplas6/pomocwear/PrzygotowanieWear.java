package trzcina.maplas6.pomocwear;

import java.io.File;
import java.io.FilenameFilter;

import trzcina.maplas6.MainActivityWear;

@SuppressWarnings("PointlessBooleanExpression")
public class PrzygotowanieWear {

    //Tworzy katalog niezbedne dla programu
    public static void utworzKatalogi() {
        boolean katalog1 = RozneWear.utworzKatalog(StaleWear.SCIEZKAMAPLAS);
        if(katalog1 == false) {
            MainActivityWear.activity.zakonczCalaAplikacje();
        }
    }

}

