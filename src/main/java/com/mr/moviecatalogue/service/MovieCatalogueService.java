package com.mr.moviecatalogue.service;

import com.mr.moviecatalogue.domain.Catalogue;
import com.mr.moviecatalogue.domain.Movie;
import com.mr.moviecatalogue.inputobject.MovieIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 *
 */
@Component
public class MovieCatalogueService {

    @Autowired
    DatabaseService databaseService;

    private static final int RATING_DECIMAL_PLACES = 1;

    //Floats cannot be stored as null in SQL, so stored as -1.0 if rating is
    //not present after eliminating ratings outside the acceptable range.
    private BiConsumer<String,Movie> handleNullRatings = (str,mov) -> {
        if (new Float(-1.0).equals(mov.getRating().get())) {
            mov.setRating(Optional.empty());
        }
    };

    /**
     *
     * @return
     */
    public Catalogue getCurrentCatalogue(){
        Catalogue catalogue = new Catalogue();
        Map<String,Movie> movieMap = databaseService.getAllMovies();
        movieMap.forEach(handleNullRatings);
        catalogue.setMovies(movieMap);
        return catalogue;
    }

    /**
     *
     * @param movieIO
     * @throws IllegalArgumentException
     */
    public void addMovie(MovieIO movieIO) throws IllegalArgumentException {
        Float rating = movieIO.getRating();
        if (rating != null) {
            checkRatingIsWithinRange(rating);
            movieIO.setRating(roundRating(rating, RATING_DECIMAL_PLACES));
        } else {
            movieIO.setRating(new Float(-1.0));
        }
        databaseService.addMovie(movieIO);
    }

    /**
     *
     * @param movieIO
     * @throws IllegalArgumentException
     */
    public void editMovie(String title, MovieIO movieIO) throws IllegalArgumentException {
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
     *
     * @param director
     * @return
     */
    public Catalogue getMoviesByDirector(String director){
        Catalogue returnCatalogue = new Catalogue();
        Map<String, Movie> movieMap = databaseService.getMoviesByDirector(director);
        movieMap.forEach(handleNullRatings);
        returnCatalogue.setMovies(movieMap);
        return returnCatalogue;
    }

    /**
     *
     * @param rating
     * @return
     */
    public Catalogue getMoviesAboveRating(Float rating) throws IllegalArgumentException {
        checkRatingIsWithinRange(rating);
        Float roundedRating = roundRating(rating, RATING_DECIMAL_PLACES);
        Catalogue returnCatalogue = new Catalogue();

        returnCatalogue.setMovies(databaseService.getMoviesAboveRating(roundedRating));
        return returnCatalogue;
    }

    /**
     * Check rating is within the allowed range (0.0 to 5.0)
     * @param rating
     */
    private void checkRatingIsWithinRange(Float rating) throws IllegalArgumentException {
        if (rating < 0.0 || rating > 5.0) {
            throw new IllegalArgumentException("The rating given was outside of the acceptable range. Please use ratings within 0.0 - 5.0");
        }
    }

    /**
     * Round to certain number of decimals
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
