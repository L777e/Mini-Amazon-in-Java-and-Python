package edu.duke.ece568.proj;

public class Configurations {
   //world server info
    public static final String WORLD_HOST = "vcm-19393.vm.duke.edu";
    public static final int WORLD_PORT = 23456;

    //ups server info(to be changed)
    public static final String UPS_HOST = "vcm-18225.vm.duke.edu";

    //port used to connect to ups server
    public static final int UPS_SERVER_PORT = 22222;

    //request must be sent again if reached timeout
    public static final int TIME_OUT = 3000;

    //debug? mock UPS?

    //Database configurations
    public static final String TABLE_ITEM = "amazon_product";
    public static final String TABLE_ORDER = "amazon_order";
    public static final String TABLE_PACKAGE = "amazon_package";
    public static final String TABLE_WAREHOUSE = "amazon_warehouse";
    public static final String TABLE_ORDERITEM = "amazon_orderitem";
    public static final String TABLE_PRODUCT = "amazon_product";

    // database configuration
    public static final String dbUrl = "jdbc:postgresql://db:5432/amazon";
    public static final String dbUser = "postgres";
    public static final String dbPassword = "passw0rd";


}
