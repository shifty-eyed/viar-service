package org.viar.tracker;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.viar.tracker.model.MakerFeaturePointOffset;
import org.viar.tracker.model.TrackerNodeConfig;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class TrackerConfig {

    @Value("file:conf/tracker.json")
    private Resource trackerConfigFile;

    @Value("file:conf/feature-markers.json")
    private Resource featureMarkersConfigFile;

    @Bean
    public TrackerNodeConfig trackerNodeConfig() throws IOException {
        try(InputStream in = trackerConfigFile.getInputStream()) {
            final String data = IOUtils.toString(in, StandardCharsets.UTF_8);
            Gson gson = new Gson();
            return gson.fromJson(data, TrackerNodeConfig.class);
        }
    }

    @Bean
    public Map<Integer, MakerFeaturePointOffset> markerFeaturePointOffsets() throws IOException {
        try(InputStream in = featureMarkersConfigFile.getInputStream()) {
            final String data = IOUtils.toString(in, StandardCharsets.UTF_8);
            Gson gson = new Gson();
            Type collectionType = new TypeToken<Collection<MakerFeaturePointOffset>>(){}.getType();
            Collection<MakerFeaturePointOffset> entries = gson.fromJson(data, collectionType);
            return entries.stream().collect(
                Collectors.toMap(MakerFeaturePointOffset::getMarkerId, Function.identity()));

        }
    }


}
