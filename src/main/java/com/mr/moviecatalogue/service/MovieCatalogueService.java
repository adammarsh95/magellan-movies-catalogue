package com.mr.moviecatalogue.service;

import com.mr.moviecatalogue.domain.Catalogue;
import com.mr.moviecatalogue.domain.Movie;
import com.mr.moviecatalogue.inputobject.DirectorIO;
import com.mr.moviecatalogue.inputobject.MovieIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * This Service class handles the logic of the catalogue application, ensuring
 * ratings stored in the database are valid and processing null ratings which
 * cannot be stored in the SQL database, and also rounding all ratings to one
 * decimal place (always rounding down).
 */
@Component
public class MovieCatalogueService {

    @Autowired
    DatabaseService databaseService;

    private static final int RATING_DECIMAL_PLACES = 1;

    //Floats cannot be stored as null in SQL, so stored as -1.0 if rating is
    //not present after eliminating ratings outside the acceptable range.
    private BiConsumer<String,Movie> handleNullRatings = (str,mov) -> {
        if (Float.valueOf((float) -1.0).equals(mov.getRating().get())) {
            mov.setRating(Optional.empty());
        }
    };

    /**
     * Calls the database and returns a Catalogue containing all the movies stored in the database
     * @return Returns the full current catalogue of movies
     */
    public Catalogue getCurrentCatalogue(){
        Catalogue catalogue = new Catalogue();
        Map<String,Movie> movieMap = databaseService.getAllMovies();
        movieMap.forEach(handleNullRatings);
        catalogue.setMovies(movieMap);
        return catalogue;
    }

    /**
     * Throws an IllegalArgumentException for ratings outside the range of 0.0 - 5.0 and
     * rounds the given rating to one decimal place, or sets the rating to -1.0 if the rating
     * in the MovieIO is null, and calls the database service to store the movie in the database
     * @param movieIO MovieIO containing mandatory title field and optional director and rating fields
     * @throws IllegalArgumentException if rating is outside of acceptable range
     */
    public void addMovie(MovieIO movieIO){
        Float rating = movieIO.getRating();
        if (rating != null) {
            checkRatingIsWithinRange(rating);
            movieIO.setRating(roundRating(rating, RATING_DECIMAL_PLACES));
        } else {
            movieIO.setRating(Float.valueOf((float) -1.0));
        }
        databaseService.addMovie(movieIO);
    }

    /**
     * Adds the director to all the given movies in the DirectorIO argument. First checks
     * the full list of titles to ensure they are all in the database, throwing an
     * IllegalArgumentException if one is not, and then calls the database service to update
     * the director for each movie in the database
     * @param directorIO DirectorIO containing the name of the director to add and a list of movie titles to add the director to
     * @throws IllegalArgumentException Thrown if any of the movies in the DirectorIO are not present in the database
     */
    public void addDirector(DirectorIO directorIO){
        directorIO.getMovies().forEach(title -> {
            Movie movie = databaseService.getMovieByTitle(title);
            if (movie == null) {
                throw new IllegalArgumentException(String.format("No movie found to edit for the title given: %s", title));
            }
        });

        //In separate forEach block to avoid some movies having director set and then
        //IllegalArgumentException being thrown partway through the list
        directorIO.getMovies().forEach(title -> databaseService.updateDirector(title, directorIO.getName()));
    }

    /**
     * Verifies that there is a movie in the database with the given title to edit. Then conditionally
     * updates the movie in the database based on which fields are set in the MovieIO and whether they
     * already match the values stored in the database for the given movie. Throws an IllegalArgumentException
     * for ratings outside the range of 0.0 - 5.0, and rounds the given rating to one decimal place. Also
     * throws an IllegalArgumentException if there is no movie present in the database with the given title.
     * @param title Current title of movie to be edited
     * @param movieIO MovieIO containing optional fields, which if set, will be updated in the given movie
     * @throws IllegalArgumentException if rating is outside of acceptable range, or if movie is not present in database for given title
     */
    public void editMovie(String title, MovieIO movieIO)  {
        Movie movie = databaseService.getMovieByTitle(title);
        if (movie == null) {
            throw new IllegalArgumentException("No movie found to edit for the title given");
        }

        Float rating = movieIO.getRating();
        if (rating != null) {
            checkRatingIsWithinRange(rating);
            if (!movie.getRating().isPresent() || !rating.equals(movie.getRating().get())) {
                databaseService.updateRating(title, roundRating(movieIO.getRating(), RATING_DECIMAL_PLACES));
            }
        }

        if (movieIO.getDirector() != null && (!movie.getDirector().isPresent() || !movieIO.getDirector().equals(movie.getDirector().get()))) {
            databaseService.updateDirector(title, movieIO.getDirector());
        }

        if (movieIO.getTitle() != null && !movieIO.getTitle().equals(title)){
            databaseService.updateTitle(title, movieIO.getTitle());
        }
    }

    /**
     * Calls the database service to search for movies with the given director and returns them in a Catalogue
     * @param director Name of director to be searched for
     * @return A Catalogue with the movies with the given director
     */
    public Catalogue getMoviesByDirector(String director){
        Catalogue returnCatalogue = new Catalogue();
        Map<String, Movie> movieMap = databaseService.getMoviesByDirector(director);
        movieMap.forEach(handleNullRatings);
        returnCatalogue.setMovies(movieMap);
        return returnCatalogue;
    }

