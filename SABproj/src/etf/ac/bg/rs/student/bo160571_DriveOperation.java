package etf.ac.bg.rs.student;

import etf.ac.bg.rs.student.models.StopsModel;
import javafx.util.Pair;
import rs.etf.sab.operations.DriveOperation;
import rs.etf.sab.operations.VehicleOperations;
import DB.DB;
import util.UtilOperations;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class bo160571_DriveOperation implements DriveOperation {
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private String query;
    private Connection connection;
    private UtilOperations utilOperations;
    private bo160571_PackageOperations packageOperations;
    private static HashMap<String, ArrayList<Integer>> VEHICLE_HAS_PACKAGE = new HashMap<>();
    private static HashMap<String, ArrayList<StopsModel>> STOPS_FOR_PLAN = new HashMap<>();
    private static HashMap<String, Integer> CURRENT_ORDERS = new HashMap<>();

    public bo160571_DriveOperation() {

        connection = DB.getInstance().getConnection();
        //helperOperations = new HelperOperations();
        //packageOperations = new MyPackageOperations();
    }

    private enum GASS {
        NATURALGASS(15),
        OIL(36),
        GASSOLINE(32);
        private int v;
        GASS(int v){
            this.v = v;
        }
        public int getv() {
            return v;
        }
    };

    private int getStockroomForCourier(String courierUsername){

        try {
            query = "SELECT a.idCity FROM address a INNER JOIN [user] u on u.idDistrict = a.idDistrict " +
                    "WHERE u.username = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, courierUsername);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                bo160571_StockroomOperations bo160571StockroomOperations = new bo160571_StockroomOperations();
                return bo160571StockroomOperations.getStockroomFromCity(resultSet.getInt(1));
            }
            else return -1;
        }
        catch (Exception e) {
            System.out.println(e);
            return -1;
        }
    }


    @Override
    public boolean planingDrive(java.lang.String courierUsername) {
        bo160571_StockroomOperations bo160571StockroomOperations = new bo160571_StockroomOperations();
        try {
            // taking vehicle method that either does the job or returns null;
            Pair<String, Integer> pairLicenceStock = takeTheVehicle(courierUsername);
            if(pairLicenceStock == null) return false;
            // planning the deliveries here
            List<StopsModel> stops = new LinkedList<>();

            UtilOperations utilOperations = new UtilOperations();
            BigDecimal capacity = utilOperations.getMaxWeight(pairLicenceStock.getKey());
            BigDecimal currentWeight = BigDecimal.ZERO;
            int currentOrder = 0;
            int startingCity = utilOperations.getCityForStockroom(pairLicenceStock.getValue());
            Pair<Integer, Integer> currentLocation = null; // TODO

            // taking packages from current city ordered by acceptance time (only these packages will be delivered)
            List<Integer> packages = utilOperations.getAllAcceptedPackagesFromCityNotPlanned(startingCity);
            for (Integer idPackage: packages){
                if (utilOperations.getPackageWeight(idPackage).add(currentWeight).compareTo(capacity) <= 0){
                    currentWeight = currentWeight.add(utilOperations.getPackageWeight(idPackage));
                    stops.add(new StopsModel(currentOrder++, idPackage, 0, utilOperations.getIdDistrictFromPackage(idPackage)));
                }
            }

            // if needed go to stockroom from that city
            List<Integer> packagesInStockroom = utilOperations.getPackagesFromStockroom(pairLicenceStock.getValue());
            for (Integer idPackage: packagesInStockroom){
                if (utilOperations.getPackageWeight(idPackage).add(currentWeight).compareTo(capacity) <= 0){
                    currentWeight = currentWeight.add(utilOperations.getPackageWeight(idPackage));
                    stops.add(new StopsModel(currentOrder++, idPackage, 1, utilOperations.getIdDistrictFromPackage(idPackage)));
                }
            }
            if (stops.size() > 0)
                currentLocation = utilOperations.getLocationFromPackage(stops.get(stops.size() - 1).getIdPackage());


            // planning delivery (shortest path)

            List<StopsModel> deliveringStops = new LinkedList<>();

            for (StopsModel stopModel: stops)
                deliveringStops.add(new StopsModel(stopModel.getOrder(), stopModel.getIdPackage(), stopModel.getIdPackage(), stopModel.getIdDistrict()));

            int lastCity = Integer.MAX_VALUE;

            while (deliveringStops.size() > 0){
                int idCurrCity = -1;
                double shortestDist = Double.MAX_VALUE;
                StopsModel shortestStop = null;
                for (StopsModel stopModel: deliveringStops) {
                    if (calculateDistanceBetween(currentLocation, utilOperations.getFinalLocationForPackage(stopModel.getIdPackage())) < shortestDist) {
                        shortestStop = stopModel;
                        shortestDist = calculateDistanceBetween(currentLocation, utilOperations.getFinalLocationForPackage(stopModel.getIdPackage()));
                    }
                }

                // checking if this is the new city
                idCurrCity = utilOperations.getCityFromLocation(utilOperations.getFinalLocationForPackage(shortestStop.getIdPackage()).getKey(), utilOperations.getFinalLocationForPackage(shortestStop.getIdPackage()).getValue());
                if (lastCity != Integer.MAX_VALUE && idCurrCity != lastCity){

                    // trying to pick up more packages

                    packagesInStockroom = utilOperations.getAllAcceptedPackagesFromCityNotPlanned(lastCity);
                    for (Integer idPackage: packagesInStockroom){
                        boolean found = false;
                        for (StopsModel stop: stops)
                            if (stop.getIdPackage() == idPackage){
                                found = true;
                                break;
                            }
                        if (found)
                            continue;
                        if (utilOperations.getPackageWeight(idPackage).add(currentWeight).compareTo(capacity) <= 0){
                            currentWeight = currentWeight.add(utilOperations.getPackageWeight(idPackage));
                            stops.add(new StopsModel(currentOrder++, idPackage, 4, utilOperations.getIdDistrictFromPackage(idPackage)));
                        }
                    }

                    packagesInStockroom = utilOperations.getPackagesFromStockroom(bo160571StockroomOperations.getStockroomFromCity(lastCity));
                    for (Integer idPackage: packagesInStockroom){
                        boolean found = false;
                        for (StopsModel stop: stops)
                            if (stop.getIdPackage() == idPackage){
                                found = true;
                                break;
                            }
                        if (found)
                            continue;
                        if (utilOperations.getPackageWeight(idPackage).add(currentWeight).compareTo(capacity) <= 0){
                            currentWeight = currentWeight.add(utilOperations.getPackageWeight(idPackage));
                            stops.add(new StopsModel(currentOrder++, idPackage, 4, utilOperations.getIdDistrictFromPackage(idPackage)));
                        }
                    }

                }
                lastCity = idCurrCity;

                // adding to stops
                deliveringStops.remove(shortestStop);
                currentLocation = utilOperations.getFinalLocationForPackage(shortestStop.getIdPackage());
                currentWeight.add(utilOperations.getPackageWeight(shortestStop.getIdPackage()).negate());
                stops.add(new StopsModel(currentOrder++, shortestStop.getIdPackage(), 2, utilOperations.getAddressToForPackage(shortestStop.getIdPackage())));

            }

            if (lastCity != Integer.MAX_VALUE){
                // for last city
                packagesInStockroom = utilOperations.getAllAcceptedPackagesFromCityNotPlanned(lastCity);
                for (Integer idPackage: packagesInStockroom){
                    boolean found = false;
                    for (StopsModel stop: stops)
                        if (stop.getIdPackage() == idPackage){
                            found = true;
                            break;
                        }
                    if (found)
                        continue;
                    if (utilOperations.getPackageWeight(idPackage).add(currentWeight).compareTo(capacity) <= 0){
                        currentWeight = currentWeight.add(utilOperations.getPackageWeight(idPackage));
                        stops.add(new StopsModel(currentOrder++, idPackage, 4, utilOperations.getIdDistrictFromPackage(idPackage)));
                    }
                }

                packagesInStockroom = utilOperations.getPackagesFromStockroom(bo160571StockroomOperations.getStockroomFromCity(lastCity));
                for (Integer idPackage: packagesInStockroom){
                    boolean found = false;
                    for (StopsModel stop: stops)
                        if (stop.getIdPackage() == idPackage){
                            found = true;
                            break;
                        }
                    if (found)
                        continue;
                    if (utilOperations.getPackageWeight(idPackage).add(currentWeight).compareTo(capacity) <= 0){
                        currentWeight = currentWeight.add(utilOperations.getPackageWeight(idPackage));
                        stops.add(new StopsModel(currentOrder++, idPackage, 4, utilOperations.getIdDistrictFromPackage(idPackage)));
                    }
                }
            }

            List<StopsModel> newStops = new LinkedList<>();
            // adding stockroom as final
            for (StopsModel stopModel: stops){
                if (stopModel.getPlanType() == 4){
                    newStops.add(new StopsModel(
                            currentOrder++,
                            stopModel.getIdPackage(),
                            3,
                            utilOperations.getIdDistrictForStockroom(pairLicenceStock.getValue())
                    ));
                }
            }

            stops.addAll(newStops);

            // insert into stops
            setCurrentOrder(courierUsername, 0);
            for (StopsModel stopModel: stops)
                insertPlanStop(courierUsername, stopModel);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Pair<String, Integer> takeTheVehicle(String courierUsername) {
        utilOperations = new UtilOperations();

        bo160571_StockroomOperations bo160571StockroomOperations = new bo160571_StockroomOperations();
        int idStockroom;

        idStockroom = getStockroomForCourier(courierUsername);
        String licenceV = null;
        query = "SELECT TOP 1 licenceV FROM park WHERE idStock = ?";
        preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, idStockroom);
            ResultSet rs = preparedStatement.executeQuery();
            if(rs.next()) {
                licenceV = rs.getString(1);
            }else return null;
            query = "DELETE FROM park WHERE licenceV = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, licenceV);
            if(preparedStatement.executeUpdate()==0){throw new Exception(); }
            query = "UPDATE courir SET licenceV = ?, [status] = 1 WHERE username = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, licenceV);
            preparedStatement.setString(2, courierUsername);
            if(preparedStatement.executeUpdate()==0){throw new Exception(); }
            return new Pair<String, Integer>(licenceV, idStockroom);
        } catch (Exception throwables) {
            throwables.printStackTrace();
            return null;
        }


    }


    private void insertPlanStop(String courierUsername, StopsModel planHasStopModel){

        try{
            if(STOPS_FOR_PLAN.get(courierUsername)==null)STOPS_FOR_PLAN.put(courierUsername, new ArrayList<>());
            STOPS_FOR_PLAN.get(courierUsername).add(planHasStopModel);
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
    private double calculateDistanceBetween (Pair<Integer, Integer> loc1, Pair<Integer, Integer> loc2){
        return Math.sqrt((loc1.getKey() - loc2.getKey()) * (loc1.getKey() - loc2.getKey()) + (loc1.getValue() - loc2.getValue()) * (loc1.getValue() - loc2.getValue()));
    }

    private void setCurrentOrder(String courierUsername, int newCurrentOrder){
        CURRENT_ORDERS.put(courierUsername, newCurrentOrder);
    }

    private int getCurrentOrder(String courierUsername){


        try {
            if(CURRENT_ORDERS.get(courierUsername) == null) return -1;
            return CURRENT_ORDERS.get(courierUsername);
        }
        catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }


    private void insertIntoVehicle(String vehicleLicence, int idPackage){
        try {
            if(!VEHICLE_HAS_PACKAGE.containsKey(vehicleLicence))
            {
                VEHICLE_HAS_PACKAGE.put(vehicleLicence, new ArrayList<>());
            }
            VEHICLE_HAS_PACKAGE.get(vehicleLicence).add(idPackage);
        } catch ( Exception e ) {
            e.printStackTrace();
            return;
        }

    }

    private void removeFromVehicle(String vehicleLicence, int idPackage){
        try {
            VEHICLE_HAS_PACKAGE.get(vehicleLicence).remove(new Integer(idPackage));
            if(VEHICLE_HAS_PACKAGE.get(vehicleLicence).size()==0)
                VEHICLE_HAS_PACKAGE.remove(vehicleLicence);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Override
    public int nextStop(String courierUsername) {
        packageOperations = new bo160571_PackageOperations();

        int currentOrder = getCurrentOrder(courierUsername);
        StopsModel currentPackage=null;
        if(STOPS_FOR_PLAN.get(courierUsername)==null)return -1;
        for (StopsModel sm: STOPS_FOR_PLAN.get(courierUsername)) {
            if(sm.getOrder()==currentOrder) {
                currentPackage = sm;
                break;
            }
        }
                int idDistrict = currentPackage.getIdDistrict();
                int idPack = currentPackage.getIdPackage();
                int packagePlanType = currentPackage.getPlanType();
                int retvalue = -1;

                String licenceV = utilOperations.getCouriersVehicle(courierUsername);

                switch (packagePlanType){

                    case 1:
                        // kupljenje iz stockrooma
                        while (true) {
                            insertIntoVehicle(licenceV, idPack);
                            packageOperations.changePackageStatus(idPack, bo160571_PackageOperations.PackageStatus.DOWNLOADED.getNumValue());
                            currentOrder++;
                            setCurrentOrder(courierUsername, currentOrder);
                            retvalue = -2;

                            if (utilOperations.stopType(courierUsername, currentOrder) != 1)
                                break;
                            for (StopsModel sm: STOPS_FOR_PLAN.get(courierUsername)) {
                                if(sm.getOrder()==currentOrder) {
                                    currentPackage = sm;
                                    break;
                                }
                            }
                            idPack = currentPackage.getIdPackage();
                        }

                        break;
                    case 2:
                        // delivery
                        packageOperations.changePackageStatus(idPack, bo160571_PackageOperations.PackageStatus.DELIVERED.getNumValue());
                        removeFromVehicle(licenceV, idPack);
                        currentOrder++;
                        setCurrentOrder(courierUsername, currentOrder);
                        retvalue = idPack;

                        break;
                    case 3:
                        // calls only last time
                        while (utilOperations.stopType(courierUsername, currentOrder) == 3){
                            removeFromVehicle(licenceV, idPack);
                            utilOperations.changeDistrictInPackage(idPack, idDistrict);
                            currentOrder++;
                            setCurrentOrder(courierUsername, currentOrder);
                            for (StopsModel sm: STOPS_FOR_PLAN.get(courierUsername)) {
                                if(sm.getOrder()==currentOrder) {
                                    currentPackage = sm;
                                    break;
                                }
                            }
                            idDistrict = currentPackage.getIdDistrict();
                            idPack = currentPackage.getIdPackage();
                            retvalue = -1;
                        }
                        break;
                    case 4:
                    case 0:
                        insertIntoVehicle(licenceV, idPack);
                        packageOperations.changePackageStatus(idPack, bo160571_PackageOperations.PackageStatus.DOWNLOADED.getNumValue());
                        currentOrder++;
                        setCurrentOrder(courierUsername, currentOrder);
                        retvalue = -2;

                        break;
                    default:
                        break;
                }


                // no stops left
                if (utilOperations.stopType(courierUsername, currentOrder) == -1){

                    bo160571_StockroomOperations stockroomOperations = new bo160571_StockroomOperations();
                    int deliveriesCount = 0;
                    double income = 0;
                    double bonus;
                    List<StopsModel> stops = stopsForPlan(courierUsername);

                    Pair<Integer, Integer> startLocation = utilOperations.getLocationFromAddress(utilOperations.getIdDistrictCurrentForPackage(stops.get(0).getIdPackage()));
                    Pair<Integer, Integer> lastLocation = utilOperations.getLocationFromAddress(utilOperations.getIdDistrictForStockroom(stockroomOperations.getStockroomFromCity(utilOperations.getCityFromLocation(startLocation.getKey(), startLocation.getValue()))));

                    finishRide(courierUsername, stockroomOperations.getStockroomFromCity(utilOperations.getCityFromLocation(startLocation.getKey(), startLocation.getValue())), licenceV);

                    if (utilOperations.stopType(courierUsername, currentOrder - 1) != 3){
                        // add going back to stockroom
                        stops.add(new StopsModel(currentOrder, -1, -1, utilOperations.getIdDistrictForLocation(lastLocation.getKey(), lastLocation.getValue())));
                    }

                    double totalDistance = calculateDistanceBetween(startLocation, lastLocation);
                    for (int i = 0; i < stops.size(); i++){
                        StopsModel stop = stops.get(i);
                        if (stop.getPlanType() == 2) {
                            deliveriesCount++;
                            income += utilOperations.getPriceForPackage(stop.getIdPackage()).doubleValue();
                        }

                        if (i != stops.size() - 1){
                            StopsModel nextStop = stops.get(i + 1);
                            totalDistance += calculateDistanceBetween(
                                    utilOperations.getLocationFromAddress(stop.getIdDistrict()),
                                    utilOperations.getLocationFromAddress(nextStop.getIdDistrict())
                            );
                        }
                    }

                    double myPrice = 0;

                    BigDecimal fuelConsumption = utilOperations.getFuelConsumptionForVehicle(licenceV);

                    switch (utilOperations.getFuel(licenceV)){
                        case 0:
                            myPrice = GASS.NATURALGASS.getv() * totalDistance * fuelConsumption.doubleValue();
                            break;

                        case 1:
                            myPrice = GASS.GASSOLINE.getv() * totalDistance * fuelConsumption.doubleValue();
                            break;
                        case 2:
                            myPrice = GASS.OIL.getv() * totalDistance * fuelConsumption.doubleValue();
                            break;
                        default: break;
                    }

                    bonus = income - myPrice;
                    utilOperations.updateProfitAndNumOfDeliveries(courierUsername, bonus, deliveriesCount);
                    removePlansAndStops(courierUsername);

                }

                return retvalue;

    }

    private void removePlansAndStops(String courierUsername) {
        STOPS_FOR_PLAN.remove(courierUsername);
    }

    private List<StopsModel> stopsForPlan(String courierUsername){
        return STOPS_FOR_PLAN.get(courierUsername);
    }

    protected boolean finishRide(String courierUsername, Integer idStockroom, String vehicleLicence){
        try {
            query = "UPDATE courir SET status = 0, licenceV = NULL WHERE username = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, courierUsername);
            preparedStatement.executeUpdate();
            VehicleOperations myVehicleOperations = new bo160571_VehicleOperations();
            myVehicleOperations.parkVehicle(vehicleLicence, idStockroom);
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Integer> getPackagesInVehicle(java.lang.String courierUsername) {
        try  {
            query = "SELECT licenceV FROM courir where username=?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, courierUsername);
            resultSet = preparedStatement.executeQuery();
            String licenceV;
            if(resultSet.next()){
                licenceV = resultSet.getString(1);
            }else
                return new LinkedList<>();
            if(VEHICLE_HAS_PACKAGE.get(licenceV)!=null) {
                return new LinkedList<>(VEHICLE_HAS_PACKAGE.get(licenceV));
            }else
                return new LinkedList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new LinkedList<>();
        }

    }

    public void deleteVEHICLEHASPACKAGE() {
        VEHICLE_HAS_PACKAGE = new HashMap<>();
    }
    public void deleteSTOPS() {
        STOPS_FOR_PLAN = new HashMap<>();
    }
    public void deleteCURRENTPACKAGES() {
        CURRENT_ORDERS = new HashMap<>();
    }
    public HashMap<String, ArrayList<Integer>> getVEHICLEHASPACKAGE() {
       return VEHICLE_HAS_PACKAGE;
    }
    public HashMap<String, ArrayList<StopsModel>> getSTOPS() {
        return STOPS_FOR_PLAN;
    }
    public HashMap<String, Integer> getCURRENTPACKAGES() {
        return CURRENT_ORDERS;
    }

}
