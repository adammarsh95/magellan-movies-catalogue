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
     * @return Returns the movie catalogue
     */
    @GetMapping("/movies")
    public ResponseEntity<Catalogue> getCatalogue(){
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

    @GetMapping("/movies/director")
    public ResponseEntity<Catalogue> getMoviesByDirector(@RequestParam(value = "director") String director){
        if (director == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(movieCatalogueService.getMoviesByDirector(director), HttpStatus.OK);
    }

    @GetMapping("/movies/aboverating")
    public ResponseEntity<Catalogue> getMoviesAboveRating(@RequestParam(value = "rating") String ratingString){
        try {
            Float rating = Float.parseFloat(ratingString);
            return new ResponseEntity<>(movieCatalogueService.getMoviesAboveRating(rating), HttpStatus.OK);
        } catch (NullPointerException npe) {
            System.out.println("No rating provided in request parameter");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (NumberFormatException e) {
            System.out.println("Invalid request parameter provided for rating");
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/movies")
    public void clearCatalogue(){
        databaseService.dropDatabase();
    }
}
