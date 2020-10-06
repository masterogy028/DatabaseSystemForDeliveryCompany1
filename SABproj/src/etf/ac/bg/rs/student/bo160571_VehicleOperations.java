package etf.ac.bg.rs.student;

import com.sun.istack.internal.NotNull;
import rs.etf.sab.operations.VehicleOperations;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import DB.DB;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class bo160571_VehicleOperations implements VehicleOperations {
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private String query;
    private Connection connection;
    bo160571_VehicleOperations() {
        connection = DB.getInstance().getConnection();
    }
    private boolean isAlreadyCourier(String username){
        bo160571_CourierOperations op = new bo160571_CourierOperations();

        return op.getAllCouriers().contains(username);
    }
    public enum FUEL {
        GASS(1),
        DIESEL(2),
        GASOLINE(3);
        private int v;
        FUEL(int v){
            this.v = v;
        }
        public int getV() {
            return v;
        }
    }


    @Override
    public boolean insertVehicle(String licencePlateNumber, int fuelType, BigDecimal fuelConsumption, BigDecimal capacity) {
        try {
            query = "INSERT INTO vehicle VALUES (?, ?, ?, ?)";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, licencePlateNumber);
            preparedStatement.setInt(2, fuelType);
            preparedStatement.setBigDecimal(3, fuelConsumption);
            preparedStatement.setBigDecimal(4, capacity);

            if (preparedStatement.executeUpdate() < 1) return false;
            else return true;

        }
        catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    @Override
    public int deleteVehicles(String... licencePlateNumbers) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < licencePlateNumbers.length; i++){
            sb.append("'" + licencePlateNumbers[i] + "'");
            if (i != licencePlateNumbers.length - 1)
                sb.append( ",");
        }
        sb.append(")");

        try  {
            query = "DELETE FROM vehicle WHERE licenceV IN " + sb.toString();
            preparedStatement = connection.prepareStatement(query);
            return preparedStatement.executeUpdate();

        } catch (Exception e) {
            System.out.println(e);
            return 0;
        }
    }

    @Override
    public List<String> getAllVehichles() {
        try {
            query = "SELECT licenceV FROM vehicle";
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();
            List<String> reter = new ArrayList<>();
            while (resultSet.next()){
                reter.add(resultSet.getString(1));
            }

            return reter;
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    @Override
    public boolean changeFuelType(String licensePlateNumber, int fuelType) {

        if (!isParked(licensePlateNumber))
            return false;


        query = "UPDATE vehicle SET gassType = ? WHERE licenceV = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)){

            statement.setInt(1, fuelType);
            statement.setString(2, licensePlateNumber);

            statement.executeUpdate();

            return true;
        }
        catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    @Override
    public boolean changeConsumption(String licensePlateNumber, BigDecimal fuelConsumption) {

        if (!isParked(licensePlateNumber))
            return false;


        query = "UPDATE vehicle SET gassCons = ? WHERE licenceV = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)){

            statement.setBigDecimal(1, fuelConsumption);
            statement.setString(2, licensePlateNumber);

            statement.executeUpdate();

            return true;
        }
        catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    @Override
    public boolean changeCapacity(String licensePlateNumber, BigDecimal capacity) {

        if (!isParked(licensePlateNumber))
            return false;


        query = "UPDATE vehicle SET cap = ? WHERE licenceV = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)){

            statement.setBigDecimal(1, capacity);
            statement.setString(2, licensePlateNumber);

            statement.executeUpdate();

            return true;
        }
        catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    @Override
    public boolean parkVehicle(@NotNull String licencePlateNumbers, int idStockroom) {

        query = "INSERT INTO park VALUES(?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)){

            statement.setInt(1, idStockroom);
            statement.setString(2, licencePlateNumbers);

            return statement.executeUpdate() == 1;
        }
        catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    private boolean isParked(String licencePlateNumber){
        try {
            query = "SELECT * FROM park WHERE licenceV = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, licencePlateNumber);
            resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        }
        catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }
}
