package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class TestUtils {

        public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
                MediaType.APPLICATION_JSON.getSubtype(), StandardCharsets.UTF_8);

        public static byte[] convertObjectToJsonBytes(Object object) throws IOException {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsBytes(object);
        }
    }
