package edu.dkv.internal.network;

import edu.dkv.exceptions.NetworkException;
import edu.dkv.internal.common.Utils;
import edu.dkv.internal.entities.EndPoint;
import edu.dkv.sdk.NetworkService;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import static edu.dkv.internal.common.Constants.MAX_MSG_SIZE;
import static edu.dkv.internal.common.Constants.UDP_SOCKET_TIMEOUT;
import static edu.dkv.internal.common.Utils.getFullStackTrace;

public class DatagramService<T> implements NetworkService {

    private final static Logger logger = LogManager.getLogger(DatagramService.class);

    private DatagramSocket socket;
    private final int port;
    private final ApplicationBuffer<T> appBuffer;

    /**
     * Constructor : Primarily use for sending packets.
     * @param port - port no. of the service.
     */
    public DatagramService(int port, ApplicationBuffer<T> appBuffer){
        this.port = port;
        this.appBuffer = appBuffer;
    }

    /**
     * Starts the network service or connection.
     * Applicable for connection oriented protocols
     */
    @Override
    public void start() {
        try {
            this.socket = new DatagramSocket(port);
            this.socket.setSoTimeout(UDP_SOCKET_TIMEOUT);
            logger.debug("Socket open successfully!!!");

            if(socket.isClosed() && !socket.isConnected())
                throw new NetworkException("ERROR: Socket NOT opened or connected !!!");
        } catch (SocketException e) {
            logger.error("Could not initialize Datagram service. \n{}", getFullStackTrace(e));
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
    public void sendMessages(final EndPoint endPoint, final byte[] data) {
        _sendMessage(endPoint, data);
    }

    /**
     * Sends the data via the implemented protocol of the {@code NetworkService}
     *
     * @param endPoint - the destination hostname and the port
     * @param data     - the data to be sent as object.
     */
    @Override
    public void sendMessages(final EndPoint endPoint, final Serializable data) {
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
    public void sendMessages(EndPoint endPoint, String data) {
        _sendMessage(endPoint, SerializationUtils.serialize(data));
    }

    /**
     * Receives the data via the implemented protocol of the {@code NetworkService}
     *
     * This implementation works in a non-blocking fashion as there is a time-out
     * set in the socket instance that throws the {@code SocketTimeoutException}
     * after the time-out elapses. The exception is ignored and the control is
     * returned to the caller to retry or loop around this implementation as desired.
     *
     * If the packets are indeed received, then the packets are pushed in to the
     * application buffer as byte[] which will be handled by the higher layers.
     *
     */
    @Override
    public void receiveMessages() {
        byte[] dataBytes = new byte[MAX_MSG_SIZE];
        DatagramPacket packet = new DatagramPacket(dataBytes, dataBytes.length);

        while(!socket.isClosed()){
            try {
                socket.receive(packet);
                logger.debug("Packets RECEIVED from {} of length: {}", packet.getSocketAddress().toString(), packet.getLength());
                appBuffer.buffer(packet.getData(), packet.getLength());
                break;
            } catch (SocketTimeoutException s) {
                return;
            } catch (SocketException s1){
                logger.error("SocketException encountered: \n{}", getFullStackTrace(s1));
            } catch (IOException e) {
                logger.error("ERROR: Unable to receive messages !!!\n{}", getFullStackTrace(e));
            }
        }
    }

    private void _sendMessage(final EndPoint endPoint, final byte[] data){
        if(!socket.isClosed()) {
            try {
                logger.trace("Attempting to send packet of length [{}] to {}:{}",
                        data.length, endPoint.getAddress(), endPoint.getPort());
                DatagramPacket packet = new DatagramPacket(data, data.length, endPoint.getAddress(), endPoint.getPort());
                socket.send(packet);
                logger.trace("Message SENT successfully!!!");
            } catch (IOException e) {
                logger.error("Unable to send message. \n{}", getFullStackTrace(e));
            }
        }
    }
}
