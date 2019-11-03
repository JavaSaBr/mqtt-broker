package com.ss.mqtt.broker.service.impl;

import com.ss.mqtt.broker.exception.CredentialsSourceException;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class FileCredentialsSource extends AbstractCredentialSource {

    private final String fileName;

    public FileCredentialsSource(@NotNull String fileName) {
        this.fileName = fileName;
        init();
    }

    @Override
    void init() {
        URL credentialUrl = FileCredentialsSource.class.getClassLoader().getResource(fileName);
        if (credentialUrl == null) {
            throw new CredentialsSourceException("Credentials file could not be found");
        }
        try {
            String credentialsPath = credentialUrl.getPath();
            Properties credenrialsProperties = new Properties();
            credenrialsProperties.load(new FileInputStream(credentialsPath));
            credenrialsProperties.forEach(this::putCredentials);
        } catch (IOException e) {
            throw new CredentialsSourceException(e);
        }
    }
}
