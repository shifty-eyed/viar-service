package org.viar.tracker;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.viar.tracker.model.TrackerNodeConfig;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class TrackerConfig {

    @Value("file:conf/tracker.json")
    private Resource trackerConfigFile;

    @Bean
    public TrackerNodeConfig trackerNodeConfig() throws IOException {
        try(InputStream in = trackerConfigFile.getInputStream()) {
            final String data = IOUtils.toString(in, StandardCharsets.UTF_8);
            Gson gson = new Gson();
            return gson.fromJson(data, TrackerNodeConfig.class);
        }
    }

}
