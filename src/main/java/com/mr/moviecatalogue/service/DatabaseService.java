package com.mr.moviecatalogue.service;

import com.mr.moviecatalogue.domain.Movie;
import com.mr.moviecatalogue.inputobject.MovieIO;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;

@Component
public class DatabaseService {

    /**
     * This method attempts to connect to the database for the project, and if it is not already present,
     * creates the database and tables in the PostgreSQL instance
     */
    public Connection connectToDatabase(){
        Connection c = null;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/moviedb",
                            "postgres", "postgrespw");
        } catch (Exception e) {
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            try {
                Class.forName("org.postgresql.Driver");
                Connection connection = DriverManager
                        .getConnection("jdbc:postgresql://localhost:5432/",
                                "postgres", "postgrespw");
                Statement statement = connection.createStatement();
                statement.executeUpdate("CREATE DATABASE moviedb");
                statement.close();
                connection.close();

                c = DriverManager
                        .getConnection("jdbc:postgresql://localhost:5432/moviedb",
                                "postgres", "postgrespw");

                Statement createTables = c.createStatement();
                createTables.executeUpdate("CREATE TABLE movie_table (TITLE TEXT PRIMARY KEY NOT NULL, DIRECTOR TEXT, RATING INT)");
                createTables.close();
                System.out.println("Table created");
            } catch (Exception exception) {
                exception.printStackTrace();
                System.err.println(exception.getClass().getName()+": "+exception.getMessage());
                System.out.println("Database could not be accessed, exiting application");
                System.exit(0);
            }
        }

        System.out.println("Opened database successfully");
        return c;
    }

    /**
     * Drops the database from the SQL server, clearing any data.
     */
    public void dropDatabase(){
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/",
                            "postgres", "postgrespw");
            Statement statement = connection.createStatement();
            statement.executeUpdate("DROP DATABASE IF EXISTS moviedb");
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getClass().getName()+": "+e.getMessage());
        }
    }

    /**
     *
     * @return
     */
    public Map<String, Movie> getAllMovies(){
        Connection connection = connectToDatabase();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM movie_table;");
            ResultSet resultSet = statement.executeQuery();
            Map<String, Movie> movieMap = new HashMap<>();
            while (resultSet.next()) {
                Movie movie = new Movie();
                movie.setDirector(Optional.ofNullable(resultSet.getString("DIRECTOR")));
                movie.setRating(Optional.ofNullable(resultSet.getInt("RATING")));
                movieMap.put(resultSet.getString("TITLE"), movie);
            }
            statement.close();
            connection.close();
            return movieMap;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            System.out.println(sqle.getClass().getName()+": "+sqle.getMessage());
            return null;
        }
    }

    /**
     *
     * @return
     */
    public void addMovie(MovieIO movieIO){
        Connection connection = connectToDatabase();
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO movie_table (TITLE,DIRECTOR,RATING) VALUES (?,?,?);");
            statement.setString(1, movieIO.getTitle());
            statement.setString(2, movieIO.getDirector());
            statement.setInt(3, movieIO.getRating());
            statement.executeUpdate();
            statement.close();
            connection.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            System.out.println(sqle.getClass().getName()+": "+sqle.getMessage());
        }
    }

    /**
     *
     * @param director
     * @return
     */
    public Map<String, Movie> getMoviesByDirector(String director){
        Connection connection = connectToDatabase();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM movie_table WHERE DIRECTOR = ?;");
            statement.setString(1, director);
            ResultSet resultSet = statement.executeQuery();
            Map<String, Movie> movieMap = new HashMap<>();
            while (resultSet.next()) {
                Movie movie = new Movie();
                movie.setDirector(Optional.ofNullable(resultSet.getString("DIRECTOR")));
                movie.setRating(Optional.ofNullable(resultSet.getInt("RATING")));
                movieMap.put(resultSet.getString("TITLE"), movie);
            }
            statement.close();
            connection.close();
            return movieMap;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            System.out.println(sqle.getClass().getName()+": "+sqle.getMessage());
            return null;
        }
    }

}
