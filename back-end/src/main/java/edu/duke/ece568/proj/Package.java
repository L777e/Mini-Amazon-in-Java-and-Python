package edu.duke.ece568.proj;


import proto.WorldAmazon.*;

/**
 * package that lives from purchase to delivered, stored in Amazon server's package map
 * created on client(front end)'s purchase request, deleted on delivered9+*--
 */
public class Package {
    private String status;
    private long packageID;
    private int warehouseID;
    private int truckID;
    //package destinations
    private int desX;
    private int desY;
    private APack apack;
    private String upsName;

    public Package(long packageID, int warehouseID, APack apack) {
        this.packageID = packageID;
        this.warehouseID = warehouseID;
        this.apack = apack;
        this.truckID = -1;
        this.desX = new SQL().queryPackageDestX(packageID);
        this.desY = new SQL().queryPackageDestY(packageID);
        this.upsName = new SQL().queryUPSID(packageID);
    }

    public String getStatus() {
        return status;
    }

    public long getPackageID() {
        return packageID;
    }

    public int getWarehouseID() {
        return warehouseID;
    }

    public int getTruckID() {
        return truckID;
    }

    public int getDesX() {
        return desX;
    }

    public int getDesY() {
        return desY;
    }

    public APack getApack() {
        return apack;
    }

    public String getUpsName() {
        return upsName;
    }

    public void setStatus(String status) {
        this.status = status;
        new SQL().updateStatus(this.packageID, this.status);
    }

    public void setPackageID(long packageID) {
        this.packageID = packageID;
    }

    public void setWarehouseID(int warehouseID) {
        this.warehouseID = warehouseID;
    }

    public void setTruckID(int truckID) {
        this.truckID = truckID;
    }

    public void setDesX(int desX) {
        this.desX = desX;
    }

    public void setDesY(int desY) {
        this.desY = desY;
    }

    public void setApack(APack apack) {
        this.apack = apack;
    }

    public void setUpsName(String upsName) {
        this.upsName = upsName;
    }
}
