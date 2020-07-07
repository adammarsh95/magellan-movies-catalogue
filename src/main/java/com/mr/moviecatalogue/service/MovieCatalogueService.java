package com.mr.moviecatalogue.service;

import com.mr.moviecatalogue.domain.Catalogue;
import com.mr.moviecatalogue.domain.Movie;
import com.mr.moviecatalogue.inputobject.MovieIO;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Optional;

@Component
public class MovieCatalogueService {

    private static final String SESSION_KEY = "Catalogue";

    public Catalogue getCurrentCatalogue(HttpServletRequest request){
        Catalogue catalogue = (Catalogue) request.getSession().getAttribute(SESSION_KEY);
        if (catalogue == null) {
            catalogue = new Catalogue();
            catalogue.setMovies(new HashMap<>());
        }
        return catalogue;
    }

    public void addMovie(MovieIO movieIO, HttpServletRequest request) {
        Catalogue catalogue = getCurrentCatalogue(request);
        catalogue.getMovies().putIfAbsent(movieIO.getTitle().toUpperCase(), new Movie(Optional.ofNullable(movieIO.getDirector()), Optional.ofNullable(movieIO.getRating())));
        request.getSession().setAttribute(SESSION_KEY, catalogue);
    }

}
