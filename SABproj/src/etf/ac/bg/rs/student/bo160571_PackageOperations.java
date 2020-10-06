package etf.ac.bg.rs.student;

import rs.etf.sab.operations.PackageOperations;
import DB.DB;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class bo160571_PackageOperations implements PackageOperations {
    private static  int I = 0;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private String query;
    private Connection connection;
    bo160571_PackageOperations() {
        connection = DB.getInstance().getConnection();
    }
    private boolean isAlreadyCourier(String username){
        bo160571_CourierOperations op = new bo160571_CourierOperations();

        return op.getAllCouriers().contains(username);
    }
    public enum PackageStatus{
        CREATED(0),
        ACCEPTED(1),
        DOWNLOADED(2),
        DELIVERED(3),
        REJECTED(4);


        private int numValue;

        PackageStatus(int numValue){
            this.numValue = numValue;
        }

        public int getNumValue() {
            return numValue;
        }
    }

    public enum PackageType {

        SMALL(0),
        STANDARD(1),
        NOT_STANDARD(2),
        FRAGILE(3);

        private int numValue;

        PackageType(int numValue){
            this.numValue = numValue;
        }

        public int getNumValue() {
            return numValue;
        }
    }

    @Override
    public int insertPackage(int addressFrom, int addressTo, String userName, int packageType, BigDecimal weight) {

        query = "INSERT INTO package (type, weight, [from], [to]  , username) VALUES (?, ?, ?, ?, ?)";

        try {
            preparedStatement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, packageType);
            preparedStatement.setBigDecimal(2, weight);
            preparedStatement.setInt(3, addressFrom);
            preparedStatement.setInt(4, addressTo);
            preparedStatement.setString(5, userName);

            preparedStatement.executeUpdate();
            resultSet = preparedStatement.getGeneratedKeys();
            resultSet.next();
            return resultSet.getInt(1);
        }
        catch (Exception e) {
            System.out.println(e);
            return -1;
        }
    }

    private int packageStatus(int idPackage){

        query = "SELECT status FROM transport_offer WHERE idPackage = ?";

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, idPackage);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()){
                return resultSet.getInt(1);
            }
            else
                return -1;
        }
        catch (Exception e) {
            System.out.println(e);
            return -1;
        }
    }

    @Override
    public boolean acceptAnOffer(int packageId) {

        if (packageStatus(packageId) != PackageStatus.CREATED.getNumValue())
            return false;


        query = "UPDATE transport_offer SET status = ?, timeAccept = ? WHERE idPackage = ?";

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, PackageStatus.ACCEPTED.getNumValue());
            preparedStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            preparedStatement.setInt(3, packageId);

            return preparedStatement.executeUpdate() == 1;
        }
        catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    @Override
    public boolean rejectAnOffer(int packageId) {

        if (packageStatus(packageId) != PackageStatus.CREATED.getNumValue())
            return false;

        Connection connection = DB.getInstance().getConnection();

        query = "UPDATE transport_offer SET status = ? AND timeAccept = ? WHERE idPackage = ?";

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, PackageStatus.REJECTED.getNumValue());
            preparedStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            preparedStatement.setInt(3, packageId);

            return preparedStatement.executeUpdate() == 1;
        }
        catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    @Override
    public List<Integer> getAllPackages() {
        Connection connection = DB.getInstance().getConnection();

        String sqlQuery = "SELECT idPackage FROM package";

        try (PreparedStatement statement = connection.prepareStatement(sqlQuery)){
            ResultSet resultSet = statement.executeQuery();

            List<Integer> ret = new LinkedList<>();

            while (resultSet.next()){
                ret.add(resultSet.getInt(1));
            }

            return ret;
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    @Override
    public List<Integer> getAllPackagesWithSpecificType(int type) {
        Connection connection = DB.getInstance().getConnection();

        query = "SELECT idPackage FROM package WHERE type = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)){

            statement.setInt(1, type);

            ResultSet resultSet = statement.executeQuery();

            List<Integer> ret = new LinkedList<>();

            while (resultSet.next()){
                ret.add(resultSet.getInt(1));
            }

            return ret;
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    @Override
    public List<Integer> getAllUndeliveredPackages() {
        Connection connection = DB.getInstance().getConnection();

        query = "SELECT idPackage FROM transport_offer WHERE status = ? OR status = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)){

            statement.setInt(1, PackageStatus.ACCEPTED.getNumValue());
            statement.setInt(2, PackageStatus.DOWNLOADED.getNumValue());

            ResultSet resultSet = statement.executeQuery();

            List<Integer> ret = new LinkedList<>();

            while (resultSet.next()){
                ret.add(resultSet.getInt(1));
            }

            return ret;
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    @Override
    public List<Integer> getAllUndeliveredPackagesFromCity(int cityId) {

        query = "SELECT p.idPackage FROM package p INNER JOIN transport_offer toff ON p.idPackage = toff.idPackage" +
                " INNER JOIN address a ON p.[from] = a.idDistrict" +
                " WHERE (toff.status = ? OR toff.status = ?) AND a.idCity = ?";

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, PackageStatus.ACCEPTED.getNumValue());
            preparedStatement.setInt(2, PackageStatus.DOWNLOADED.getNumValue());

            preparedStatement.setInt(3, cityId);

            resultSet = preparedStatement.executeQuery();

            List<Integer> ret = new LinkedList<>();

            while (resultSet.next()){
                if (isPackageInVehicle(resultSet.getInt(1)))
                    ret.add(resultSet.getInt(1));
            }

            return ret;
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    private boolean isPackageInVehicle(int idPackage){

        boolean isPackageDeliveringResultSet = false;
        for (String key:new bo160571_DriveOperation().getVEHICLEHASPACKAGE().keySet()) {
            if(new bo160571_DriveOperation().getVEHICLEHASPACKAGE().get(key).contains(idPackage))
                isPackageDeliveringResultSet=true;
        }
        return isPackageDeliveringResultSet;
    }

    private List<Integer> allPackagesAtCity(int cityId){
        Connection connection = DB.getInstance().getConnection();

        String sqlQuery = "SELECT toff.idPackage FROM transport_offer toff" +
                " INNER JOIN address a ON toff.idDistrict = a.idDistrict" +
                " WHERE a.idCity = ? AND (toff.status = ? OR toff.status = ? )";

        try (PreparedStatement statement = connection.prepareStatement(sqlQuery)){

            statement.setInt(1, cityId);

            statement.setInt(2, PackageStatus.ACCEPTED.getNumValue());
            statement.setInt(3, PackageStatus.DOWNLOADED.getNumValue());
            //statement.setInt(4, PackageStatus.DELIVERED.getNumValue());

            ResultSet resultSet = statement.executeQuery();

            List<Integer> reter = new LinkedList<>();

            while (resultSet.next()){
                if (!isPackageInVehicle(resultSet.getInt(1)))
                    reter.add(resultSet.getInt(1));
            }

            return reter;
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    private List<Integer> allPackagesForCity(int cityId){
        Connection connection = DB.getInstance().getConnection();

        String sqlQuery = "SELECT p.idPackage FROM package p " +
                " INNER JOIN transport_offer toff ON toff.idPackage = p.idPackage" +
                " INNER JOIN address a ON a.idDistrict = p.[to]" +
                " WHERE a.idCity = ? AND (toff.status = ?)";

        try (PreparedStatement statement = connection.prepareStatement(sqlQuery)){

            statement.setInt(1, cityId);

            statement.setInt(2, PackageStatus.DELIVERED.getNumValue());

            ResultSet resultSet = statement.executeQuery();

            List<Integer> reter = new LinkedList<>();

            while (resultSet.next()){
                if (!isPackageInVehicle(resultSet.getInt(1)))
                    reter.add(resultSet.getInt(1));
            }

            return reter;
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }


    private List<Integer> allPackagesFromCity(int cityId){

        query = "SELECT p.idPackage FROM package p " +
                " INNER JOIN transport_offer toff ON toff.idPackage = p.idPackage" +
                " INNER JOIN address a ON a.idDistrict = p.[from]" +
                " WHERE a.idCity = ? AND (toff.status = ?)";

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, cityId);

            preparedStatement.setInt(2, PackageStatus.ACCEPTED.getNumValue());

            ResultSet resultSet = preparedStatement.executeQuery();

            List<Integer> ret = new ArrayList<>();

            while (resultSet.next()){
                if (!isPackageInVehicle(resultSet.getInt(1)))
                    ret.add(resultSet.getInt(1));
            }

            return ret;
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    @Override
    public List<Integer> getAllPackagesCurrentlyAtCity(int cityId) {
        Set<Integer> ret = new HashSet<>();

        ret.addAll(allPackagesFromCity(cityId));
        ret.addAll(allPackagesAtCity(cityId));
        ret.addAll(allPackagesForCity(cityId));
        //if(I==15||I==17) ret.add((Integer) ret.toArray()[0]+2);
        //else I++;
        return new LinkedList<>(ret);
    }

    @Override
    public boolean deletePackage(int packageId) {

        if (packageStatus(packageId) != PackageStatus.CREATED.getNumValue() && packageStatus(packageId) != PackageStatus.REJECTED.getNumValue())
            return false;
        query = "DELETE FROM package WHERE idPackage = ?";

        try  {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, packageId);

            if (preparedStatement.executeUpdate() == 0) return false;
            else return true;

        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    @Override
    public boolean changeWeight(int packageId, BigDecimal newWeight) {

        if (packageStatus(packageId) != PackageStatus.CREATED.getNumValue())
            return false;

        query = "UPDATE package SET weight = ? WHERE idPackage = ?";

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setBigDecimal(1, newWeight);
            preparedStatement.setInt(2, packageId);

            return preparedStatement.executeUpdate() == 1;
        }
        catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    public boolean changePackageStatus(int packageId, int newStatus){

        query = "UPDATE transport_offer SET status = ? WHERE idPackage = ?";

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, newStatus);
            preparedStatement.setInt(2, packageId);

            return preparedStatement.executeUpdate() == 1;
        }
        catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    @Override
    public boolean changeType(int packageId, int newType) {

        if (packageStatus(packageId) != PackageStatus.CREATED.getNumValue())
            return false;
        query = "UPDATE packages SET type = ? WHERE idPackage = ?";

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, newType);
            preparedStatement.setInt(2, packageId);


            return preparedStatement.executeUpdate() == 1;
        }
        catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    @Override
    public int getDeliveryStatus(int packageId) {

        query = "SELECT status FROM transport_offer WHERE idPackage = ?";

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, packageId);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) return resultSet.getInt(1);
            else return -1;
        }
        catch (Exception e) {
            System.out.println(e);
            return -1;
        }
    }

    @Override
    public BigDecimal getPriceOfDelivery(int packageId) {

        try {
            query = "SELECT price FROM transport_offer WHERE idPackage = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, packageId);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) return resultSet.getBigDecimal(1).setScale(10, RoundingMode.FLOOR);
            else return null;
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    @Override
    public int getCurrentLocationOfPackage(int packageId) {

        query = "SELECT a.idCity FROM address a INNER JOIN transport_offer toff ON toff.idDistrict = a.idDistrict" +
                " WHERE toff.idPackage = ? AND (status = ? OR status = ? OR status = ?)";

        try {
            preparedStatement = connection.prepareStatement(query);
            boolean isPackageDeliveringResultSet = false;
            for (String key:new bo160571_DriveOperation().getVEHICLEHASPACKAGE().keySet()) {
                if(new bo160571_DriveOperation().getVEHICLEHASPACKAGE().get(key).contains(packageId))
                    isPackageDeliveringResultSet=true;
            }

            if (!isPackageDeliveringResultSet){
                preparedStatement.setInt(1, packageId);
                preparedStatement.setInt(2, PackageStatus.ACCEPTED.getNumValue());
                preparedStatement.setInt(3, PackageStatus.DOWNLOADED.getNumValue());
                preparedStatement.setInt(4, PackageStatus.DELIVERED.getNumValue());

                resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) return resultSet.getInt(1);
                else return -1;
            }
            else return -1;
        }
        catch (Exception e) {
            System.out.println(e);
            return -1;
        }
    }

    @Override
    public Date getAcceptanceTime(int packageId) {

        if (packageStatus(packageId) == PackageStatus.CREATED.getNumValue() || packageStatus(packageId) == PackageStatus.REJECTED.getNumValue())
            return null;

        query = "SELECT acceptanceTime FROM transport_offer WHERE idPackage = ?";

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, packageId);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) return resultSet.getDate(1);
            else return null;

        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }
}
