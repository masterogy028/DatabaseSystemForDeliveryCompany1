package util;

import etf.ac.bg.rs.student.bo160571_DriveOperation;
import etf.ac.bg.rs.student.bo160571_PackageOperations;
import etf.ac.bg.rs.student.models.StopsModel;
import javafx.util.Pair;
import DB.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class UtilOperations {
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private String query;
    private Connection connection;

    public UtilOperations() {
        connection = DB.getInstance().getConnection();
    }
    private bo160571_DriveOperation bo160571DriveOperation = new bo160571_DriveOperation();
    public BigDecimal getMaxWeight(String vehicleLicence){

        try (PreparedStatement statement = connection.prepareStatement("SELECT cap FROM vehicle WHERE licenceV = ?")){
            statement.setString(1, vehicleLicence);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next())
                return resultSet.getBigDecimal(1);
            else
                return BigDecimal.valueOf(-1);
        } catch (SQLException e) {
            e.printStackTrace();
            return BigDecimal.valueOf(-1);
        }
    }

    public Integer getIdDistrictCurrentForPackage(int idPackage){
        try {
            query = "SELECT toff.idDistrict FROM transport_offer toff WHERE toff.idPackage = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, idPackage);

           resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                return resultSet.getInt(1);
            else
                return -1;
        }
        catch(Exception e){
            e.toString();
            return -1;
        }
    }

    public Integer getIdDistrictFromPackage(int idPackage){
        try {
            query = "SELECT p.[from] FROM [package] p WHERE p.idPackage = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, idPackage);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                return resultSet.getInt(1);
            else {
                return -1;
            }

        }
        catch(Exception e){
            e.toString();
            return -1;
        }
    }

    public Pair<Integer, Integer> getLocationFromPackage(int idPackage){



        try {
            query = "SELECT a.xCoordination, a.yCoordination FROM address a INNER JOIN transport_offer toff ON toff.idDistrict = a.idDistrict" +
                    " WHERE toff.idPackage = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, idPackage);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                return new Pair(resultSet.getInt(1), resultSet.getInt(2));
            else
                return null;
        }
        catch(Exception e){
            e.toString();
            return null;
        }
    }

    public Pair<Integer, Integer> getFinalLocationForPackage(int idPackage){



        try {
            query = "SELECT a.xCoordination, a.yCoordination FROM address a INNER JOIN package p ON p.[to] = a.idDistrict" +
                    " WHERE p.idPackage = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, idPackage);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                return new Pair(resultSet.getInt(1), resultSet.getInt(2));
            else
                return null;
        }
        catch(Exception e){
            e.toString();
            return null;
        }
    }

    public Integer getAddressToForPackage(int idPackage){
        try {
            query = "SELECT p.[to] FROM package p WHERE p.idPackage = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, idPackage);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                return resultSet.getInt(1);
            else
                return null;
        }
        catch(Exception e){
            e.toString();
            return null;
        }
    }

    public Pair<Integer, Integer> getLocationFromAddress(int idDistrict){


        try {
            query = "SELECT a.xCoordination, a.yCoordination FROM address a WHERE a.idDistrict = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, idDistrict);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                return new Pair(resultSet.getInt(1), resultSet.getInt(2));
            else
                return null;
        }
        catch(Exception e){
            e.toString();
            return null;
        }
    }

    public String getCouriersVehicle(String username){
        try {
            query = "SELECT licenceV FROM courir WHERE username = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                return resultSet.getString(1);
            }
            else
                return null;
        } catch (SQLException e) {
            e.toString();
            return null;
        }
    }

    public Integer getIdDistrictForStockroom(int idStockroom){

        try {
            query = "SELECT s.idDistrict FROM stock s WHERE s.idStock = ?";
            preparedStatement = connection.prepareStatement(query);

            preparedStatement.setInt(1, idStockroom);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                return resultSet.getInt(1);
            }
            else
                return -1;
        } catch (SQLException e) {
            e.toString();
            return -1;
        }
    }

    public List<Integer> getPackagesFromStockroom(int idStockroom){

        try {
            query = "SELECT toff.idPackage FROM transport_offer toff INNER JOIN stock s ON s.idDistrict = toff.idDistrict" +
                    " WHERE s.idStock = ? AND toff.status = ? ORDER BY toff.timeAccept ASC";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, idStockroom);
            preparedStatement.setInt(2, bo160571_PackageOperations.PackageStatus.DOWNLOADED.getNumValue());

            resultSet = preparedStatement.executeQuery();
            List<Integer> reter = new LinkedList<>();
            while (resultSet.next()){
                if (!isPackagePlanned(resultSet.getInt(1)))
                    reter.add(resultSet.getInt(1));
            }
            return reter;
        }
        catch(Exception e){
            e.toString();
            return null;
        }
    }



    private boolean isPackagePlanned(int idPackage){
        for(String key: bo160571DriveOperation.getSTOPS().keySet()) {
            if(bo160571DriveOperation.getSTOPS().get(key).contains(idPackage))return true;
        }
        return false;
    }

    public Integer getCityFromLocation(int xCord, int yCord){
        try {
            query = "SELECT a.idCity FROM address a WHERE a.xCoordination = ? AND a.yCoordination = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, xCord);
            preparedStatement.setInt(2, yCord);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()){
                return resultSet.getInt(1);
            }
            else
                return -1;
        } catch (SQLException e) {
            e.toString();
            return -1;
        }
    }

    public int getIdDistrictForLocation(int xCord, int yCord){



        try {
            query = "SELECT a.idDistrict FROM address a WHERE a.xCoordination = ? AND a.yCoordination = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, xCord);
            preparedStatement.setInt(2, yCord);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()){
                return resultSet.getInt(1);
            }
            else
                return -1;
        } catch (SQLException e) {
            e.toString();
            return -1;
        }
    }

    public BigDecimal getPackageWeight(int idPackage){

        try {
            query = "SELECT weight FROM package WHERE idPackage = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, idPackage);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()){
                return resultSet.getBigDecimal(1);
            }
            else
                return BigDecimal.valueOf(-1);
        } catch (SQLException e) {
            e.toString();
            return BigDecimal.valueOf(-1);
        }
    }

    public Integer getCityForStockroom(int idStockroom){



        try {
            query = "SELECT a.idCity FROM address a INNER JOIN stock s ON s.idDistrict = a.idDistrict" +
                    " WHERE s.idStock = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, idStockroom);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                return resultSet.getInt(1);
            }
            else
                return -1;
        } catch (SQLException e) {
            e.toString();
            return -1;
        }
    }

    public List<Integer> getAllAcceptedPackagesFromCityNotPlanned(int cityId) {


        try {
            query = "SELECT p.idPackage FROM package p INNER JOIN transport_offer toff ON p.idPackage = toff.idPackage" +
                    " INNER JOIN address a ON p.[from] = a.idDistrict" +
                    " WHERE toff.status = ? AND a.idCity = ?" +
                    " ORDER BY toff.timeAccept ASC";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, bo160571_PackageOperations.PackageStatus.ACCEPTED.getNumValue());
            preparedStatement.setInt(2, cityId);
            resultSet = preparedStatement.executeQuery();
            List<Integer> reter = new ArrayList<>();
            while (resultSet.next()){
                if (!isPackagePlanned(resultSet.getInt(1)))
                    reter.add(resultSet.getInt(1));
            }
            return reter;
        }
        catch (SQLException e){
            e.toString();
            return null;
        }
    }


    public int getFuel(String vehicleLicence) {
        try {
            query = "SELECT gassType FROM vehicle WHERE licenceV = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, vehicleLicence);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                return resultSet.getInt(1);
            }
            else
                return -1;
        } catch (SQLException e) {
            e.toString();
            return -1;
        }
    }
    public void updateProfitAndNumOfDeliveries(String username, double profit, int numOfDeliveries) {
        try {
            query = "UPDATE courir SET profit += ?, deliveriesCount += ? WHERE username = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setBigDecimal(1, BigDecimal.valueOf(profit));
            preparedStatement.setInt(2, numOfDeliveries);
            preparedStatement.setString(3, username);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

    }
    public void changeDistrictInPackage(int idPackage, int idDistrict) {
        try {
            query = "UPDATE transport_offer SET idDistrict = ? WHERE idPackage = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, idDistrict);
            preparedStatement.setInt(2, idPackage);
            preparedStatement.executeUpdate();

        }
        catch (Exception ex){
            ex.toString();
            return;
        }

    }
    public int stopType(String courierUsername, int order) {
        try {
            for (StopsModel sm: new bo160571_DriveOperation().getSTOPS().get(courierUsername)
            ) {
                if(sm.getOrder() == order) return sm.getPlanType();
            }
            return -1;
        }catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

    }
    public BigDecimal getFuelConsumptionForVehicle(String vehicleLicence) {
        try {
            query = "SELECT gassCons FROM vehicle WHERE licenceV = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, vehicleLicence);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getBigDecimal(1);
            } else
                return BigDecimal.ZERO;
        } catch (SQLException e) {
            e.toString();
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getPriceForPackage(int idPackage) {
        try {
            query = "SELECT price FROM transport_offer WHERE idPackage = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, idPackage);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                return resultSet.getBigDecimal(1);
            }
            else
                return BigDecimal.ZERO;
        } catch (SQLException e) {
            return BigDecimal.ZERO;
        }


    }
}

 /*public Pair<Integer, Integer> getCurrentLocationForStockroom(int idStockroom){
        Connection connection = DB.getInstance().getConnection();

        String sqlQuery = "SELECT a.xCoordination, a.yCoordination FROM address a INNER JOIN stock s ON s.idDistrict = a.idDistrict" +
                " WHERE s.idStockroom = ?";

        try (PreparedStatement statement = connection.prepareStatement(sqlQuery)){

            statement.setInt(1, idStockroom);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next())
                return new Pair(resultSet.getInt(1), resultSet.getInt(2));
            else
                return null;
        }
        catch(Exception e){
            return null;
        }
    }*/