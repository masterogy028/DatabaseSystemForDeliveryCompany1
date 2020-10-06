package etf.ac.bg.rs.student;

import rs.etf.sab.operations.CourierRequestOperation;
import DB.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class bo160571_CourierRequestOperation implements CourierRequestOperation {
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private String query;
    private Connection connection;
    private static HashMap<String, String> requestMap = new HashMap<>();
    bo160571_CourierRequestOperation() {
        connection = DB.getInstance().getConnection();
    }
    private boolean isAlreadyCourier(String username){
        bo160571_CourierOperations op = new bo160571_CourierOperations();

        return op.getAllCouriers().contains(username);
    }

    /*@Override
    public boolean insertCourierRequest(String userName, String driverLicenceNumber) {

        if (isAlreadyCourier(userName)) {
            return false;
        }
        Connection connection = DB.getInstance().getConnection();

        String sqlQuery = "INSERT INTO courier_requests (username, driversLicence) VALUES(?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sqlQuery)){

            requestList.add(new Pair<>(userName, driverLicenceNumber));

            statement.setString(1, userName);
            statement.setString(2, driverLicenceNumber);

            return statement.executeUpdate() == 1;
        }
        catch (SQLException ex){

            return false;
        }
    }*/
    @Override
    public boolean insertCourierRequest(String userName, String driverLicenceNumber) {
        bo160571_CourierOperations op = new bo160571_CourierOperations();
        bo160571_UserOperations ou = new bo160571_UserOperations();
        if (op.getAllCouriers().contains(userName))
            return false;
        else if(!ou.getAllUsers().contains(userName))
            return false;
        if(requestMap.containsValue(driverLicenceNumber)) return false;
        if(requestMap.containsKey(userName)) return false;
            requestMap.put(userName, driverLicenceNumber);
            return true;
    }

    /*@Override
    public boolean deleteCourierRequest(String userName) {
        Connection connection = DB.getInstance().getConnection();

        String sqlQuery = "DELETE FROM courier_requests WHERE username = ?";

        try (PreparedStatement statement = connection.prepareStatement(sqlQuery);) {

            statement.setString(1, userName);

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {

            return false;
        }
    }*/
    @Override
    public boolean deleteCourierRequest(String userName) {

        if(requestMap.containsKey(userName)) {
            requestMap.remove(userName);
            return true;
        }
        return false;
    }

    /*@Override
    public boolean changeDriverLicenceNumberInCourierRequest(String userName, String driversLicenceNumber) {
        Connection connection = DB.getInstance().getConnection();

        String sqlQuery = "UPDATE courier_requests SET driversLicence = ? WHERE username = ?";

        try (PreparedStatement statement = connection.prepareStatement(sqlQuery)){

            statement.setString(1, driversLicenceNumber);
            statement.setString(2, userName);


            return statement.executeUpdate() == 1;
        }
        catch (SQLException ex){

            return false;
        }
    }*/
    @Override
    public boolean changeDriverLicenceNumberInCourierRequest(String userName, String driversLicenceNumber) {

        if(requestMap.containsKey(userName)) {
            requestMap.put(userName, driversLicenceNumber);
            return true;
        }
        return false;

    }

    @Override
    public List<String> getAllCourierRequests() {
        
        List<String> ret = new LinkedList<>();

        Iterator it = requestMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            //System.out.println(pair.getKey() + " = " + pair.getValue());
            ret.add((String) pair.getKey());
            //it.remove();
        }
        return ret;
    }
    /*@Override
    public List<String> getAllCourierRequests() {
        Connection connection = DB.getInstance().getConnection();

        String sqlQuery = "SELECT username FROM courier_requests";

        try (PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            ResultSet resultSet = statement.executeQuery();

            List<String> ret = new LinkedList<>();

            while (resultSet.next()) {
                ret.add(resultSet.getString(1));
            }

            return ret;
        } catch (SQLException e) {

            return null;
        }
    }*/

    /*@Override
    public boolean grantRequest(String username) {
        Connection connection = DB.getInstance().getConnection();

        String sqlQuery = "SELECT driversLicence FROM courier_requests WHERE username = ?";

        try (PreparedStatement statement = connection.prepareStatement(sqlQuery)){

            statement.setString(1, username);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()){

                String driversLicence = resultSet.getString(1);

                MyCourierOperations op = new MyCourierOperations();

                op.insertCourier(username, driversLicence);

                deleteCourierRequest(username);

                return true;
            }
            else {
                return false;
            }

        }
        catch (SQLException ex){

            return false;
        }
    }*/
    @Override
    public boolean grantRequest(String username) {

                if(requestMap.containsKey(username)) {
                    bo160571_CourierOperations op = new bo160571_CourierOperations();
                    op.insertCourier(username, requestMap.get(username));
                    requestMap.remove(username);
                    return true;
                }

                return false;
    }

    public void deleteCourierRequests() {
        requestMap = new HashMap<>();
    }
}
