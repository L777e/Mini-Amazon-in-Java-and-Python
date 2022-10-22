package edu.duke.ece568.proj;


import proto.WorldAmazon;
import proto.WorldAmazon.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static edu.duke.ece568.proj.Configurations.*;

/**
 * SQL class is responsible for connect and query local database
 */
public class SQL {
    public SQL() {
    }

    /**
     * construct a APurchaseMore object based on packageID
     * @param packageID
     * @return
     */
    public APurchaseMore.Builder queryPackage(long packageID){
        try {
            
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            conn.setAutoCommit(false);
            Statement statement = conn.createStatement();

            //query database
            String queryString = String.format(
                "SELECT product.id, product.description, orderitem.quantity "
                + "FROM %s AS orderitem, %s AS product , %s AS package "
                + "WHERE product.id = orderitem.product_id AND orderitem.order_id = package.order_id "
                + "AND package.id = %d;",
                    TABLE_ORDERITEM, TABLE_PRODUCT, TABLE_PACKAGE, packageID);
            ResultSet result = statement.executeQuery(queryString);

            //construct APurchaseMore result
            APurchaseMore.Builder apmore = APurchaseMore.newBuilder();
            apmore.setWhnum(queryWHNum(packageID));
            while (result.next()){//increment search
                //add each item to apmore object
                AProduct.Builder aproduct = AProduct.newBuilder();
                aproduct.setId(result.getInt("id"));
                System.out.println(result.getInt("id"));
                aproduct.setCount(result.getInt("quantity"));
                System.out.println(result.getInt("quantity"));
                aproduct.setDescription(result.getString("description"));
                System.out.println(result.getString("description"));
                apmore.addThings(aproduct);
            }
            //close database connection and return apmore object
            statement.close();
            conn.close();
            return apmore;

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * return the warehouse id of the given package(id)
     * @param packageID
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public int queryWHNum(long packageID) throws SQLException, ClassNotFoundException {
        int res = 0;
        //get database connection
        Class.forName("org.postgresql.Driver");
        Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        conn.setAutoCommit(false);

        Statement statement = conn.createStatement();
        String queryString = String.format("SELECT warehouse FROM %s WHERE id = %d;", TABLE_PACKAGE, packageID);

        ResultSet result = statement.executeQuery(queryString);
        if (result.next()){//increment search
            res = result.getInt("warehouse");
        }

        //close db connection
        statement.close();
        conn.close();
        return res;
    }

    /**
     * update package status
     * package default status is processing
     * @param packageID
     * @param status
     * @return
     */
    public boolean updateStatus(long packageID, String status) {
        try {
            //connection to database
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            conn.setAutoCommit(false);

            Statement statement = conn.createStatement();
            String updateString = String.format("UPDATE %s SET status='%s' WHERE id=%d;", TABLE_PACKAGE, status, packageID);
            statement.executeUpdate(updateString);
            conn.commit();

            //close db connection
            statement.close();
            conn.close();
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }




    /**
     * update package truck_id
     * package default truck_id is null
     * @param packageID
     * @param status
     * @return
     */
    public boolean updateTruckID(long packageID, int truck_id) {
        try {
            //connection to database
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            conn.setAutoCommit(false);

            Statement statement = conn.createStatement();
            String updateString = String.format("UPDATE %s SET truck_id='%d' WHERE id=%d;", TABLE_PACKAGE, truck_id, packageID);
            statement.executeUpdate(updateString);
            conn.commit();

            //close db connection
            statement.close();
            conn.close();
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * query for the destination x-coordinate for given package num
     * @param packageID
     * @return
     */
    public int queryPackageDestX(long packageID) {
        int destX = 0;
        try {
            //connection to database
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            conn.setAutoCommit(false);

            //
            Statement statement = conn.createStatement();
            String queryString = String.format("SELECT dest_x FROM %s WHERE id = %d;", TABLE_PACKAGE, packageID);
            ResultSet result = statement.executeQuery(queryString);


            if (result.next()){
                destX = result.getInt("dest_x");
            }

            //close db connection
            statement.close();
            conn.close();
        }catch (SQLException | ClassNotFoundException e){
            e.printStackTrace();
        }
        return destX;
    }

    /**
     * query for the destination y-coordinate for given package num
     * @param packageID
     * @return
     */
    public int queryPackageDestY(long packageID) {
        int destY = 0;
        try {
            //connection to database
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            conn.setAutoCommit(false);

            //
            Statement statement = conn.createStatement();
            String queryString = String.format("SELECT dest_y FROM %s WHERE id = %d;", TABLE_PACKAGE, packageID);
            ResultSet result = statement.executeQuery(queryString);


            if (result.next()){
                destY = result.getInt("dest_y");
            }

            //close db connection
            statement.close();
            conn.close();
        }catch (SQLException | ClassNotFoundException e){
            e.printStackTrace();
        }
        return destY;
    }


    /**
     * get the package's UPS ID (String)
     * @param packageID
     * @return
     */
    public String queryUPSID(long packageID) {
        String res = "";
        try {
            //connection to database
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            conn.setAutoCommit(false);

            Statement statement = conn.createStatement();
            String queryString = String.format("SELECT \"UPS_id\" FROM %s WHERE id = %d;", TABLE_PACKAGE, packageID);
            ResultSet result = statement.executeQuery(queryString);


            if (result.next()){
                res = result.getString("UPS_id");
            }

            //close db connection
            statement.close();
            conn.close();
            return res;
        }catch (SQLException | ClassNotFoundException e){
            e.printStackTrace();
        }
        return "";
    }

    /**
     * get a list of all warehouses
     * @return
     */
    public List<AInitWarehouse> queryWHs() {
        List<AInitWarehouse> res = new ArrayList<>();
        try {
            //connection to db
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            conn.setAutoCommit(false);

            //query warehouses
            Statement statement = conn.createStatement();
            String queryString = String.format("SELECT * FROM %s;", TABLE_WAREHOUSE);
            ResultSet result = statement.executeQuery(queryString);

            while (result.next()){
                //for each warehouse store it as an AInitWarehouse in res
                AInitWarehouse.Builder currWarehouseBuilder = AInitWarehouse.newBuilder();
                currWarehouseBuilder.setId(result.getInt("id"));
                currWarehouseBuilder.setX(result.getInt("x"));
                currWarehouseBuilder.setY(result.getInt("y"));
                res.add(currWarehouseBuilder.build());
            }

            //close db connection
            statement.close();
            conn.close();
            return res;
        }catch (SQLException | ClassNotFoundException e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

}
