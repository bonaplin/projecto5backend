package aor.paj.utils;

import aor.paj.dto.UserDto;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class JsonUtils {

    private static JsonbConfig config = new JsonbConfig().withFormatting(true);
    private static Jsonb jsonb = JsonbBuilder.create(config);
    private static final String filename = "users.json";
    public static String convertObjectToJson(Object object) {
        return jsonb.toJson(object);
    }

}
