package com.mr.moviecatalogue.controller;

import com.mr.moviecatalogue.domain.Catalogue;
import com.mr.moviecatalogue.inputobject.MovieIO;
import com.mr.moviecatalogue.service.DatabaseService;
import com.mr.moviecatalogue.service.MovieCatalogueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
public class MovieCatalogueController {

    @Autowired
    MovieCatalogueService movieCatalogueService;

    @Autowired
    DatabaseService databaseService;

    @GetMapping("/moviecatalogue/catalogue")
    public ResponseEntity<Catalogue> getCatalogue(HttpServletRequest request){
        return new ResponseEntity<>(movieCatalogueService.getCurrentCatalogue(), HttpStatus.OK);
    }

    @PostMapping("moviecatalogue/movie")
    public ResponseEntity<HttpStatus> addMovie(@RequestBody MovieIO movieIO, HttpServletRequest request){
        if (movieIO == null || movieIO.getTitle() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        movieCatalogueService.addMovie(movieIO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("moviecatalogue/director")
    public ResponseEntity<Catalogue> getMoviesByDirector(@RequestParam(value = "director") String director, HttpServletRequest request){
        if (director == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(movieCatalogueService.getMoviesByDirector(director), HttpStatus.OK);
    }

    @DeleteMapping("moviecatalogue")
    public void clearCatalogue(){
        databaseService.dropDatabase();
    }
}
