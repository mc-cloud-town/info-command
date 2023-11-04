package monkey.info.command.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

// from https://github.com/gnembon/carpet-extra/blob/70ac69d9095a1cf0cfbc6f1f8e49fed84d012a90/src/main/java/carpetextra/utils/CarpetExtraTranslations.java
public class CarpetExtraTranslations {
    public static Map<String, String> getTranslationFromResourcePath(String lang) {
        InputStream langFile = CarpetExtraTranslations.class.getClassLoader().getResourceAsStream("assets/info-command/lang/%s.json".formatted(lang));
        if (langFile == null) {
            // we don't have that language
            return Collections.emptyMap();
        }

        String jsonData;
        try {
            jsonData = IOUtils.toString(langFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return Collections.emptyMap();
        }

        Gson gson = new GsonBuilder().setLenient().create(); // lenient allows for comments
        return gson.fromJson(jsonData, new TypeToken<Map<String, String>>() {
        }.getType());
    }
}
