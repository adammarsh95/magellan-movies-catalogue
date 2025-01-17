package com.mr.moviecatalogue.service;

import com.mr.moviecatalogue.domain.Movie;
import com.mr.moviecatalogue.inputobject.MovieIO;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;

/**
 * This service class handles all of the application's interactions with the database
 */
@Component
public class DatabaseService {

    private static final String DB_USER = "postgres";
    private static final String DB_PW = "postgrespw";

    /**
     * This method attempts to connect to the database for the project, and if it is not already present,
     * creates the database and tables in the PostgreSQL instance
     * @return Returns the Connection to the database
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
     * Returns all movies in the database in a Map
     * @return A HashMap with all the movies in the database, keyed by their title
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
     * Adds the given movie to the database
     * @param movieIO MovieIO containing the mandatory title primary key and optional director and rating values
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
     * @param title Title to search the database for
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
     * Gets all the movies from the database directed by the given director. Allows the use of
     * * or % allow partial queries e.g. Ben* will return all movies for all directors with the
     * first name Ben. The input string is not case sensitive.
     * @param director Director name to be searched for. Can contain wildcard characters * or % e.g. Ben* or Ben% will return all movies for all directors starting with Ben
     * @return A HashMap containing all the movies by the given director, keyed by title
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
     * Gets all the movies from the database with a rating value above or equal to the given
     * rating
     * @param rating Rating to be searched for all movies above this rating.
     * @return A HashMap containing all the movies above the given rating, keyed by title
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
     * Gets all the movies from the database directed by the given director and above the given
     * rating. Allows the use of * or % to allow partial queries e.g. Ben* will return all movies
     * for all directors with the first name Ben. The input string is not case sensitive. The
     * movies returned are all above or equal to the given rating.
     * @param rating Rating to be searched for all movies above this rating.
     * @param director Director name to be searched for. Can contain wildcard characters * or % e.g. Ben* or Ben% will return all movies for all directors starting with Ben
     * @return A HashMap containing all the movies in the database by the given director and above the given rating, keyed by title.
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
     * Updates the title of the movie in the database.
     * @param currentTitle current title of movie to be updated
     * @param newTitle new title to be set
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
     * Updates the director of the movie in the database for the given title.
     * @param title title of movie to be updated
     * @param director Director name to be set
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
     * Updates the rating of the movie in the database for the given title.
     * @param title title of movie to be updated
     * @param rating rating to be set
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

    /**
     * Deletes the movie from the database for the given title. Title is case insensitive.
     * @param title title of movie to be deleted
     */
    public void deleteMovie(String title){
        Connection connection = connectToDatabase();
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("DELETE FROM movie_table WHERE LOWER(TITLE) = LOWER(?)");
            statement.setString(1, title);
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
     * Sets the director column to null for all movies with the given director. Case insensitive.
     * @param director director to be deleted from all movies they are currently set in
     */
    public void deleteDirector(String director){
        Connection connection = connectToDatabase();
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("UPDATE movie_table SET DIRECTOR = ? WHERE lower(DIRECTOR) = lower(?)");
            statement.setString(1, null);
            statement.setString(2, director);
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
