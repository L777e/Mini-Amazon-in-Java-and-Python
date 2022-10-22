package edu.duke.ece568.proj;

import proto.A2U;
import proto.A2U.*;
import proto.U2A.*;
import proto.WorldAmazon.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

import static edu.duke.ece568.proj.Configurations.UPS_HOST;
import static edu.duke.ece568.proj.GoogleProtocolIO.messFromStream;
import static edu.duke.ece568.proj.GoogleProtocolIO.messToStream;

public class AmazonServer {
   //in and out stream
    private InputStream in;
    private OutputStream out;
    //sequence number, should increment after use
    private long seqNum;
    // a socket wrapper to listen UPS connection request
    private serverSocketWrapper upsListener;
    // a socket wrapper to communicate with UPS
    private Socket upsCommunicator;
    //use a thread object to communicate with front-end
    private FrontEndcommunicator frontEndcommunicator;
    //all unfinished package
    private final Map<Long, Package> idMapPackage;
    // mapping between sequence number and request(the timer handle the re-send task)
    private final Map<Long, Timer> requestMapTimer;
    // server thread pool, set all before used
    private final ThreadPoolExecutor threadPool;
    // all warehouses
    private final List<AInitWarehouse> warehouses;


    synchronized long getSeqNum() {
        long currSeqNum = seqNum;
        ++seqNum;
        return currSeqNum;
    }
    /**
     * Amazon server default constructor
     */
    public AmazonServer() throws IOException {
        this.seqNum = 0;//sequence num start at 0
        this.frontEndcommunicator= null;
        this.idMapPackage = new ConcurrentHashMap<>();
        this.requestMapTimer = new ConcurrentHashMap<>();
        //initialize thread pool
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(30);
        this.threadPool = new ThreadPoolExecutor(50, 80, 5, TimeUnit.SECONDS, queue);
        this.warehouses = new SQL().queryWHs();
        this.upsCommunicator = null;
    }

    /**
     * set up server connection to ups and to world
     * @throws IOException
     */
    public void setUpServer() throws IOException {
        System.out.println("Waiting UPS connect request at " + Configurations.UPS_SERVER_PORT);
        // the server listening the request comes from UPS
        upsListener = new serverSocketWrapper(Configurations.UPS_SERVER_PORT);
        // we will keep trying until we successfully connect to the world
        while (true) {
            System.out.println("---------In setupServer----------");
            System.out.println("Listening to UPS connection");
            //set up a temp communication socket to get UPS side message
            //note that s will auto close after use, so keep listening on upsListener is fine
            upsCommunicator = upsListener.accept();
            if (upsCommunicator != null) {
                //get mess from UPS
                UToA.Builder builder = UToA.newBuilder();
                messFromStream(builder, upsCommunicator.getInputStream());
                // has a valid world id and world server feedback is success!
                System.out.println("The received mess from UPS is:" + builder.toString());
                if (builder.getWorldInfo().hasWorldid() && connectWorld(builder.getWorldInfo().getWorldid())) {
                    long wID = builder.getWorldInfo().getWorldid();//world ID(proven to be valid)
                    System.out.println("Connected to world server with world ID: "+ wID);
                    //respond with world id and result true on successfully connect to world
                    System.out.println("-1----------");
                    AToU.Builder amazonResponse = AToU.newBuilder();
                    System.out.println("0----------");
                    A2U.world_info.Builder responseWorldInfo = new A2U.world_info.Builder();
                    System.out.println("1----------");
                    responseWorldInfo.setWorldid(wID);
                    System.out.println("2----------");
                    responseWorldInfo.setResult(true);//true on success
                    System.out.println("3----------");
                    amazonResponse.setWorldInfo(responseWorldInfo);
                    System.out.println("4----------");
                    System.out.println("Successfully connected to world, now send ack mess"
                    + " to UPS server, the message is: " + amazonResponse.toString());
                    messToStream(amazonResponse.build(), upsCommunicator.getOutputStream());//
                    break;//ack, so move on
                }
                else{//upon failure to connect to world, sent result=false to require UPS resend message
                    long wID = builder.getWorldInfo().hasWorldid() ? builder.getWorldInfo().getWorldid() : -1;//world ID(not valid or does not exist)
                    System.out.println("Failed connecting to world server: "+ wID);
                    //respond with world id and result false on connect to world server failure
                    AToU.Builder amazonResponse = AToU.newBuilder();
                    A2U.world_info.Builder responseWorldInfo = new A2U.world_info.Builder();
                    responseWorldInfo.setWorldid(wID);
                    responseWorldInfo.setResult(false);//false on failure
                    amazonResponse.setWorldInfo(responseWorldInfo);
                    messToStream(amazonResponse.build(), upsCommunicator.getOutputStream());//send to UPS
                    //does not ack, so keep listening to UPS's incoming world id
                }
            }
        }
    }

