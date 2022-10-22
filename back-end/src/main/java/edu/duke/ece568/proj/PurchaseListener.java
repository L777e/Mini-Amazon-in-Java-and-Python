package edu.duke.ece568.proj;

/**
 * we require front end communication thread to implement this interface
 * so that it knows what method to call after received a purchase request from front end(package id)
 */
public interface PurchaseListener {
    void doPurchase(long packageID);
}
