package com.ss.mqtt.broker.util;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public class MqttDataUtils {

    /**
     * Write a MQTT multi-byte integer to byte buffer.
     *
     * @throws IllegalArgumentException if number is too big.
     */
    public static @NotNull ByteBuffer writeMbi(long number, @NotNull ByteBuffer buffer) {

        var sizeInBytes = 0;
        var valueToWrite = number;
        do {

            var digit = (byte) (valueToWrite % 128);
            valueToWrite = valueToWrite / 128;

            if (valueToWrite > 0) {
                digit |= 0x80;
            }

            buffer.put(digit);
            sizeInBytes++;

        } while (valueToWrite > 0);

        if (sizeInBytes > 4) {
            throw new IllegalArgumentException(number + " is too big.");
        }

        return buffer;
    }

    /**
     * Read a MQTT multi-byte integer from byte buffer.
     *
     * @return -1 if buffer's data isn't enough to read integer.
     */
    public static long readMbi(@NotNull ByteBuffer buffer) {

        var originalPos = buffer.position();

        long result = 0;
        int multiplier = 1;

        byte readValue;
        do {

            if (!buffer.hasRemaining()) {
                buffer.position(originalPos);
                return -1;
            }

            readValue = buffer.get();
            result += ((readValue & 0x7F) * multiplier);
            multiplier *= 128;

        } while ((readValue & 0x80) != 0);

        return result;
    }

    /**
     * Get byte count of MQTT multi-byte integer.
     */
    public static int sizeOfMbi(long number) {

        var sizeInBytes = 0;
        var valueToWrite = number;
        do {
            valueToWrite = valueToWrite / 128;
            sizeInBytes++;
        } while (valueToWrite > 0);

        return sizeInBytes;
    }
}
