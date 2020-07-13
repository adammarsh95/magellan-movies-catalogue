package com.mr.moviecatalogue.service;

import com.mr.moviecatalogue.domain.Movie;
import com.mr.moviecatalogue.inputobject.MovieIO;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;

/**
 *
 */
@Component
public class DatabaseService {

    private static final String DB_USER = "postgres";
    private static final String DB_PW = "postgrespw";

    /**
     * This method attempts to connect to the database for the project, and if it is not already present,
     * creates the database and tables in the PostgreSQL instance
     */
    public Connection connectToDatabase(){
        Connection c = null;
        Statement statement = null;
        Statement createTables = null;
        try {
            c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/moviedb",
                            DB_USER, DB_PW);
        } catch (Exception e) {
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            try {
                c = DriverManager
                        .getConnection("jdbc:postgresql://localhost:5432/",
                                DB_USER, DB_PW);
                statement = c.createStatement();
                statement.executeUpdate("CREATE DATABASE moviedb");
                statement.close();
                c.close();

                c = DriverManager
                        .getConnection("jdbc:postgresql://localhost:5432/moviedb",
                                DB_USER, DB_PW);

                createTables = c.createStatement();
                createTables.executeUpdate("CREATE TABLE movie_table (TITLE TEXT PRIMARY KEY NOT NULL, DIRECTOR TEXT, RATING FLOAT)");
                createTables.close();
                System.out.println("Table created");
            } catch (Exception exception) {
                exception.printStackTrace();
                System.err.println(exception.getClass().getName()+": "+exception.getMessage());
                System.out.println("Database could not be accessed, exiting application");
                System.exit(0);
            }
        } finally {
            try {
                if (statement != null && !statement.isClosed()) {
                    statement.close();
                }
                if (createTables != null && !createTables.isClosed()) {
                    createTables.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getClass().getName()+": "+e.getMessage());
            }
        }

