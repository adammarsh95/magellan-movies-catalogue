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
                createTables.executeUpdate("CREATE TABLE movie_table (TITLE TEXT PRIMARY KEY NOT NULL, DIRECTOR TEXT, RATING FLOAT)");
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
    public Map<String, Movie> getAllMovies() {
        Connection connection = connectToDatabase();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM movie_table;");
            ResultSet resultSet = statement.executeQuery();
            Map<String, Movie> movieMap = getMovieMapFromResultSet(resultSet);
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
            statement.setFloat(3, movieIO.getRating());
            statement.executeUpdate();
            statement.close();
            connection.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            System.out.println(sqle.getClass().getName()+": "+sqle.getMessage());
        }
    }

    /**
     * Gets a movie from the database by searching for the provided title. This parameter is case sensitive.
     * @param title
     * @return Returns a Movie object for the provided title
     */
    public Movie getMovieByTitle(String title){
        Connection connection = connectToDatabase();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM movie_table WHERE TITLE = ?;");
            statement.setString(1, title);
            ResultSet resultSet = statement.executeQuery();
            Movie movie = null;
            while (resultSet.next()) {
                movie = new Movie();
                movie.setDirector(Optional.ofNullable(resultSet.getString("DIRECTOR")));
                movie.setRating(Optional.ofNullable(resultSet.getFloat("RATING")));
            }
            statement.close();
            connection.close();
            return movie;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            System.out.println(sqle.getClass().getName()+": "+sqle.getMessage());
            return null;
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
            director.replaceAll("\\*","%");
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM movie_table WHERE LOWER(DIRECTOR) LIKE LOWER(?);");
            statement.setString(1, director);
            ResultSet resultSet = statement.executeQuery();
            Map<String, Movie> movieMap = getMovieMapFromResultSet(resultSet);
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
     * @param rating
     * @return
     */
    public Map<String, Movie> getMoviesAboveRating(Float rating){
        Connection connection = connectToDatabase();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM movie_table WHERE RATING >= ?;");
            statement.setFloat(1, rating);
            ResultSet resultSet = statement.executeQuery();
            Map<String, Movie> movieMap = getMovieMapFromResultSet(resultSet);
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
     * @param currentTitle
     * @param newTitle
     */
    public void updateTitle(String currentTitle, String newTitle){
        Connection connection = connectToDatabase();
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE movie_table SET TITLE = ? WHERE lower(TITLE) = lower(?)");
            statement.setString(1, newTitle);
            statement.setString(2, currentTitle);
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
     * @param title
     * @param director
     */
    public void updateDirector(String title, String director){
        Connection connection = connectToDatabase();
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE movie_table SET DIRECTOR = ? WHERE lower(TITLE) = lower(?)");
            statement.setString(1, director);
            statement.setString(2, title);
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
     * @param title
     * @param rating
     */
    public void updateRating(String title, Float rating){
        Connection connection = connectToDatabase();
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE movie_table SET RATING = ? WHERE lower(TITLE) = lower(?)");
            statement.setFloat(1, rating);
            statement.setString(2, title);
            statement.executeUpdate();
            statement.close();
            connection.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            System.out.println(sqle.getClass().getName()+": "+sqle.getMessage());
        }
    }

    Map<String, Movie> getMovieMapFromResultSet(ResultSet resultSet) throws SQLException {
        Map<String, Movie> movieMap = new HashMap<>();
        while (resultSet.next()) {
            Movie movie = new Movie();
            movie.setDirector(Optional.ofNullable(resultSet.getString("DIRECTOR")));
            movie.setRating(Optional.ofNullable(resultSet.getFloat("RATING")));
            movieMap.put(resultSet.getString("TITLE"), movie);
        }
        return movieMap;
    }
}