    /**
     * Try connect world.
     * @param worldID
     * @return true on success
     */
    boolean connectWorld(long worldID) throws IOException {
        // set up the TCP connection to the world
        System.out.println("Trying to connect to world with world is = "+ worldID);
        Socket worldCommunicationSocket = new Socket(Configurations.WORLD_HOST, Configurations.WORLD_PORT);
        in = worldCommunicationSocket.getInputStream();
        out = worldCommunicationSocket.getOutputStream();
        // connect to the world(send AConnect message)
        AConnect.Builder aconnect = AConnect.newBuilder();
        AConnected.Builder aconnected = AConnected.newBuilder();
        aconnect.setIsAmazon(true);
        aconnect.addAllInitwh(warehouses);
        //if not 0 world id,
        if (worldID >= 0) {
            aconnect.setWorldid(worldID);
        }
        //send to world server
        messToStream(aconnect.build(), worldCommunicationSocket.getOutputStream());
        //receive from world server
        messFromStream(aconnected, worldCommunicationSocket.getInputStream());
        //debug
        System.out.println("received world response "+"world id: " + aconnected.getWorldid() + "\nresult: " + aconnected.getResult());

        //remember only move on when connect world success!
        String res = aconnected.getResult();
        return res.equals("connected!");
    }



    /**
     * run front end handling thread(FrontEndCommunicator)
     * only responsible for front end package purchasing
     */
    private void goFrontEnd() {
        //a new thread to handle purchasing request from front end
        frontEndcommunicator = new FrontEndcommunicator(this::purchase);
        frontEndcommunicator.start();
    }


    /**
     *handle front end purchase request by:
     * first get package info from DB
     * then construct package then&keep track of it in map and send request to world
     * @param packageID
     */
    private void purchase(long packageID) {
        threadPool.execute(() -> {
            long currSeq = getSeqNum();
            APurchaseMore.Builder newPackage = new SQL().queryPackage(packageID);
            newPackage.setSeqnum(currSeq);
            APack.Builder builder = APack.newBuilder();
            builder.setWhnum(newPackage.getWhnum());
            builder.addAllThings(newPackage.getThingsList());
            builder.setShipid(packageID);
            builder.setSeqnum(-1);
            //
            Package p = new Package(packageID, newPackage.getWhnum(), builder.build());
            p.setStatus(packState.PROCESSING);
            // store this unfinished package to the map
            idMapPackage.put(packageID, p);
            //send purchase more request to world, front end handler job is done here
            sendToWorld(ACommands.newBuilder().addBuy(newPackage), currSeq);
        });
    }