        System.out.println("Opened database successfully");
        return c;
    }

    /**
     * Drops the database from the SQL server, clearing any data.
     */
    public void dropDatabase(){
        Connection connection = null;
        Statement statement = null;
        try {
//            Class.forName("org.postgresql.Driver");
            connection = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/",
                            DB_USER, DB_PW);
            statement = connection.createStatement();
            statement.executeUpdate("DROP DATABASE IF EXISTS moviedb");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getClass().getName()+": "+e.getMessage());
        }  finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getClass().getName()+": "+e.getMessage());
            }
        }
    }

    /**
     *
     * @return
     */
    public Map<String, Movie> getAllMovies() {
        Connection connection = connectToDatabase();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement("SELECT * FROM movie_table;");
            resultSet = statement.executeQuery();
            return getMovieMapFromResultSet(resultSet);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            System.out.println(sqle.getClass().getName()+": "+sqle.getMessage());
            return null;
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                connection.close();

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getClass().getName()+": "+e.getMessage());
            }
        }
    }

    /**
     *
     * @return
     */
    public void addMovie(MovieIO movieIO){
        Connection connection = connectToDatabase();
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("INSERT INTO movie_table (TITLE,DIRECTOR,RATING) VALUES (?,?,?);");
            statement.setString(1, movieIO.getTitle());
            statement.setString(2, movieIO.getDirector());
            statement.setFloat(3, movieIO.getRating());
            statement.executeUpdate();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            System.out.println(sqle.getClass().getName()+": "+sqle.getMessage());
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getClass().getName()+": "+e.getMessage());
            }
        }
    }

    /**
     * Gets a movie from the database by searching for the provided title. This parameter is case sensitive.
     * @param title
     * @return Returns a Movie object for the provided title
     */
    public Movie getMovieByTitle(String title){
        Connection connection = connectToDatabase();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement("SELECT * FROM movie_table WHERE TITLE = ?;");
            statement.setString(1, title);
            resultSet = statement.executeQuery();
            Movie movie = null;
            while (resultSet.next()) {
                movie = new Movie();
                movie.setDirector(Optional.ofNullable(resultSet.getString("DIRECTOR")));
                movie.setRating(Optional.ofNullable(resultSet.getFloat("RATING")));
            }
            return movie;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            System.out.println(sqle.getClass().getName()+": "+sqle.getMessage());
            return null;
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getClass().getName()+": "+e.getMessage());
            }
        }
    }

    /**
     *
     * @param director
     * @return
     */
    public Map<String, Movie> getMoviesByDirector(String director){
        Connection connection = connectToDatabase();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            director = director.replaceAll("\\*","%");
            statement = connection.prepareStatement("SELECT * FROM movie_table WHERE LOWER(DIRECTOR) LIKE LOWER(?);");
            statement.setString(1, director);
            resultSet = statement.executeQuery();
            return getMovieMapFromResultSet(resultSet);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            System.out.println(sqle.getClass().getName()+": "+sqle.getMessage());
            return null;
        }  finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getClass().getName()+": "+e.getMessage());
            }
        }
    }

    /**
     *
     * @param rating
     * @return
     */
    public Map<String, Movie> getMoviesAboveRating(Float rating){
        Connection connection = connectToDatabase();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement("SELECT * FROM movie_table WHERE RATING >= ?;");
            statement.setFloat(1, rating);
            resultSet = statement.executeQuery();
            return getMovieMapFromResultSet(resultSet);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            System.out.println(sqle.getClass().getName()+": "+sqle.getMessage());
            return null;
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getClass().getName()+": "+e.getMessage());
            }
        }
    }

    /**
     *
     * @param rating
     * @return
     */
    public Map<String, Movie> getMoviesByDirectorAboveRating(String director, Float rating){
        Connection connection = connectToDatabase();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            director = director.replaceAll("\\*","%");
            statement = connection.prepareStatement("SELECT * FROM movie_table WHERE LOWER(DIRECTOR) LIKE LOWER(?) AND RATING >= ?;");
            statement.setString(1, director);
            statement.setFloat(2, rating);
            resultSet = statement.executeQuery();
            return getMovieMapFromResultSet(resultSet);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            System.out.println(sqle.getClass().getName()+": "+sqle.getMessage());
            return null;
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getClass().getName()+": "+e.getMessage());
            }
        }
    }

    /**
     *
     * @param currentTitle
     * @param newTitle
     */
    public void updateTitle(String currentTitle, String newTitle){
        Connection connection = connectToDatabase();
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("UPDATE movie_table SET TITLE = ? WHERE lower(TITLE) = lower(?)");
            statement.setString(1, newTitle);
            statement.setString(2, currentTitle);
            statement.executeUpdate();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            System.out.println(sqle.getClass().getName()+": "+sqle.getMessage());
        }  finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getClass().getName()+": "+e.getMessage());
            }
        }
    }

    /**
     *
     * @param title
     * @param director
     */
    public void updateDirector(String title, String director){
        Connection connection = connectToDatabase();
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("UPDATE movie_table SET DIRECTOR = ? WHERE lower(TITLE) = lower(?)");
            statement.setString(1, director);
            statement.setString(2, title);
            statement.executeUpdate();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            System.out.println(sqle.getClass().getName()+": "+sqle.getMessage());
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getClass().getName()+": "+e.getMessage());
            }
        }
    }

    /**
     *
     * @param title
     * @param rating
     */
    public void updateRating(String title, Float rating){
        Connection connection = connectToDatabase();
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("UPDATE movie_table SET RATING = ? WHERE lower(TITLE) = lower(?)");
            statement.setFloat(1, rating);
            statement.setString(2, title);
            statement.executeUpdate();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            System.out.println(sqle.getClass().getName()+": "+sqle.getMessage());
        }  finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getClass().getName()+": "+e.getMessage());
            }
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
