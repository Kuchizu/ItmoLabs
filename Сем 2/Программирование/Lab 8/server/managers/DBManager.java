package managers;

import collections.Coordinates;
import collections.Flat;
import collections.Furnish;
import collections.House;
import commands.Struct;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.ZoneId;
import java.util.ArrayDeque;

/**
 * XMLManager for managing XML DB
 */
public class DBManager {

    public DBManager() throws SQLException {
    }
    public static ArrayDeque<Flat> getData() {
        return data;
    }

    public void setData(ArrayDeque<Flat> data) {
        DBManager.data = data;
    }

    // jdbc:postgresql://localhost/postgres?user=postgres&password=postgres
    // jdbc:postgresql://127.0.0.1:5432/studs?user=s373746&password=zxXKGXjCdzuyqIhi
    private static String host = "jdbc:postgresql://localhost/postgres?user=postgres&password=postgres";
    static Connection conn;
    public static void setHost(String sqlhost) {
        host = sqlhost;
    }

    static {
        try {
            conn = DriverManager.getConnection(host);
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            System.err.println("Ошибка при подключении к PostgresSQL");
            e.printStackTrace();
        }
    }
    private static Statement st;

    static {
        try {
            st = conn.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static ArrayDeque<Flat> data = new ArrayDeque<>();

    public static String getSHA224Hash(String input) {
        try {
            StringBuilder hexString = new StringBuilder();
            MessageDigest digest = MessageDigest.getInstance("SHA-224");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String[] check_user(String login, String pass) throws SQLException {

        PreparedStatement psuser = conn.prepareStatement("Select * from Users where login = ?");
        psuser.setString(1, login);
        ResultSet rsuser = psuser.executeQuery();

        if(!rsuser.next()){
            return new String[]{"Err1", String.format("User with login %s not found.", login)};
        }

        PreparedStatement pspass = conn.prepareStatement("Select * from Users where login = ? and password = ?");
        pspass.setString(1, login);
        pspass.setString(2, getSHA224Hash(pass));
        ResultSet rspass = pspass.executeQuery();

        if(!rspass.next()){
            return new String[]{"Err2", String.format("Wrong password for user %s", login)};
        }

        return new String[]{"Succ", "Успешная авторизация."};
    }

    public static String[] reg_user(String login, String pass) throws SQLException {
        if(!check_user(login, pass)[0].equals("Err1")){
            return new String[]{"Err1", String.format("Пользователь с логином %s уже существует.", login)};
        }

        PreparedStatement psreg = conn.prepareStatement("Insert into Users values (DEFAULT, ?, ?)");
        psreg.setString(1, login);
        psreg.setString(2, getSHA224Hash(pass));
        psreg.execute();

        return new String[]{"Succ", String.format("Пользователь %s успешно зарегистрирован", login)};
    }

    public static String getFlatOwnerLogin(int flatid) throws SQLException {

        PreparedStatement psown = conn.prepareStatement("Select owner_id from Flats where id = ?");
        psown.setInt(1, flatid);
        ResultSet rsown = psown.executeQuery();

        if(!rsown.next()){
            return null;
        }

        PreparedStatement pslog = conn.prepareStatement("Select login from Users where id = ?");
        pslog.setInt(1, rsown.getInt(1));
        ResultSet rslog = pslog.executeQuery();
        rslog.next();

        return rslog.getString(1);
    }

    public static void removeflat(int flatid) throws SQLException {
        PreparedStatement psdel = conn.prepareStatement("Delete from Flats where id = ?");
        psdel.setInt(1, flatid);
        psdel.execute();
        data.removeIf((x) -> x.getId() == flatid);

    }

    public static void loadData() throws SQLException {
        st.execute(Struct.struct);

        ResultSet rs = st.executeQuery("Select * from flats");
        while(rs.next()) {
            int flatid = rs.getInt(1);
            int ownerid = rs.getInt(2);
            String name = rs.getString(3);
            int cordid = rs.getInt(4);
            Timestamp ctime = rs.getTimestamp(5);
            int area = rs.getInt(6);
            int numberOfRooms = rs.getInt(7);
            float timeToMetroOnFoot = rs.getFloat(8);
            double timeToMetroByTransport = rs.getDouble(9);
            Furnish furnish = Furnish.valueOf(rs.getString(10));
            int houseid = rs.getInt(11);

            PreparedStatement pscord = conn.prepareStatement("Select x, y from Coordinates where id = ?");
            PreparedStatement pshouse = conn.prepareStatement("Select name, year, numberOfFloors, numberOfLifts from Houses where id = ?");
            pscord.setInt(1, cordid);
            pshouse.setInt(1, houseid);

            ResultSet cord = pscord.executeQuery();
            cord.next();

            ResultSet house = pshouse.executeQuery();
            house.next();


            Flat flat = new Flat(
                    flatid,
                    ownerid,
                    name,
                    new Coordinates(cord.getLong(1), cord.getDouble(2)),
                    ctime.toLocalDateTime().atZone(ZoneId.systemDefault()),
                    area,
                    numberOfRooms,
                    timeToMetroOnFoot,
                    timeToMetroByTransport,
                    furnish,
                    new House(house.getString(1), house.getLong(2), house.getInt(3), house.getInt(4))
            );

            data.add(flat);
        }
    }
    public static void addElement(Flat flat, String ownerlogin, String ownerpass) throws SQLException {

        PreparedStatement psowner = conn.prepareStatement("Select id from Users where login = ? and password = ?");
        psowner.setString(1, ownerlogin);
        psowner.setString(2, getSHA224Hash(ownerpass));
        ResultSet owner_id = psowner.executeQuery();
        owner_id.next();

        PreparedStatement pscord = conn.prepareStatement("Insert into Coordinates values (DEFAULT, ?, ?) returning id");
        PreparedStatement pshouse = conn.prepareStatement("Insert into Houses values (DEFAULT, ?, ?, ?, ?) returning id");

        pscord.setLong(1, flat.getCoordinates().getX());
        pscord.setDouble(2, flat.getCoordinates().getY());
        pshouse.setString(1, flat.getHouse().getName());
        pshouse.setLong(2, flat.getHouse().getYear());
        pshouse.setLong(3, flat.getHouse().getNumberOfFloors());
        pshouse.setInt(4, flat.getHouse().getNumberOfLifts());

        ResultSet cord = pscord.executeQuery();
        cord.next();

        ResultSet house = pshouse.executeQuery();
        house.next();

        PreparedStatement addflat = conn.prepareStatement("Insert into flats values (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) returning id");
        addflat.setInt(1, owner_id.getInt(1));
        addflat.setString(2, flat.getName());
        addflat.setInt(3, cord.getInt(1));
        addflat.setTimestamp(4, Timestamp.valueOf(flat.getCreationDate().toLocalDateTime()));
        addflat.setInt(5, flat.getArea());
        addflat.setInt(6, flat.getNumberOfRooms());
        addflat.setFloat(7, flat.getTimeToMetroOnFoot());
        addflat.setDouble(8, flat.getTimeToMetroByTransport());
        addflat.setObject(9, flat.getFurnish(), Types.OTHER);
        addflat.setInt(10, house.getInt(1));

        ResultSet flatid = addflat.executeQuery();
        flatid.next();

        flat.setId(flatid.getInt(1));

        data.add(flat);

    }
    public static void changeElement(int flatid, Flat flat, String login, String pass) throws SQLException {

        removeflat(flat.getId());
        addElement(flat, login, pass);

        for(Flat f : DBManager.data){
            if (f.getId() == flatid){
                flat.setId(f.getId());
                flat.setCreationDate(f.getCreationDate());
                DBManager.data.remove(f);
                break;
            }
        }
        DBManager.data.add(flat);
    }
}
