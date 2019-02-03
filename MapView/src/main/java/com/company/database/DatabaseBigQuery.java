package com.company.database;

import java.sql.*;

public class DatabaseBigQuery {
    private static final String CONNECTION_URL = "jdbc:bigquery://https://www.googleapis.com/bigquery/v2:443;ProjectId=isen-roadeo;OAuthType=0;OAuthServiceAcctEmail=python-bq-pubsub@isen-roadeo.iam.gserviceaccount.com;OAuthPvtKeyPath=C:\\Users\\Jelle Metzlar\\isen-roadeo-067b664ac003.json;";

    private static Connection connection;

    static {
        try {
            Class.forName("com.simba.googlebigquery.jdbc42.Driver");
            Class.forName("com.simba.googlebigquery.jdbc42.DataSource");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public DatabaseBigQuery() throws SQLException {
        connection = DriverManager.getConnection(CONNECTION_URL);
        System.out.println(connection.isValid(5000));
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT count(*) FROM `sensor_data.measurements`");

        if (rs.next()) {
            System.out.println(rs.getInt(1));
        }
    }
}
