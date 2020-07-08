package com.mr.moviecatalogue.service;

import com.mr.moviecatalogue.domain.Catalogue;
import com.mr.moviecatalogue.inputobject.MovieIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MovieCatalogueService {

    @Autowired
    DatabaseService databaseService;

    public Catalogue getCurrentCatalogue(){
        Catalogue catalogue = new Catalogue();
        catalogue.setMovies(databaseService.getAllMovies());
        return catalogue;
    }

    public void addMovie(MovieIO movieIO) {
        databaseService.addMovie(movieIO);
    }

    public Catalogue getMoviesByDirector(String director){
        Catalogue returnCatalogue = new Catalogue();
        returnCatalogue.setMovies(databaseService.getMoviesByDirector(director));
        return returnCatalogue;
    }

}
