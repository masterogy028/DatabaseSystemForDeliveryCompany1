package etf.ac.bg.rs.student;

import rs.etf.sab.operations.CourierOperations;
import DB.DB;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class bo160571_CourierOperations implements CourierOperations {
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private String query;
    private Connection connection;
    bo160571_CourierOperations() {
        connection = DB.getInstance().getConnection();
    }
    @Override
    public boolean insertCourier(String courierUserName, String driversLicence) {

        query = "INSERT INTO courir (username, status, profit, deliveriesCount, licence) VALUES (?, ?, ?, ?, ?)";
        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, courierUserName);
            preparedStatement.setInt(2, 0);
            preparedStatement.setInt(3, 0);
            preparedStatement.setInt(4, 0);
            preparedStatement.setString(5, driversLicence);

            if (preparedStatement.executeUpdate() < 1) return false;
            else return true;

        }
        catch (Exception ex){
            System.out.println(ex.toString());
            return false;
        }
    }

    @Override
    public boolean deleteCourier(String courierUserName) {
        query = "DELETE FROM courir WHERE username = ?";
        try  {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, courierUserName);
            boolean b = preparedStatement.executeUpdate() != 0;
            return b;
        } catch (Exception ex) {
            System.out.println(ex.toString());
            return false;
        }
    }

    @Override
    public List<String> getCouriersWithStatus(int statusOfCourier) {
        query = "SELECT username FROM courir WHERE status = ?";
        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, statusOfCourier);
            resultSet = preparedStatement.executeQuery();
            List<String> reter = new ArrayList<>();
            while (resultSet.next()){
                reter.add(resultSet.getString(1));
            }
            return reter;
        }
        catch (Exception ex){
            System.out.println(ex.toString());
            return null;
        }
    }

    @Override
    public List<String> getAllCouriers() {
        query = "SELECT username FROM courir ORDER BY profit DESC";

        try {
            preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<String> reter = new ArrayList<>();

            while (resultSet.next()){
                reter.add(resultSet.getString(1));
            }

            return reter;
        }
        catch (Exception ex){
            System.out.println(ex.toString());
            return null;
        }
    }

    @Override
    public BigDecimal getAverageCourierProfit(int numberOfDeliveries) {

        query = "SELECT AVG(profit) FROM courir";
        if (numberOfDeliveries != -1)
            query += " WHERE deliveriesCount = ?";

        try {
            preparedStatement = connection.prepareStatement(query);
            if (numberOfDeliveries != -1)
                preparedStatement.setInt(1, numberOfDeliveries);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                return BigDecimal.valueOf(resultSet.getDouble(1)).setScale(3, RoundingMode.FLOOR);
            else
                return null;

        } catch (Exception ex) {
            System.out.println(ex.toString());
            return null;
        }
    }
}
