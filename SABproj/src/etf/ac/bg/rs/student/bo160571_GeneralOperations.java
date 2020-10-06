package etf.ac.bg.rs.student;

import rs.etf.sab.operations.GeneralOperations;
import DB.DB;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

public class bo160571_GeneralOperations implements GeneralOperations {
    @Override
    public void eraseAll() {
        new bo160571_CourierRequestOperation().deleteCourierRequests();
        new bo160571_DriveOperation().deleteVEHICLEHASPACKAGE();
        new bo160571_DriveOperation().deleteSTOPS();
        Connection connection = DB.getInstance().getConnection();

        try (CallableStatement callableStatement = connection.prepareCall("EXEC EraseAll")){

            callableStatement.execute();

        } catch (SQLException e) {

        }
    }
}
