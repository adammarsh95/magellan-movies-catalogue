package com.mr.moviecatalogue.controller;

import com.mr.moviecatalogue.domain.Catalogue;
import com.mr.moviecatalogue.domain.Movie;
import com.mr.moviecatalogue.service.MovieCatalogueService;
import org.junit.Before;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
public class MovieCatalogueControllerTest {

    @Mock
    MovieCatalogueService service;

    @InjectMocks
    MovieCatalogueController controller;

    Catalogue catalogue;

    @Before
    public void setup(){
        catalogue = new Catalogue();
        catalogue.setMovies(new HashMap<>());
        catalogue.getMovies().put("Tropic Thunder", new Movie(Optional.of("Ben Stiller"), Optional.of(Float.valueOf((float) 5.0))));
    }

    @Test
    public void test_get_movies(){

    }

}
