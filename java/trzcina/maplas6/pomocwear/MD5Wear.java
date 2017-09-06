package trzcina.maplas6.pomocwear;

import java.math.BigInteger;
import java.security.MessageDigest;

public class MD5Wear {

    //Liczymy MD5 stringa
    public static String md5(String tekst) {
        try {
            MessageDigest md5skrot = MessageDigest.getInstance("MD5");
            md5skrot.update(tekst.getBytes(), 0, tekst.getBytes().length);
            String md5 = new BigInteger(1, md5skrot.digest()).toString(16);
            while (md5.length() < 32) {
                md5 = "0" + md5;
            }
            return md5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
