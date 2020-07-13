package com.mr.moviecatalogue.controller;

import com.mr.moviecatalogue.domain.Catalogue;
import com.mr.moviecatalogue.inputobject.MovieIO;
import com.mr.moviecatalogue.service.DatabaseService;
import com.mr.moviecatalogue.service.MovieCatalogueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * RestController class to define URIs, provide validation on requests and then call Service class to perform the logic of the application
 */
@RestController
public class MovieCatalogueController {

    @Autowired
    MovieCatalogueService movieCatalogueService;

    @Autowired
    DatabaseService databaseService;

    /**
     * Calls the service method to return the current movie catalogue
     * @param title
     * @param director
     * @param ratingString
     * @return Returns the movie catalogue
     */
    @GetMapping("/movies")
    public ResponseEntity<Catalogue> getMovies(@RequestParam(required = false, value = "director") final String director,
                                               @RequestParam(required = false, value = "title") final String title,
                                               @RequestParam(required = false, value = "rating") final String ratingString){
        boolean directorNotPresent = director == null || director.equalsIgnoreCase("");
        boolean ratingNotPresent = ratingString == null || ratingString.equalsIgnoreCase("");
        boolean titleNotPresent = title == null || title.equalsIgnoreCase("");
        if (!titleNotPresent) {
            return new ResponseEntity<>(movieCatalogueService.getMovieByTitle(title), HttpStatus.OK);
        } else if (titleNotPresent && ratingNotPresent && !directorNotPresent) {
            return new ResponseEntity<>(movieCatalogueService.getMoviesByDirector(director), HttpStatus.OK);
        } else if (titleNotPresent && directorNotPresent && !ratingNotPresent) {
            try {
                Float rating = Float.parseFloat(ratingString);
                return new ResponseEntity<>(movieCatalogueService.getMoviesAboveRating(rating), HttpStatus.OK);
            } catch (NumberFormatException e) {
                System.out.println("Invalid request parameter provided for rating");
                System.out.println(e.getMessage());
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } else if (titleNotPresent && !ratingNotPresent && !directorNotPresent) {
            try {
                Float rating = Float.parseFloat(ratingString);
                return new ResponseEntity<>(movieCatalogueService.getMoviesByDirectorAboveRating(director, rating), HttpStatus.OK);
            } catch (NumberFormatException e) {
                System.out.println("Invalid request parameter provided for rating");
                System.out.println(e.getMessage());
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>(movieCatalogueService.getCurrentCatalogue(), HttpStatus.OK);
    }

    /**
     * Calls the service method to add a movie to the catalogue. Returns 400 bad request if request body is null or contains no title
     * @param movieIO
     * @return Http status code
     */
    @PostMapping("/movies")
    public ResponseEntity<HttpStatus> addMovie(@RequestBody MovieIO movieIO){
        if (movieIO == null || movieIO.getTitle() == null || movieIO.getTitle().equalsIgnoreCase("")) {
            System.out.println("Movie must be provided with title in request body to add a movie");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            movieCatalogueService.addMovie(movieIO);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Calls the service method to edit a pre-existing movie in the database, identified by the path variable which is the title
     * of the movie to be edited. Returns 400 bad request if request body is null, or title is null or empty string
     * @param movieIO
     * @param title
     * @return Http status code
     */
    @PatchMapping("/movies/{title}")
    public ResponseEntity<HttpStatus> editMovie(@RequestBody MovieIO movieIO, @PathVariable(value = "title") final String title){
        if (movieIO == null || title == null || title.equalsIgnoreCase("")) {
            System.out.println("Movie must be provided with title to edit a movie");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            movieCatalogueService.editMovie(title, movieIO);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Calls the database service to drop the database from the SQL server, clearing
     * the stored data
     * @return Http status code
     */
    @DeleteMapping("/movies")
    public ResponseEntity<HttpStatus> clearCatalogue(){
        databaseService.dropDatabase();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Deletes the director from the movie in the database for the given title
     * @param title
     * @return Http status code
     */
    @DeleteMapping("/movies/{title}/director")
    public ResponseEntity<HttpStatus> deleteDirectorFromMovie(@PathVariable(value = "title") final String title){
        try {
            movieCatalogueService.deleteDirectorFromMovie(title);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Deletes the rating from the movie in the database for the given title
     * @param title
     * @return Http status code
     */
    @DeleteMapping("/movies/{title}/rating")
    public ResponseEntity<HttpStatus> deleteRatingFromMovie(@PathVariable(value = "title") final String title){
        try {
            movieCatalogueService.deleteRatingFromMovie(title);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Deletes the movie from the database for the given title
     * @param title
     * @return Http status code
     */
    @DeleteMapping("/movies/{title}")
    public ResponseEntity<HttpStatus> deleteMovie(@PathVariable(value = "title") final String title){
        try {
            movieCatalogueService.deleteMovie(title);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Deletes the given director from all movies with the given director
     * @param director
     * @return Http status code
     */
    @DeleteMapping("/movies/directors/{director}")
    public ResponseEntity<HttpStatus> deleteDirector(@PathVariable(value = "director") final String director){
        try {
            movieCatalogueService.deleteDirector(director);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
