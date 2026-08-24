package ws.furrify.core.utils;

import java.util.Base64;

public class EntitySpecUtils {

    public static String encodeSpecToBase64(String spec) {
        return Base64.getUrlEncoder().encodeToString(spec.getBytes());
    }

    public static String decodeSpecFromBase64(String spec) {
        return new String(Base64.getUrlDecoder().decode(spec));
    }

}
