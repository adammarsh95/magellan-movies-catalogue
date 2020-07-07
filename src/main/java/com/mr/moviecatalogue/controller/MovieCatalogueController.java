package com.mr.moviecatalogue.controller;

import com.mr.moviecatalogue.domain.Catalogue;
import com.mr.moviecatalogue.inputobject.MovieIO;
import com.mr.moviecatalogue.service.MovieCatalogueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class MovieCatalogueController {

    @Autowired
    MovieCatalogueService movieCatalogueService;

    @GetMapping("/moviecatalogue/catalogue")
    public Catalogue getCatalogue(HttpServletRequest request){
        return movieCatalogueService.getCurrentCatalogue(request);
    }

    @PostMapping("moviecatalogue/addmovie")
    public ResponseEntity<HttpStatus> addMovie(@RequestBody MovieIO movieIO, HttpServletRequest request){
        if (movieIO == null || movieIO.getTitle() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        movieCatalogueService.addMovie(movieIO, request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
