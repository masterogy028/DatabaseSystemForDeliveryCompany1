package etf.ac.bg.rs.student;


import DB.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

public class Klasa {

    public boolean PretprodajaUlaznica(String imeKupca, String nazivDogadjaja, int brojUlaznica) {
        if(brojUlaznica>3) return false;
        Connection connection = DB.getInstance().getConnection();
        String query = "select * from DOGADJAJ where Naziv = ? ";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, nazivDogadjaja);
            ResultSet rs = preparedStatement.executeQuery();
            if(!rs.next()) return false;
            int sifD = rs.getInt("SifD");
            System.out.println("SifD : "+ sifD);
            preparedStatement = connection.prepareStatement("select * from KUPAC where Ime=?");
            preparedStatement.setString(1, imeKupca);
            rs=preparedStatement.executeQuery();
            if(!rs.next()) return false;
            int sifK =rs.getInt("SifK");
            System.out.println("SifK : "+ sifK);

            preparedStatement = connection.prepareStatement("SELECT v.SifU\n" +
                    "FROM VAZI v\n" +
                    "LEFT JOIN KUPIO k ON k.SifU = v.SifU\n" +
                    "WHERE k.SifU IS NULL and v.SifD=?");
            preparedStatement.setInt(1, sifD);
            rs=preparedStatement.executeQuery();
            LinkedList<Integer> ll = new LinkedList<>();
            while (rs.next()) {
                System.out.println(rs.getInt("SifU"));
                ll.add(rs.getInt("SifU"));
            }
            if(ll.size()<brojUlaznica)return false;

            


        } catch (SQLException throwables) {

            throwables.printStackTrace();
            return false;
        }

        return true;
    }
    public static void main(String[] args) {

        DB.getInstance().getConnection();
        Klasa k =new Klasa();
        boolean b;
        b = k.PretprodajaUlaznica("K1", "D1", 3);
        System.out.println(b);
        /*b = k.PretprodajaUlaznica("K1", "D1", 4);
        System.out.println(b);
        b = k.PretprodajaUlaznica("K1", "D1", 4);
        System.out.println(b);
        b = k.PretprodajaUlaznica("K1", "D1", 4);
        System.out.println(b);
        b = k.PretprodajaUlaznica("K1", "D1", 4);
        System.out.println(b);*/
    }
}
