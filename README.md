# viar-service

### High level architecture
Cameras connected to tracker-node, tracker-node connected to hub-server, hub-server to game engine

tracker-node:
- capture images from cameras
- detect features in images: body parts, aruco codes
- track features
- send features with their 2d coordinates (CameraSpaceFeature) to hub-server

hub-server:
- receive CameraSpaceFeature set from multiple tracker-nodes
- calculate 3d coordinates (WorldSpaceFeature) for each CameraSpaceFeature
- send WorldSpaceFeature set to game engine

### Configuration
cameras.json: 
- intrinsics and extrinsics for each camera
- has all known cameras connected to different tracker-nodes, used by tracker-node and hub-server
- each camera has a unique name

tracker.json:
- configuration for tracker-node to connect to hub-server
- mapping of camera names to device_id in OS

### Detection
Happens every n-th frame to find feature-zones in the image and track in the other frames

Aruco codes: if detected, got 4 corners (and normal vector if camera is calibrated).
Need to derive a single point from these 4 corners and normal vector.
- easy way: use the center of the aruco code
- better way: use the normal vector and additional offset config to calculate the point. This way we can assign multiple aruco codes to a single feature for robustness.
  
Body parts: use model, convert heatmap to set of features, each representing a body part
    
