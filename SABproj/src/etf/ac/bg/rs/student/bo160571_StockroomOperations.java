package etf.ac.bg.rs.student;

import rs.etf.sab.operations.StockroomOperations;
import DB.DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class bo160571_StockroomOperations implements StockroomOperations {
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private String query;
    private Connection connection;
    bo160571_StockroomOperations() {
        connection = DB.getInstance().getConnection();
    }
    private boolean isAlreadyCourier(String username){
        bo160571_CourierOperations op = new bo160571_CourierOperations();

        return op.getAllCouriers().contains(username);
    }
    public int getStockroomFromCity(int idCity){

        try {
            query = "SELECT sr.idStock FROM stock sr INNER JOIN address a ON a.idDistrict = sr.idDistrict" +
                    " WHERE a.idCity = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, idCity);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            else return -1;
        }
        catch (Exception e) {
            System.out.println(e);
            return -1;
        }
    }
    @Override
    public int insertStockroom(int address) {
        try {
            query = "select idCity from address where idDistrict=?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, address);
            ResultSet rs  =preparedStatement.executeQuery();
            if(rs.next()){
                if(stockroomFromCity(rs.getInt(1))!=-1)
                    return -1;
            }
            query = "INSERT INTO stock VALUES (?)";
            preparedStatement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, address);
            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            resultSet.next();
            return resultSet.getInt(1);
        }
        catch (Exception e) {
            System.out.println(e);
            return -1;
        }
    }

    @Override
    public boolean deleteStockroom(int idStockroom) {
        try  {
            query = "DELETE FROM stock WHERE idStock = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, idStockroom);
            if (preparedStatement.executeUpdate() == 0) return false;
            else return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    public int stockroomFromCity(int idCity){
        try {
            query = "SELECT sr.idStock FROM stock sr INNER JOIN address a ON a.idDistrict = sr.idDistrict WHERE a.idCity = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, idCity);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            else return -1;
        }
        catch (Exception e) {
            System.out.println(e);
            return -1;
        }
    }

    @Override
    public int deleteStockroomFromCity(int idCity) {
        int idStockroom = stockroomFromCity(idCity);
        deleteStockroom(idStockroom);
        return idStockroom;
    }

    @Override
    public List<Integer> getAllStockrooms() {
        try {
            query = "SELECT idStock FROM stock";
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();
            List<Integer> reter = new ArrayList<>();
            while (resultSet.next()){
                reter.add(resultSet.getInt(1));
            }
            return reter;
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }
}
