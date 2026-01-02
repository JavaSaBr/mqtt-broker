package javasabr.mqtt.auth.service.config.property;

import java.net.URI;

public interface FileConfig extends SwitchableProperty{

  URI fsPath();
}
