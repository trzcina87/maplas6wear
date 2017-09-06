package trzcina.maplas6.lokalizacjawear;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import trzcina.maplas6.pomocwear.StaleWear;

public class PlikGPXWear {

    public String sciezka;
    public String nazwa;
    public int stan;
    public List<PunktNaMapieWear> punkty;
    public List<PunktWTrasieWear> trasa;
    public Boolean zaznaczony;
    public long rozmiar;
    public long rozmiarbajty;
    public float dlugosctrasy;
    public long czastrasy;

    public PlikGPXWear(String sciezka) {
        this.sciezka = sciezka;
        nazwa = new File(sciezka).getName().replace(".gpx", "");
        stan = StaleWear.PLIKNOWY;
        punkty = new ArrayList<>(100);
        trasa = new ArrayList<>(100);
        zaznaczony = false;
        rozmiar = 0;
        rozmiarbajty = 0;
        dlugosctrasy = 0;
        czastrasy = 0;
        try {
            rozmiar = new File(sciezka).length();
            rozmiarbajty = rozmiar;
            rozmiar = Math.round(rozmiar / (double)1024);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void naprawJesliTrzeba() {
        try {
            BufferedReader input = new BufferedReader(new FileReader(sciezka));
            String ostatnialinia = "</gpx>";
            String czytanalinia;
            boolean plikpunktowbool = false;
            boolean pliktrasbool = false;
            while ((czytanalinia = input.readLine()) != null) {
                if(czytanalinia.trim().startsWith("<wpt ")) {
                    plikpunktowbool = true;
                }
                if(czytanalinia.trim().startsWith("<trk>")) {
                    pliktrasbool = true;
                }
                if(czytanalinia.trim().length() > 0) {
                    ostatnialinia = czytanalinia;
                }
            }
            input.close();
            if(ostatnialinia.trim().replace(" ", "").endsWith("</gpx>") == false) {
                if(!((pliktrasbool) && (plikpunktowbool))) {
                    if(pliktrasbool) {
                        FileWriter filewriter = new FileWriter(new File(sciezka), true);
                        PrintWriter printwriter = new PrintWriter(filewriter);
                        printwriter.println("");
                        printwriter.println("    </trkseg>");
                        printwriter.println("  </trk>");
                        printwriter.println("</gpx>");
                        printwriter.flush();
                        printwriter.close();
                    }
                    if(plikpunktowbool) {
                        FileWriter filewriter = new FileWriter(new File(sciezka), true);
                        PrintWriter printwriter = new PrintWriter(filewriter);
                        printwriter.println("");
                        printwriter.println("</gpx>");
                        printwriter.flush();
                        printwriter.close();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String pobierzWartoscParametru(String tag, Element element) {
        try {
            NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
            Node node = nodeList.item(0);
            if (node != null) {
                return node.getNodeValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Document otworzPlik() throws Exception {
        InputStream strumien = new FileInputStream(sciezka);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document dokument = dBuilder.parse(strumien);
        Element element = dokument.getDocumentElement();
        element.normalize();
        return dokument;
    }

    private void szukajWPT(Document dokument) {
        NodeList lista = dokument.getElementsByTagName("wpt");
        for (int i = 0; i < lista.getLength(); i++) {
            Node node = lista.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element)node;
                PunktNaMapieWear punktnamapie = new PunktNaMapieWear(Float.parseFloat(element.getAttribute("lon")), Float.parseFloat(element.getAttribute("lat")), pobierzWartoscParametru("name", element), pobierzWartoscParametru("cmt", element));
                punkty.add(punktnamapie);
            }
        }
    }

    private void szukajTRK(Document dokument) {
        NodeList lista = dokument.getElementsByTagName("trkpt");
        PunktWTrasieWear poprzedni = null;
        long czasstart = 0;
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(tz);
        Element element = null;
        for(int i = 0; i < lista.getLength(); i++) {
            Node node = lista.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                element = (Element) node;
                PunktWTrasieWear punktwtrasie = new PunktWTrasieWear(Float.parseFloat(element.getAttribute("lon")), Float.parseFloat(element.getAttribute("lat")), 0);
                trasa.add(punktwtrasie);
                if(poprzedni == null) {
                    String data = pobierzWartoscParametru("time", element);
                    try {
                        Date datad = dateFormat.parse(data);
                        czasstart = datad.getTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                if(poprzedni != null) {
                    dlugosctrasy = dlugosctrasy + PunktWTrasieWear.zmierzDystans(poprzedni, punktwtrasie);
                }
                poprzedni = punktwtrasie;
            }
        }
        if(poprzedni != null) {
            String data = pobierzWartoscParametru("time", element);
            try {
                Date datakoniec = dateFormat.parse(data);
                czastrasy = datakoniec.getTime() - czasstart;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public void parsuj() {
        stan = StaleWear.PLIKROBIE;
        try {
            Document dokument = otworzPlik();
            szukajWPT(dokument);
            szukajTRK(dokument);
            stan = StaleWear.PLIKGOTOWY;
        } catch (Exception e) {
            e.printStackTrace();
            stan = StaleWear.PLIKBLAD;
        }
    }

}