    /**
     * Checks the given rating is within the acceptable range of 0.0-5.0 and throws an IllegalArgumentException
     * if not. Then rounds the rating to 1 decimal place, and calls the database service to search for movies
     * with the given rating and returns them in a Catalogue
     * @param rating Rating above which movies will be searched for in database. Must be within range 0.0-5.0 and will be rounded down to 1 decimal place
     * @throws IllegalArgumentException if rating is outside of acceptable range
     * @return A Catalogue containing all the movies with a rating equal to or above the given rating
     */
    public Catalogue getMoviesAboveRating(Float rating)  {
        checkRatingIsWithinRange(rating);
        Float roundedRating = roundRating(rating, RATING_DECIMAL_PLACES);
        Catalogue returnCatalogue = new Catalogue();

        returnCatalogue.setMovies(databaseService.getMoviesAboveRating(roundedRating));
        return returnCatalogue;
    }

    /**
     * Calls the database service to get the movie with the given title from the database
     * and sets it in the Catalogue if there is a movie with the title. Otherwise, sets an
     * empty HashMap in the Catalogue and returns it.
     * @param title Title of the movie to be retrieved from Database
     * @return A Catalogue containing the movie with the given title
     */
    public Catalogue getMovieByTitle(String title) {
        Movie movie = databaseService.getMovieByTitle(title);
        Catalogue catalogue = new Catalogue();
        catalogue.setMovies(new HashMap<>());
        if (movie != null) {
            catalogue.getMovies().put(title, movie);
            catalogue.getMovies().forEach(handleNullRatings);
        }
        return catalogue;
    }

    /**
     * This method throws an IllegalArgumentException for ratings outside the range of 0.0 - 5.0,
     * rounds the given rating to one decimal place and then calls the database service with the
     * given director and rating
     * @param director Name of director to be filtered by
     * @param rating Rating to filter for movies above. Must be between 0.0-5.0 and will be rounded down to 1 decimal place
     * @throws IllegalArgumentException if rating is outside of acceptable range
     * @return A Catalogue containing all movies by the given director above the given rating
     */
    public Catalogue getMoviesByDirectorAboveRating(String director, Float rating)  {
        checkRatingIsWithinRange(rating);
        Float roundedRating = roundRating(rating, RATING_DECIMAL_PLACES);
        Catalogue returnCatalogue = new Catalogue();

        returnCatalogue.setMovies(databaseService.getMoviesByDirectorAboveRating(director, roundedRating));
        return returnCatalogue;
    }

    /**
     * Calls the database service to see if there is a movie stored for the given title,
     * and if so, calls the database service to delete the director for it
     * @param title Title of the movie to have its director deleted
     * @throws IllegalArgumentException if movie is not returned from database for given title
     */
    public void deleteDirectorFromMovie(String title){
        Movie movie = databaseService.getMovieByTitle(title);
        if (movie == null) {
            throw new IllegalArgumentException("No movie found to edit for the title given");
        }

        if (movie.getDirector().isPresent()) {
            databaseService.updateDirector(title, null);
        }
    }

    /**
     * Calls the database service to see if there is a movie stored for the given title,
     * and if so, calls the database service to update the rating to -1.0 which is being
     * used for null ratings in the database
     * @param title Title of the movie to have its rating deleted
     * @throws IllegalArgumentException if movie is not returned from database for given title
     */
    public void deleteRatingFromMovie(String title){
        Movie movie = databaseService.getMovieByTitle(title);
        if (movie == null) {
            throw new IllegalArgumentException("No movie found to edit for the title given");
        }

        if (movie.getRating().isPresent() && !movie.getRating().get().equals(Float.valueOf((float) -1.0))) {
            databaseService.updateRating(title, Float.valueOf((float) -1.0));
        }
    }

    /**
     * Calls the database service to see if there is a movie stored for the given title,
     * and if so, calls the database service to delete the movie
     * @param title Title of the movie to be deleted
     * @throws IllegalArgumentException if movie is not returned from database for given title
     */
    public void deleteMovie(String title){
        Movie movie = databaseService.getMovieByTitle(title);
        if (movie == null) {
            throw new IllegalArgumentException("No movie found to edit for the title given");
        }

        databaseService.deleteMovie(title);
    }

    /**
     * Calls database service to delete the director value for all movies with the given director
     * @param director Name of database to be deleted from database
     */
    public void deleteDirector(String director){
        databaseService.deleteDirector(director);
    }

    /**
     * Check rating is within the allowed range (0.0 to 5.0)
     * @param rating rating to be validated
     * @throws IllegalArgumentException when rating is outside range 0.0 - 5.0
     */
    private void checkRatingIsWithinRange(Float rating)  {
        if (rating < 0.0 || rating > 5.0) {
            throw new IllegalArgumentException("The rating given was outside of the acceptable range. Please use ratings within 0.0 - 5.0");
        }
    }

    /**
     * Round to given number of decimals
     * @param d Float to be rounded
     * @param decimalPlace number of decimal places to round to
     * @return d rounded to specified number of decimal places
     */
    private Float roundRating(Float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_DOWN);
        return bd.floatValue();
    }

}
