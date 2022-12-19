package edu.dkv.sdk;

import edu.dkv.internal.network.ApplicationBuffer;
import edu.dkv.internal.network.EndPoint;

import java.io.Serializable;

public interface NetworkService {

    /**
     * Starts the network service or connection.
     * Applicable for connection oriented protocols
     */
    void start();

    /**
     * Stops or terminates the connection.
     * Applicable for connection oriented protocols
     */
    void stop();

    /**
     * Sends the data via the implemented protocol of the {@code NetworkService}
     *
     * @param endPoint      - the destination hostname and the port
     * @param data          - the data to be sent as byte array
     */
    void sendMessage(EndPoint endPoint, byte[] data);

    /**
     * Sends the data via the implemented protocol of the {@code NetworkService}
     *
     * @param endPoint      - the destination hostname and the port
     * @param data          - the data to be sent as serializable data.
     */
    void sendMessage(EndPoint endPoint, Serializable data);

    /**
     * Sends the data via the implemented protocol of the {@code NetworkService}
     *
     * @param endPoint      - the destination hostname and the port
     * @param data          - the data to be sent as string data.
     */
    void sendMessage(EndPoint endPoint, String data);

    /**
     * Receives the data via the implemented protocol of the {@code NetworkService}
     * and adds it to the application buffer.
     *
     * @return {@code ApplicationBuffer} instance
     */
    ApplicationBuffer receiveMessage();
}
