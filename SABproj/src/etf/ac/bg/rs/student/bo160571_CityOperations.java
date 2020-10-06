package etf.ac.bg.rs.student;

import rs.etf.sab.operations.CityOperations;
import DB.DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class bo160571_CityOperations implements CityOperations {
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private String query;
    private Connection connection;
    bo160571_CityOperations() {
        connection = DB.getInstance().getConnection();
    }
    @Override
    public int insertCity(String name, String postalCode) {
        query = "INSERT INTO city VALUES (?, ?)";

        try {
            preparedStatement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, postalCode);
            preparedStatement.executeUpdate();

            resultSet = preparedStatement.getGeneratedKeys();
            resultSet.next();
            return resultSet.getInt(1);
        }
        catch (Exception ex){
            System.out.println(ex.toString());
            return -1;
        }
    }

    @Override
    public int deleteCity(String... names) {

        //String arr = "(";
        StringBuilder stringBuilder = new StringBuilder("(");
        for (int i = 0; i < names.length; i++){
            stringBuilder.append("'" + names[i] + "'");
            if (i != names.length - 1)
                stringBuilder.append(",");
        }
        stringBuilder.append(")");
        query = "DELETE FROM city WHERE [name] IN " + stringBuilder.toString();
        try {
            preparedStatement = connection.prepareStatement(query);
            int ret = preparedStatement.executeUpdate();
            return ret;
        } catch (Exception ex) {
            System.out.println(ex.toString());
            return 0;
        }
    }

    @Override
    public boolean deleteCity(int idCity) {
        query = "DELETE FROM city WHERE idCity = ?";
        try  {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, idCity);
            if (preparedStatement.executeUpdate() == 0) return false;
            else return true;
        } catch (Exception ex) {
            System.out.println(ex.toString());
            return false;
        }
    }

    @Override
    public List<Integer> getAllCities() {
        query = "SELECT idCity FROM city";
        try {
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();
            List<Integer> reter = new ArrayList<>();
            while (resultSet.next()){
                reter.add(resultSet.getInt(1));
            }
            return reter;
        }
        catch (Exception ex){
            System.out.println(ex.toString());
            return null;
        }
    }
}
