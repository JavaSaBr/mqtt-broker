# JMQTT-Broker

JMQTT-Broker is an open source (GPL v3) Java based MQTT Broker with network implementation based on Java.NIO.2

## Dependencies for building
### java 11+
##### Ubuntu:
```bash
sudo wget -qO - https://adoptopenjdk.jfrog.io/adoptopenjdk/api/gpg/key/public | sudo apt-key add -
sudo add-apt-repository --yes https://adoptopenjdk.jfrog.io/adoptopenjdk/deb/
sudo apt-get install adoptopenjdk-11-hotspot
```
##### Windows: [Windows x64 installer](https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.4%2B11/OpenJDK11U-jdk_x64_windows_hotspot_11.0.4_11.msi)
##### MacOS: [MacOS x64 installer](https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.4%2B11.4/OpenJDK11U-jdk_x64_mac_hotspot_11.0.4_11.pkg)
### Docker
##### Ubuntu: [installation guide](https://docs.docker.com/install/linux/docker-ce/ubuntu)
##### Windows: [installation guide](https://docs.docker.com/docker-for-windows/install)
##### MacOS: [installation guide](https://docs.docker.com/docker-for-mac/install)
## Build
```bash
./gradlew buildSingleArtifact 
```
## Run
```bash
./gradlew bootRun
```
