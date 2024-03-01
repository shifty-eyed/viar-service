package org.viar.tracker.model;

import lombok.Data;

import java.util.Map;

@Data
public class TrackerNodeConfig {
    private String serverHost;
    private int serverPort;
    private Map<String, Integer> cameraNameToId;
}
