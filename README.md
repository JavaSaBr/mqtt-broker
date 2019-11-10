# JMQTT-Broker

JMQTT-Broker is an open source (GPL v3) Java based MQTT Broker with network implementation based on Java.NIO.2

## Dependencies for building
### java 13+
##### Ubuntu:
```bash
sudo wget -qO - https://adoptopenjdk.jfrog.io/adoptopenjdk/api/gpg/key/public | sudo apt-key add -
sudo add-apt-repository --yes https://adoptopenjdk.jfrog.io/adoptopenjdk/deb/
sudo apt-get install adoptopenjdk-13-hotspot
```
##### Windows: [Windows x64 installer](https://github.com/AdoptOpenJDK/openjdk13-binaries/releases/download/jdk-13.0.1%2B9/OpenJDK13U-jdk_x64_windows_hotspot_13.0.1_9.msi)
##### MacOS: [MacOS x64 installer](https://github.com/AdoptOpenJDK/openjdk13-binaries/releases/download/jdk-13.0.1%2B9/OpenJDK13U-jdk_x64_mac_hotspot_13.0.1_9.pkg)
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
