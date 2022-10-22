package edu.duke.ece568.proj;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class FrontEndcommunicator extends Thread{

  PurchaseListener actionListener;

    public FrontEndcommunicator(PurchaseListener actionListener) {
        this.setDaemon(true);//always
        this.actionListener = actionListener;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()){
            try (serverSocketWrapper frontEndServerSocket = new serverSocketWrapper(6666)){
                //System.out.println("Listening connection from front-end at 6666");
                while (!Thread.currentThread().isInterrupted()){
                    //System.out.println("accepting front end request");
                    Socket s = frontEndServerSocket.accept();
                    if (s != null){//get connection from front end, now handle purchase
                        System.out.println("connected to front end at port 6666, now handle purchase request");
                        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                        PrintWriter writer = new PrintWriter(s.getOutputStream());
                        String purchasePackageID = reader.readLine();
                        System.out.println("Client want to buy(packageID): " + purchasePackageID);
                        long packageID = Long.parseLong(purchasePackageID);
                        writer.write(String.format("ack:%d", packageID));
                        writer.flush();
                        // close the connection
                        s.close();
                        if (actionListener != null){
                            actionListener.doPurchase(packageID);
                        }
                    }
                    else{
                        //System.out.println("Can not get front end message, retry...");
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
