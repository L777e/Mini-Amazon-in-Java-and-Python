package edu.duke.ece568.proj;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Socket(connected to UPS server) wrapper, socket will auto close after use
 */
public class serverSocketWrapper implements AutoCloseable{
    ServerSocket CommunicationSocket;
    @Override
    public void close() throws Exception {
        CommunicationSocket.close();
    }
    public serverSocketWrapper(int portNum) throws IOException {
        CommunicationSocket = new ServerSocket(portNum);
        CommunicationSocket.setSoTimeout(Configurations.TIME_OUT);
    }
    public Socket accept(){
        try {
            return CommunicationSocket.accept();
        }
        catch(SocketTimeoutException timeoutE){
            System.out.println("Waiting for connection...");
            return null;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
