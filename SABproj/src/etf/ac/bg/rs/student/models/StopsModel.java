package etf.ac.bg.rs.student.models;

public class StopsModel {
    private int idCity;
    private int idPack;
    private int order;
    private int idPackage;
    private int planType;
    private int idDistrict;

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getIdPackage() {
        return idPackage;
    }

    public void setIdPackage(int idPackage) {
        this.idPackage = idPackage;
    }

    public int getPlanType() {
        return planType;
    }

    public void setPlanType(int planType) {
        this.planType = planType;
    }

    public StopsModel(int order, int idPackage, int planType, int idDistrict) {
        this.order = order;
        this.idPackage = idPackage;
        this.planType = planType;
        this.idDistrict = idDistrict;
    }

    public int getIdDistrict() {
        return idDistrict;
    }

    public void setIdDistrict(int idDistrict) {
        this.idDistrict = idDistrict;
    }

    public int getIdCity() {
        return idCity;
    }

    public void setIdCity(int idCity) {
        this.idCity = idCity;
    }

    public int getIdPack() {
        return idPack;
    }

    public void setIdPack(int idPack) {
        this.idPack = idPack;
    }
}
