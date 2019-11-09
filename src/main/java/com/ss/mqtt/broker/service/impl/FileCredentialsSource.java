package com.ss.mqtt.broker.service.impl;

import com.ss.mqtt.broker.exception.CredentialsSourceException;
import com.ss.rlib.common.util.dictionary.Dictionary;
import com.ss.rlib.common.util.dictionary.DictionaryCollectors;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class FileCredentialsSource extends AbstractCredentialSource {

    private final String fileName;

    public FileCredentialsSource(@NotNull String fileName) {
        this.fileName = fileName;
        init();
    }

    @Override
    void init() {
        var credentialUrl = FileCredentialsSource.class.getClassLoader().getResource(fileName);
        if (credentialUrl == null) {
            throw new CredentialsSourceException("Credentials file could not be found");
        }
        try {
            var credentialsProperties = new Properties();
            credentialsProperties.load(new FileInputStream(credentialUrl.getPath()));

            Dictionary<String, byte[]> creds = credentialsProperties.entrySet()
                .stream()
                .collect(DictionaryCollectors.toObjectDictionary(
                    entry -> entry.getKey().toString(),
                    entry -> entry.getValue().toString().getBytes(StandardCharsets.UTF_8)
                ));

            putAll(creds);

        } catch (IOException e) {
            throw new CredentialsSourceException(e);
        }
    }
}
