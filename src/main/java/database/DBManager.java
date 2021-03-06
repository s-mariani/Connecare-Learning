package database;

import model.Model;
import model.interfaces.IModel;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Andrea De Castri on 09/11/2017.
 *
 */
public class DBManager {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final String URL = "jdbc:mysql://localhost:3306/osteolab?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "r00t";


    public DBManager(){
        super();
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public boolean saveNewModel(Connection connection, String modelID, String modelName, String providerId, String description,
                                String xVariables, String yVariable) throws SQLException {

        PreparedStatement stmt = connection.prepareStatement("INSERT INTO MODEL (id, name, providerId, description, " +
                "xVariables, yVariable) VALUES (?,?,?,?,?,?)");
        stmt.setString(1, modelID);
        stmt.setString(2, modelName);
        stmt.setString(3, providerId);
        stmt.setString(4, description);
        stmt.setString(5, xVariables);
        stmt.setString(6, yVariable);

        int numRows = stmt.executeUpdate();

        stmt.close();

        return numRows == 1;
    }

    public IModel getModelFromID(Connection connection, String modelID) throws SQLException {
        String sql = "SELECT M.*, C.name AS provider FROM MODEL M, CLINIC C " +
                "WHERE C.id = M.providerId AND M.id = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, modelID);

        ResultSet rs = stmt.executeQuery();
        rs.next();

        String name = rs.getString("name");
        String providerId = rs.getString("providerId");
        String provider = rs.getString("provider");
        String description = rs.getString("description");
        boolean isTrainable = rs.getBoolean("trainable");
        boolean hasGlobalAccess = rs.getBoolean("globalAccess");
        boolean isOnlineTrainable = rs.getBoolean("onlineTrainable");
        String xVariables = rs.getString("xVariables");
        String yVariable = rs.getString("yVariable");
        Date creationDate = rs.getDate("creationDate");
        Date lastUpdateDate = rs.getDate("lastUpdate");

        IModel model = new Model(modelID, name, providerId, provider, description, xVariables, yVariable, isTrainable,
                hasGlobalAccess, isOnlineTrainable, creationDate, lastUpdateDate);

        stmt.close();

        return model;
    }

    public boolean snapshotModel(Connection connection, String modelID) throws SQLException {
        String sql = "UPDATE MODEL SET trainable = ?, onlineTrainable = ?, lastUpdate = NOW() WHERE id = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setBoolean(1, false);
        stmt.setBoolean(2, false);
        stmt.setString(3, modelID);

        int numRows = stmt.executeUpdate();

        stmt.close();

        return numRows == 1;
    }

    public boolean deleteModel(Connection connection, String modelID) throws SQLException {
        String sql = "DELETE FROM MODEL WHERE id = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, modelID);

        int numRows = stmt.executeUpdate();

        stmt.close();

        return numRows == 1;
    }

    public boolean updateOnlineTraining(Connection connection, String modelID, boolean isOnlineTrainable) throws SQLException {
        String sql = "UPDATE MODEL SET onlineTrainable = ?, lastUpdate = NOW() WHERE id = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setBoolean(1, isOnlineTrainable);
        stmt.setString(2, modelID);

        int numRows = stmt.executeUpdate();

        stmt.close();

        return numRows == 1;
    }

    public boolean updateGlobalAccess(Connection connection, String modelID, boolean globalAccess) throws SQLException {
        String sql = "UPDATE MODEL SET globalAccess = ?, lastUpdate = NOW() WHERE id = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setBoolean(1, globalAccess);
        stmt.setString(2, modelID);

        int numRows = stmt.executeUpdate();

        stmt.close();

        return numRows == 1;
    }

    public List<Model> getModels(Connection connection) throws SQLException {
        String sql = "SELECT C.name AS clinic, M.* FROM CLINIC C, MODEL M WHERE C.id = M.providerId";
        PreparedStatement stmt = connection.prepareStatement(sql);

        ResultSet rs = stmt.executeQuery();
        List<Model> list = new ArrayList<>();
        while (rs.next()){
            String modelID = rs.getString("id");
            String name = rs.getString("name");
            String providerID = rs.getString("providerId");
            String provider = rs.getString("clinic");
            String description = rs.getString("description");
            String xVariables = rs.getString("xVariables");
            String yVariable = rs.getString("yVariable");
            boolean trainable = rs.getBoolean("trainable");
            boolean globalAccess = rs.getBoolean("globalAccess");
            boolean onlineLearning = rs.getBoolean("onlineTrainable");
            String creationDateString = rs.getString("creationDate");
            String lastUpdateString = rs.getString("lastUpdate");
            Date creationDate = null;
            Date lastUpdate = null;
            try {
                creationDate = dateFormat.parse(creationDateString);
                lastUpdate = dateFormat.parse(lastUpdateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Model model = new Model(modelID, name, providerID, provider, description, xVariables, yVariable, trainable,
                    globalAccess, onlineLearning, creationDate, lastUpdate);
            list.add(model);
        }

        return list;
    }

}
