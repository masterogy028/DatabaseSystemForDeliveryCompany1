package etf.ac.bg.rs.student;

import rs.etf.sab.tests.TestHandler;
import rs.etf.sab.tests.TestRunner;


public class StudentMain {

    public static void main(String[] args) {
        bo160571_AddressOperations addressOperations = new bo160571_AddressOperations(); // Change this to your implementation.
        bo160571_CityOperations cityOperations = new bo160571_CityOperations(); // Do it for all classes.
        bo160571_CourierOperations courierOperations = new bo160571_CourierOperations(); // e.g. = new MyDistrictOperations();
        bo160571_CourierRequestOperation courierRequestOperation = new bo160571_CourierRequestOperation();
        bo160571_DriveOperation driveOperation = new bo160571_DriveOperation();
        bo160571_GeneralOperations generalOperations = new bo160571_GeneralOperations();
        bo160571_PackageOperations packageOperations = new bo160571_PackageOperations();
        bo160571_StockroomOperations stockroomOperations = new bo160571_StockroomOperations();
        bo160571_UserOperations userOperations = new bo160571_UserOperations();
        bo160571_VehicleOperations vehicleOperations = new bo160571_VehicleOperations();

        generalOperations.eraseAll();


        TestHandler.createInstance(
                addressOperations,
                cityOperations,
                courierOperations,
                courierRequestOperation,
                driveOperation,
                generalOperations,
                packageOperations,
                stockroomOperations,
                userOperations,
                vehicleOperations);

        TestRunner.runTests();
    }
}
