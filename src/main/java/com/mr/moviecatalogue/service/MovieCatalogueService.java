package com.mr.moviecatalogue.service;

import com.mr.moviecatalogue.domain.Catalogue;
import com.mr.moviecatalogue.domain.Movie;
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
     * @param movieIO
     */
    public void addMovie(MovieIO movieIO)  {
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
     * Verifies that there is a movie in the database with the given title to edit. Then conditionally
     * updates the movie in the database based on which fields are set in the MovieIO and whether they
     * already match the values stored in the database for the given movie. Throws an IllegalArgumentException
     * for ratings outside the range of 0.0 - 5.0, and rounds the given rating to one decimal place. Also
     * throws an IllegalArgumentException if there is no movie present in the database with the given title.
     * @param title
     * @param movieIO
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
     * @param director
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
     * @param rating
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
     * @param title
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
     * @param director
     * @param rating
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
     * @param title
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
     * @param title
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
     * @param title
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
     * @param director
     */
    public void deleteDirector(String director){
        databaseService.deleteDirector(director);
    }

    /**
     * Check rating is within the allowed range (0.0 to 5.0)
     * @param rating
     */
    private void checkRatingIsWithinRange(Float rating)  {
        if (rating < 0.0 || rating > 5.0) {
            throw new IllegalArgumentException("The rating given was outside of the acceptable range. Please use ratings within 0.0 - 5.0");
        }
    }

    /**
     * Round to given number of decimals
     * @param d
     * @param decimalPlace
     * @return
     */
    private Float roundRating(Float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_DOWN);
        return bd.floatValue();
    }

}
