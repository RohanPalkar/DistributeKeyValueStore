package edu.dkv.internal.network;

import edu.dkv.exceptions.NetworkException;
import edu.dkv.internal.common.Utils;
import edu.dkv.sdk.NetworkService;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

import static edu.dkv.internal.common.Constants.MAX_MSG_SIZE;

public class DatagramService implements NetworkService {

    private final static Logger logger = LogManager.getLogger(DatagramService.class);

    private DatagramSocket socket;
    private final int port;
    private final ApplicationBuffer buffer;

    public DatagramService(int port){
        this.port = port;
        this.buffer = null;
    }

    public DatagramService(int port, ApplicationBuffer buffer){
        this.port = port;
        this.buffer = buffer;
    }

    /**
     * Starts the network service or connection.
     * Applicable for connection oriented protocols
     */
    @Override
    public void start() {
        try {
            this.socket = new DatagramSocket(port);
            logger.debug("Socket open successfully!!!");

            if(socket.isClosed() && !socket.isConnected())
                throw new NetworkException("ERROR: Socket NOT opened or connected !!!");
        } catch (SocketException e) {
            logger.error("Could not initialize Datagram service. \n{}", Utils.getFullStackTrace(e));
        }
    }

    /**
     * Stops or terminates the connection.
     * Applicable for connection oriented protocols
     */
    @Override
    public void stop() {
        if(socket != null){
            logger.trace("Attempting to close the socket...");

            if(socket.isClosed()){
                logger.trace("Socket is already closed.");
                return;
            }

            socket.close();
            logger.debug("Socket closed successfully !!!");

            if(!socket.isClosed())
                throw new NetworkException("ERROR: Socket still OPEN after closing.");

            return;
        }
        logger.trace("Socket is not intialized");
    }

    /**
     * Sends the data via the implemented protocol of the {@code NetworkService}
     *
     * @param endPoint - the destination hostname and the port
     * @param data     - the data to be sent as byte array
     */
    @Override
    public void sendMessage(final EndPoint endPoint, final byte[] data) {
        _sendMessage(endPoint, data);
    }

    /**
     * Sends the data via the implemented protocol of the {@code NetworkService}
     *
     * @param endPoint - the destination hostname and the port
     * @param data     - the data to be sent as object.
     */
    @Override
    public void sendMessage(final EndPoint endPoint, final Serializable data) {
        byte[] dataBytes = SerializationUtils.serialize(data);
        _sendMessage(endPoint, dataBytes);
    }

    /**
     * Sends the data via the implemented protocol of the {@code NetworkService}
     *
     * @param endPoint - the destination hostname and the port
     * @param data     - the data to be sent as string data.
     */
    @Override
    public void sendMessage(EndPoint endPoint, String data) {
        _sendMessage(endPoint, data.getBytes(StandardCharsets.UTF_8));
    }

    private void _sendMessage(final EndPoint endPoint, final byte[] data){
        try {
            logger.trace("Attempting to send packet of length [{}] to {}:{}",
                    data.length, endPoint.getAddress(), endPoint.getPort());
            DatagramPacket packet = new DatagramPacket(data, data.length, endPoint.getAddress(), endPoint.getPort());
            socket.send(packet);
            logger.trace("Message SENT successfully!!!");
        } catch (IOException e) {
            logger.error("Unable to send message. \n{}", Utils.getFullStackTrace(e));
        }
    }

    /**
     * Receives the data via the implemented protocol of the {@code NetworkService}
     * and adds it to the application buffer.
     *
     * @return {@code ApplicationBuffer} instance
     */
    @Override
    public ApplicationBuffer receiveMessage() {
        byte[] dataBytes = new byte[MAX_MSG_SIZE];
        DatagramPacket packet = new DatagramPacket(dataBytes, dataBytes.length);
        try {
            logger.trace("Attempting to received packet...");
            socket.receive(packet);
            if(buffer != null)
                buffer.addToBuffer(packet.getData(), packet.getLength());
            logger.trace("Message RECEIVED successfully of length: {}", packet.getLength());
        } catch (IOException e) {
            logger.error("Unable to receive message. \n{}", Utils.getFullStackTrace(e));
        }
        return buffer;
    }
}
