package etf.ac.bg.rs.student;

import DB.DB;
import rs.etf.sab.operations.AddressOperations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class bo160571_AddressOperations implements AddressOperations {
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private String query;
    private Connection connection;
    bo160571_AddressOperations() {
        connection = DB.getInstance().getConnection();
    }
    @Override
    public int insertAddress(String street, int number, int cityId, int xCoordination, int yCoordination) {
        query = "insert into address VALUES (?, ?, ?, ?, ?)";
        try {
            preparedStatement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, street);
            preparedStatement.setInt(2, number);
            preparedStatement.setInt(3, xCoordination);
            preparedStatement.setInt(4, yCoordination);
            preparedStatement.setInt(5, cityId);
            preparedStatement.executeUpdate();

            resultSet = preparedStatement.getGeneratedKeys();
            resultSet.next();
            return resultSet.getInt(1);
        }
        catch (Exception ex){
            System.out.println(ex.toString());
            return -1;
        }finally {

        }
    }

    @Override
    public int deleteAddresses(String name, int number) {

        query = "DELETE FROM address WHERE street = ? AND number = ?";

        try  {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, number);

            return preparedStatement.executeUpdate();
        } catch (Exception ex) {
            System.out.println(ex.toString());
            return 0;
        }finally {

        }
    }

    @Override
    public boolean deleteAdress(int idDistrict) {

        query = "DELETE FROM address WHERE idDistrict = ?";

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, idDistrict);
            if (preparedStatement.executeUpdate() == 0) return false;
            else return true;

        } catch (Exception ex) {
            System.out.println(ex.toString());
            return false;
        }finally {

        }
    }

    @Override
    public int deleteAllAddressesFromCity(int idCity) {
        query = "DELETE FROM address WHERE idCity = ?";
        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, idCity);
            return preparedStatement.executeUpdate();
        } catch (Exception ex) {
            System.out.println(ex.toString());
            return 0;
        }finally {

        }
    }

    @Override
    public List<Integer> getAllAddressesFromCity(int idCity) {

        String cityExistsSqlQuery = "SELECT * FROM city WHERE idCity = ?";
        query = "SELECT idDistrict FROM address WHERE idCity = ?";

        try {
            preparedStatement = connection.prepareStatement(query);
            PreparedStatement cityExistsStatement = connection.prepareStatement(cityExistsSqlQuery);
            // checking if city exists
            cityExistsStatement.setInt(1, idCity);
            ResultSet cityExistsResultSet = cityExistsStatement.executeQuery();

            boolean cityExists = cityExistsResultSet.next();

            if (cityExists){
                preparedStatement.setInt(1, idCity);
                resultSet = preparedStatement.executeQuery();
                List<Integer> retur = new ArrayList<>();
                while (resultSet.next()){
                    retur.add(resultSet.getInt(1));
                }

                return retur;
            }
            else {
                return null;
            }
        }
        catch (Exception ex){
            System.out.println(ex.toString());
            return null;
        }finally {

        }
    }

    @Override
    public List<Integer> getAllAddresses() {

        query = "SELECT idDistrict FROM address";

        try {
            preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Integer> ret = new ArrayList<>();
            while (resultSet.next()){
                ret.add(resultSet.getInt(1));
            }
            return ret;
        }
        catch (Exception ex){
            System.out.println(ex.toString());
            return null;
        }finally {

        }
    }
}
