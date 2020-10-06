package etf.ac.bg.rs.student;

import rs.etf.sab.operations.UserOperations;
import DB.DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class bo160571_UserOperations implements UserOperations {
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private String query;
    private Connection connection;
    bo160571_UserOperations() {
        connection = DB.getInstance().getConnection();
    }
    @Override
    public boolean insertUser(String userName, String firstName, String lastName, String password, int idAddress) {

        if (Character.isLowerCase(lastName.charAt(0))){return false;}
        if (password.length() <= 7){return false;}
        if (Character.isLowerCase(firstName.charAt(0))){return false;}

        boolean[] allGood = {false, false, false, false};
        for (int i = 0; i < password.length(); i++){
            if (Character.isDigit(password.charAt(i)))
                allGood[0]=true;
            if (Character.isAlphabetic(password.charAt(i)) && Character.isLowerCase(password.charAt(i)))
                allGood[1]=true;
            if (Character.isAlphabetic(password.charAt(i)) && Character.isUpperCase(password.charAt(i)))
                allGood[2]=true;
            if (!Character.isLetter(password.charAt(i)) && !Character.isDigit(password.charAt(i)))
                allGood[3]=true;
        }
        if (allGood[0]!=true||allGood[1]!=true||allGood[2]!=true||allGood[3]!=true)
            return false;
        try {
            query = "INSERT INTO [user] (username, firstName, lastName, password, idDistrict) VALUES (?, ?, ?, ?, ?)";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, userName);
            preparedStatement.setString(2, firstName);
            preparedStatement.setString(3, lastName);
            preparedStatement.setString(4, password);
            preparedStatement.setInt(5, idAddress);

            if (preparedStatement.executeUpdate() < 1) return false;
            else return true;
        }
        catch (Exception ex){
            System.out.println(ex.toString());
            return false;
        }
    }

    private boolean isAdmin(String userName){

        try {
            preparedStatement = connection.prepareStatement("SELECT isAdmin FROM [user] WHERE username = ?");
            preparedStatement.setString(1, userName);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) return resultSet.getInt(1) == 1;
            return false;
        } catch (Exception ex) {
            System.out.println(ex.toString());
            return false;
        }
    }

    @Override
    public boolean declareAdmin(String userName) {
        if (!isAdmin(userName)) {
            query = "UPDATE [user] SET isAdmin = 1 WHERE username = ?";
            try  {
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, userName);
                return preparedStatement.executeUpdate() == 1;
            } catch (Exception ex) {
                System.out.println(ex.toString());
                return false;
            }
        }
        else return false;
    }

    @Override
    public int getSentPackages(String... userNames) {
        // TODO
        StringBuilder stringBuilder = new StringBuilder("(");

        for (int i = 0; i < userNames.length; i++){
            stringBuilder.append("'" + userNames[i] + "'");
            if (i != userNames.length - 1)
                stringBuilder.append(",");
        }
        stringBuilder.append(")") ;
        String usersExistSqlQuery = "SELECT * FROM [user] WHERE username in " + stringBuilder.toString();
        String sqlQuery = "SELECT COUNT(*) FROM transport_offer toff INNER JOIN package p on toff.idPackage = p.idPackage " +
                "WHERE username IN " + stringBuilder.toString() + " AND toff.status = ?";

        try {
            preparedStatement = connection.prepareStatement(sqlQuery);
            PreparedStatement usersExistStatement = connection.prepareStatement(usersExistSqlQuery);
            // if username exists
            resultSet = usersExistStatement.executeQuery();
            if (resultSet.next()){
                preparedStatement.setInt(1, bo160571_PackageOperations.PackageStatus.DELIVERED.getNumValue());
                resultSet = preparedStatement.executeQuery();
                {
                    if (resultSet.next())
                        return resultSet.getInt(1);
                    else
                        return -1;
                }
            }
            else {
                return -1;
            }
        }
        catch (Exception ex){
            System.out.println(ex.toString());
            return -1;
        }
    }

    @Override
    public int deleteUsers(String... userNames) {
        StringBuilder stringBuilder = new StringBuilder("(");
        for (int i = 0; i < userNames.length; i++){
            stringBuilder.append( "'" + userNames[i] + "'");
            if (i != userNames.length - 1)
                stringBuilder.append( ",");
        }
        stringBuilder.append(")") ;
        query = "DELETE FROM [user] WHERE username IN " + stringBuilder.toString();
        try  {
            preparedStatement = connection.prepareStatement(query);
            return preparedStatement.executeUpdate();
        } catch (Exception ex) {
            System.out.println(ex.toString());
            return 0;
        }
    }

    @Override
    public List<String> getAllUsers() {
        query = "SELECT username FROM [user]";
        try {
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();
            List<String> ret = new ArrayList<>();
            while (resultSet.next()){
                ret.add(resultSet.getString(1));
            }
            return ret;
        }
        catch (Exception ex){
            System.out.println(ex.toString());
            return null;
        }
    }
}