    //new thread to handle UPS request
    private void goUPS() {
        Thread upsThread = new Thread(() -> {
                if (upsCommunicator != null) {
                    System.out.println("UPS connection request accepted, now goto UPS handler(From now on, every communication with UPS happen inside handler)");
                    threadPool.execute(() -> {
                        try {
                            upsHandler(upsCommunicator.getInputStream(), upsCommunicator.getOutputStream());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
        });
        upsThread.start();
    }

    /**
     * handle all communications with ups side
     * @param inputStream
     * @param outputStream
     */
    private void upsHandler(InputStream inputStream, OutputStream outputStream) {
        while(true){
            int availableBytes = 0;
            try{
                availableBytes = inputStream.available();
            }catch(Exception e){
                e.printStackTrace();
            }
            if(availableBytes != 0){
                UToA.Builder uaRequest = UToA.newBuilder();
                messFromStream(uaRequest, inputStream);
                //no need ack?
                System.out.println("Got an UtoA from UPS, the message is\n"
                + uaRequest.toString());
                //truck has arrived, then change package state and tell world to load if it is packed
                if(uaRequest.hasTruckArrived()) {
                    truck_arrived arr = uaRequest.getTruckArrived();
                    System.out.println("A truck has arrived, truck id is: " + arr.getTruckid());
                    //update package's truck_id in database
                    new SQL().updateTruckID(arr.getShipid() ,arr.getTruckid());
                    picked(arr.getShipid(), arr.getTruckid());
                }   

                //package has been delivered, then remove it from map
                if(uaRequest.hasDelivered()){
                     delivered dlv = uaRequest.getDelivered();
                    System.out.println("A package has been delivered, package id is: " + dlv.getShipid());
                    idMapPackage.get(dlv.getShipid()).setStatus(packState.DELIVERED);
                    idMapPackage.remove(dlv.getShipid());//shipid is the package id
                }
            }
            else{
                
            }  
        }

    }

    /**
     * truck arrived, tell the world to load
     * @param packageID
     * @param truckID
     */
    void picked(long packageID, int truckID) {
        if (!packageUnfinished(packageID)){
            return;
        }
        Package p = idMapPackage.get(packageID);
        p.setTruckID(truckID);
        //tell world to load if package is packed
        if (p.getStatus().equals(packState.PACKED)) {
            load(packageID);
        }
    }


    /**
     * tell the world to load
     * situation1: packed and see truck has already there
     * situation2: truck arrived and see package already packed
     * @param packageID
     */
    private void load(long packageID) {
        if (!packageUnfinished(packageID)){
            return;
        }
        System.out.println("Telling the world to load, package id is "+ packageID);
        Package p = idMapPackage.get(packageID);
        p.setStatus(packState.LOADING);
        threadPool.execute(() -> {
            ACommands.Builder command = ACommands.newBuilder();
            long seq = getSeqNum();
            APutOnTruck.Builder load = APutOnTruck.newBuilder();
            load.setWhnum(p.getWarehouseID());
            load.setTruckid(p.getTruckID());
            load.setShipid(packageID);
            load.setSeqnum(seq);
            command.addLoad(load);
            sendToWorld(command, seq);
        });
    }

    private boolean packageUnfinished(long packageID) {
        if (!idMapPackage.containsKey(packageID)) {
            System.out.println("package id " + packageID + " not in execution status");
            return false;
        }
        return true;
    }

    /**
     * handle all request from world
     */
    private void goWorld() {
        //in and out could be used to communicate with world
        Thread worldThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()){
                AResponses.Builder responses = AResponses.newBuilder();
                // only this thread will access the "in" object, no need to synchronized
                messFromStream(responses, in);
                worldHandler(responses.build());
            }
        });
        worldThread.start();
    }

    private void worldHandler(AResponses worldResponse) {

        ackWorld(worldResponse);
        //package has arrived
        for (APurchaseMore purchaseMore : worldResponse.getArrivedList()){
            System.out.println("A package has arrived id ");
            afterArrived(purchaseMore);
        }
        // package has been packed
        for (APacked p : worldResponse.getReadyList()){
            System.out.println("A package has been packed, id = " + p.getShipid());
            afterPacked(p.getShipid());
        }
        // packed has been loaded
        for (ALoaded l : worldResponse.getLoadedList()){
            System.out.println("A package has been loaded");
            afterLoaded(l.getShipid());
        }
        // if there is error message
        for (AErr err : worldResponse.getErrorList()){
            System.out.println(err.getErr());
        }
        // update package status
        for (APackage aPackage : worldResponse.getPackagestatusList()){
            idMapPackage.get(aPackage.getPackageid()).setStatus(aPackage.getStatus());
        }
        // stop timer upon ack received
        for (long ack : worldResponse.getAcksList()){
            if (requestMapTimer.containsKey(ack)){
                requestMapTimer.get(ack).cancel();
                requestMapTimer.remove(ack);
            }
        }
        // stop world connection
        if (worldResponse.hasFinished()){
            System.out.println("world disconnected");
        }
    }

    /**
     * after the world loaded the package
     * we tell UPS side to deliver our package
     * @param shipid
     */
    private void afterLoaded(long shipid) {
        if (!packageUnfinished(shipid)){
            return;
        }
        idMapPackage.get(shipid).setStatus(packState.LOADED);
        deliver(shipid);
    }

    /**
     * after package was loaded
     * tell UPS to deliver this package
     * @param shipid
     */
    private void deliver(long shipid) {
        if (!packageUnfinished(shipid)){
            return;
        }
        System.out.println("Telling UPS to deliver package id is "+ shipid);
        idMapPackage.get(shipid).setStatus(packState.DELIVERING);
        threadPool.execute(() -> {
            long seqNum = getSeqNum();
            AToU.Builder auMess = AToU.newBuilder();
            //set request for delivery
            auMess.setReadyForDelivery(ready_for_delivery.newBuilder().setShipid(shipid).build());
            sendToUPS(auMess.build(), seqNum);
        });
    }

    /**
     * after package has been packed
     * we tell world to load it if there is a truck there
     * @param shipid
     */
    private void afterPacked(long shipid) {
        //ship id is the package id on UPS side!!!!
        if (!packageUnfinished(shipid)){
            return;
        }
        System.out.println("after package has been packed, set package id to packed");
        Package p = idMapPackage.get(shipid);
        p.setStatus(packState.PACKED);
        //if UPS truck has arrived, load
        //note here that, if not arrived here,
        //when it does, we will check again if packed
        //and load at that time

        if (p.getTruckID() != -1){
            load(shipid);
        }
    }

    /**
     * world told that package has arrived
     * we need then tell UPS to pick and tell world to pack
     * @param purchaseMore
     */
    private void afterArrived(APurchaseMore purchaseMore) {
        synchronized (idMapPackage){
            // loop all package to find the corresponding package
            for (Package p : idMapPackage.values()){
                //if warehouse id and thingslist are all equal, we believe they are the same package
                if(p.getWarehouseID() == purchaseMore.getWhnum() && p.getApack().getThingsList().equals(purchaseMore.getThingsList())) {
                    p.setStatus(packState.PROCESSED);
                    // tell UPS to pick the package
                    upsPick(p.getPackageID());
                    // tell the world to pack the package
                    worldPack(p.getPackageID());
                    break;
                }
            }
        }
    }

    /**
     * after package has arrived, tell the world to pack this package
     * execute with upsPick in the meantime
     * @param packageID
     */
    private void worldPack(long packageID) {
        if (!packageUnfinished(packageID)){
            return;
        }
        System.out.println("Telling the world to pack the package id = " + packageID);
        Package p = idMapPackage.get(packageID);
        //set package status to packing
        p.setStatus(packState.PACKING);
        threadPool.execute(() -> {
            ACommands.Builder command = ACommands.newBuilder();
            long seq = getSeqNum();
            APack pack = idMapPackage.get(packageID).getApack();
            command.addTopack(pack.toBuilder().setSeqnum(seq));
            sendToWorld(command, seq);
        });
    }

    /**
     * package is arrived, request a truck
     * to tell UPS to pick the package
     * @param packageID
     */
    private void upsPick(long packageID) {
        if (!packageUnfinished(packageID)){
            return;
        }
        System.out.println("Requesting a truck from UPS to load package " + packageID);
        Package p = idMapPackage.get(packageID);
        threadPool.execute(() -> {
            long seqNum = getSeqNum();
            AToU.Builder mess = AToU.newBuilder();
            //set request truck
            request_truck.Builder reqT = request_truck.newBuilder();
            reqT.setShipid(packageID);
            reqT.setWhnum(p.getWarehouseID());
            reqT.setLocationX(p.getDesX()); 
            reqT.setLocationY(p.getDesY());
            reqT.setUpsAccountId("");
            reqT.setItemDesc("weird description: this is a very expensive product");
            

            
            //reqT.setUpsAccountId(p.getUpsName());
            mess.setRequest(reqT.build());
            System.out.println("sending message to UPS to request for a truck, the message is: \n" +
            mess.toString());
            sendToUPS(mess.build(), seqNum);
        });
    }

    /**
     * send mess to UPS and wait for ack?
     * @param auMess
     * @param seqNum
     */
    private void sendToUPS(AToU auMess, long seqNum) {
        try {
            
            InputStream reader = upsCommunicator.getInputStream();
            OutputStream writer = upsCommunicator.getOutputStream();
            //send
            messToStream(auMess, writer);
            //receive ack?
            /*
            while (true) {
                UToA.Builder upsMess = UToA.newBuilder();
                messFromStream(upsMess, reader);
                // print out any error message
            }*/
            System.out.println("A message has been sent to UPS, the message is: \n"
            + auMess.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * ack all commands from world
     * @param worldResponse
     */
    private void ackWorld(AResponses worldResponse) {
        List<Long> seqs = new ArrayList<>();
        for (APurchaseMore a : worldResponse.getArrivedList()) {
            seqs.add(a.getSeqnum());
        }
        for (APacked a : worldResponse.getReadyList()) {
            seqs.add(a.getSeqnum());
        }
        for (ALoaded a : worldResponse.getLoadedList()) {
            seqs.add(a.getSeqnum());
        }
        for (AErr a : worldResponse.getErrorList()) {
            seqs.add(a.getSeqnum());
        }
        for (APackage a : worldResponse.getPackagestatusList()) {
            seqs.add(a.getSeqnum());
        }
        if (seqs.size() > 0) {
            ACommands.Builder commands = ACommands.newBuilder();
            for (long seq : seqs) {
                commands.addAcks(seq);
            }
            synchronized (out) {
                messToStream(commands.build(), out);
            }
        }
    }

    /**
     *this function is called after amazon server set up(ie, connected to world and send ack to UPS)
     * It will start three thread, one to handle front end communication one to handle UPS communication and one for communicate with world
     */
    public void go(){
        //start threads in thread pool to achieve high concurrency
        threadPool.prestartAllCoreThreads();

        //front end, UPS and World thread
        goFrontEnd();
        goUPS();
        goWorld();
    }


    /**
     * send Acommands to world, resend if Timeout
     * @param commands
     * @param seqNum
     */
    void sendToWorld(ACommands.Builder commands, long seqNum) {
        commands.setSimspeed(500);
        System.out.println("sending commands to world server: " + commands.toString());
        //resend if timeout
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (out){
                    messToStream(commands.build(), out);
                }
            }
        }, 0, Configurations.TIME_OUT);

        //store request and its timer in a map
        requestMapTimer.put(seqNum, timer);
    }

}
