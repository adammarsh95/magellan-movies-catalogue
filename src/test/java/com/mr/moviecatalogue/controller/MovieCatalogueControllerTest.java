package com.mr.moviecatalogue.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mr.moviecatalogue.domain.Catalogue;
import com.mr.moviecatalogue.domain.Movie;
import com.mr.moviecatalogue.inputobject.DirectorIO;
import com.mr.moviecatalogue.inputobject.MovieIO;
import com.mr.moviecatalogue.service.DatabaseService;
import com.mr.moviecatalogue.service.MovieCatalogueService;
import org.junit.Before;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(MovieCatalogueController.class)
public class MovieCatalogueControllerTest {

    @MockBean
    MovieCatalogueService service;

    @MockBean
    DatabaseService database;

    @Autowired
    MockMvc mvc;

    private Catalogue serviceResponse;
    private MovieIO movieIO;

    @Before
    public void setup(){
        serviceResponse = new Catalogue();
        serviceResponse.setMovies(new HashMap<>());
        serviceResponse.getMovies().put("Hot Fuzz", new Movie(Optional.of("Edgar Wright"), Optional.of(Float.valueOf((float) 5.0))));
        serviceResponse.getMovies().put("Shaun of the Dead", new Movie(Optional.of("Edgar Wright"), Optional.of(Float.valueOf((float) 4.5))));

        movieIO = new MovieIO();
    }

    @Test
    public void test_get_movies_no_params(){
        Mockito.when(service.getCurrentCatalogue()).thenReturn(serviceResponse);
        try {
            MvcResult response = mvc.perform(MockMvcRequestBuilders.get("/movies"))
                    .andExpect(status().isOk()).andReturn();
            Catalogue responseCatalogue = Jackson2ObjectMapperBuilder.json().build().readerFor(Catalogue.class).readValue(response.getResponse().getContentAsString());
            assertEquals(serviceResponse, responseCatalogue);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_get_movies_title_param_calls_correct_service(){
        String title = "Hot Fuzz";
        serviceResponse.getMovies().remove("Shaun of the Dead");
        Mockito.when(service.getMovieByTitle(title)).thenReturn(serviceResponse);
        try {
            MvcResult response = mvc.perform(MockMvcRequestBuilders.get("/movies").param("title", title))
                    .andExpect(status().isOk()).andReturn();
            Catalogue responseCatalogue = Jackson2ObjectMapperBuilder.json().build().readerFor(Catalogue.class).readValue(response.getResponse().getContentAsString());
            assertEquals(serviceResponse, responseCatalogue);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_get_movies_title_param_ignores_other_params(){
        String title = "Hot Fuzz";
        String director = "Edgar Wright";
        String ratingString = "4.5";
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("title", Arrays.asList(title));
        queryParams.put("director", Arrays.asList(director));
        queryParams.put("rating", Arrays.asList(ratingString));
        serviceResponse.getMovies().remove("Shaun of the Dead");
        Mockito.when(service.getMovieByTitle(title)).thenReturn(serviceResponse);
        try {
            MvcResult response = mvc.perform(MockMvcRequestBuilders.get("/movies").params(queryParams))
                    .andExpect(status().isOk()).andReturn();
            Catalogue responseCatalogue = Jackson2ObjectMapperBuilder.json().build().readerFor(Catalogue.class).readValue(response.getResponse().getContentAsString());
            assertEquals(serviceResponse, responseCatalogue);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_get_movies_director_param_calls_correct_service(){
        String director = "Edgar Wright";
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("director", Arrays.asList(director));
        Mockito.when(service.getMoviesByDirector(director)).thenReturn(serviceResponse);
        try {
            MvcResult response = mvc.perform(MockMvcRequestBuilders.get("/movies").params(queryParams))
                    .andExpect(status().isOk()).andReturn();
            Catalogue responseCatalogue = Jackson2ObjectMapperBuilder.json().build().readerFor(Catalogue.class).readValue(response.getResponse().getContentAsString());
            assertEquals(serviceResponse, responseCatalogue);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_get_movies_rating_param_calls_correct_service(){
        String ratingString = "4.0";
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("rating", Arrays.asList(ratingString));
        Mockito.when(service.getMoviesAboveRating(Float.valueOf(ratingString))).thenReturn(serviceResponse);
        try {
            MvcResult response = mvc.perform(MockMvcRequestBuilders.get("/movies").params(queryParams))
                    .andExpect(status().isOk()).andReturn();
            Catalogue responseCatalogue = Jackson2ObjectMapperBuilder.json().build().readerFor(Catalogue.class).readValue(response.getResponse().getContentAsString());
            assertEquals(serviceResponse, responseCatalogue);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_get_movies_rating_and_director_params_calls_correct_service(){
        String director = "Edgar Wright";
        String ratingString = "4.5";
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("director", Arrays.asList(director));
        queryParams.put("rating", Arrays.asList(ratingString));
        Mockito.when(service.getMoviesByDirectorAboveRating(director, Float.valueOf(ratingString))).thenReturn(serviceResponse);
        try {
            MvcResult response = mvc.perform(MockMvcRequestBuilders.get("/movies").params(queryParams))
                    .andExpect(status().isOk()).andReturn();
            Catalogue responseCatalogue = Jackson2ObjectMapperBuilder.json().build().readerFor(Catalogue.class).readValue(response.getResponse().getContentAsString());
            assertEquals(serviceResponse, responseCatalogue);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_get_movies_rating_param_returns_bad_request_for_number_format_exception(){
        String ratingString = "Sgt. Lincoln Osiris";
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("rating", Arrays.asList(ratingString));
        try {
            mvc.perform(MockMvcRequestBuilders.get("/movies").params(queryParams))
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_get_movies_rating_param_returns_bad_request_for_illegal_argument_exception(){
        String ratingString = "51";
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("rating", Arrays.asList(ratingString));
        Mockito.when(service.getMoviesAboveRating(Float.valueOf(ratingString))).thenThrow(IllegalArgumentException.class);
        try {
            mvc.perform(MockMvcRequestBuilders.get("/movies").params(queryParams))
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_get_movies_rating_and_director_param_returns_bad_request_for_number_format_exception(){
        String ratingString = "Sgt. Lincoln Osiris";
        String director = "Edgar Wright";
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("rating", Arrays.asList(ratingString));
        queryParams.put("director", Arrays.asList(director));
        try {
            mvc.perform(MockMvcRequestBuilders.get("/movies").params(queryParams))
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_get_movies_rating_and_director_param_returns_bad_request_for_illegal_argument_exception(){
        String ratingString = "51";
        String director = "Edgar Wright";
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("rating", Arrays.asList(ratingString));
        queryParams.put("director", Arrays.asList(director));
        Mockito.when(service.getMoviesByDirectorAboveRating(director, Float.valueOf(ratingString))).thenThrow(IllegalArgumentException.class);
        try {
            mvc.perform(MockMvcRequestBuilders.get("/movies").params(queryParams))
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_add_movie_returns_bad_request_for_null_body(){
        try {
            mvc.perform(MockMvcRequestBuilders.post("/movies").contentType(MediaType.APPLICATION_JSON_VALUE).content(""))
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_add_movie_returns_bad_request_for_no_title_set_in_body(){
        try {
            ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String requestBody = writer.writeValueAsString(movieIO);
            mvc.perform(MockMvcRequestBuilders.post("/movies").contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody))
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_add_movie_returns_bad_request_for_empty_title(){
        movieIO.setTitle("");
        try {
            ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String requestBody = writer.writeValueAsString(movieIO);
            mvc.perform(MockMvcRequestBuilders.post("/movies").contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody))
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_add_movie_calls_add_movie_in_service_for_valid_body(){
        movieIO.setTitle("Tropic Thunder");
        try {
            ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String requestBody = writer.writeValueAsString(movieIO);
            mvc.perform(MockMvcRequestBuilders.post("/movies").contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody))
                    .andExpect(status().isOk());
            Mockito.verify(service, Mockito.times(1)).addMovie(movieIO);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_add_movie_returns_bad_request_when_service_throws_illegal_argument_exception(){
        Mockito.doAnswer(invocation -> {
            throw new IllegalArgumentException();
        }).when(service).addMovie(movieIO);
        movieIO.setTitle("Tropic Thunder");
        try {
            ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String requestBody = writer.writeValueAsString(movieIO);
            mvc.perform(MockMvcRequestBuilders.post("/movies").contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody))
                    .andExpect(status().isBadRequest());
            Mockito.verify(service, Mockito.times(1)).addMovie(movieIO);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_edit_movie_returns_bad_request_when_body_is_empty(){
        try {
            mvc.perform(MockMvcRequestBuilders.patch("/movies/Tropic Thunder").contentType(MediaType.APPLICATION_JSON_VALUE).content(""))
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_edit_movie_returns_bad_request_when_service_throws_illegal_argument_exception(){
        Mockito.doAnswer(invocation -> {
            throw new IllegalArgumentException();
        }).when(service).editMovie("Tropic Thunder", movieIO);
        movieIO.setTitle("Hot Fuzz");
        movieIO.setRating(Float.valueOf("52.0"));
        try {
            ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String requestBody = writer.writeValueAsString(movieIO);
            mvc.perform(MockMvcRequestBuilders.patch("/movies/Tropic Thunder").contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody))
                    .andExpect(status().isBadRequest());
            Mockito.verify(service, Mockito.times(1)).editMovie("Tropic Thunder", movieIO);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_edit_movie_returns_ok_when_body_is_valid(){
        movieIO.setTitle("Hot Fuzz");
        movieIO.setRating(Float.valueOf("5.0"));
        try {
            ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String requestBody = writer.writeValueAsString(movieIO);
            mvc.perform(MockMvcRequestBuilders.patch("/movies/Tropic Thunder").contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody))
                    .andExpect(status().isOk());
            Mockito.verify(service, Mockito.times(1)).editMovie("Tropic Thunder", movieIO);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_clear_catalogue_calls_database_service(){
        try {
            mvc.perform(MockMvcRequestBuilders.delete("/movies")).andExpect(status().isOk());
            Mockito.verify(database, Mockito.times(1)).dropDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_delete_director_from_movie_returns_bad_request_if_service_throws_illegal_argument_exception(){
        Mockito.doAnswer(invocation -> {
            throw new IllegalArgumentException();
        }).when(service).deleteDirectorFromMovie("Tropic Thunder");
        try {
            mvc.perform(MockMvcRequestBuilders.delete("/movies/Tropic Thunder/director")).andExpect(status().isBadRequest());
            Mockito.verify(service, Mockito.times(1)).deleteDirectorFromMovie("Tropic Thunder");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_delete_director_from_movie_calls_service_method(){
        try {
            mvc.perform(MockMvcRequestBuilders.delete("/movies/Tropic Thunder/director")).andExpect(status().isOk());
            Mockito.verify(service, Mockito.times(1)).deleteDirectorFromMovie("Tropic Thunder");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_delete_rating_from_movie_returns_bad_request_if_service_throws_illegal_argument_exception(){
        Mockito.doAnswer(invocation -> {
            throw new IllegalArgumentException();
        }).when(service).deleteRatingFromMovie("Tropic Thunder");
        try {
            mvc.perform(MockMvcRequestBuilders.delete("/movies/Tropic Thunder/rating")).andExpect(status().isBadRequest());
            Mockito.verify(service, Mockito.times(1)).deleteRatingFromMovie("Tropic Thunder");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_delete_rating_from_movie_calls_service_method(){
        try {
            mvc.perform(MockMvcRequestBuilders.delete("/movies/Tropic Thunder/rating")).andExpect(status().isOk());
            Mockito.verify(service, Mockito.times(1)).deleteRatingFromMovie("Tropic Thunder");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_delete_movie_returns_bad_request_if_service_throws_illegal_argument_exception(){
        Mockito.doAnswer(invocation -> {
            throw new IllegalArgumentException();
        }).when(service).deleteMovie("Tropic Thunder");
        try {
            mvc.perform(MockMvcRequestBuilders.delete("/movies/Tropic Thunder")).andExpect(status().isBadRequest());
            Mockito.verify(service, Mockito.times(1)).deleteMovie("Tropic Thunder");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_delete_movie_calls_service_method(){
        try {
            mvc.perform(MockMvcRequestBuilders.delete("/movies/Tropic Thunder")).andExpect(status().isOk());
            Mockito.verify(service, Mockito.times(1)).deleteMovie("Tropic Thunder");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_delete_director_returns_bad_request_if_service_throws_illegal_argument_exception(){
        Mockito.doAnswer(invocation -> {
            throw new IllegalArgumentException();
        }).when(service).deleteDirector("Ben Stiller");
        try {
            mvc.perform(MockMvcRequestBuilders.delete("/movies/directors/Ben Stiller")).andExpect(status().isBadRequest());
            Mockito.verify(service, Mockito.times(1)).deleteDirector("Ben Stiller");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_delete_director_calls_service_method(){
        try {
            mvc.perform(MockMvcRequestBuilders.delete("/movies/directors/Ben Stiller")).andExpect(status().isOk());
            Mockito.verify(service, Mockito.times(1)).deleteDirector("Ben Stiller");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_add_director_returns_bad_request_for_null_body(){
        try {
            mvc.perform(MockMvcRequestBuilders.post("/movies/directors").contentType(MediaType.APPLICATION_JSON_VALUE).content(""))
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_add_director_returns_bad_request_for_no_name_set_in_body(){
        DirectorIO directorIO = new DirectorIO();
        try {
            ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String requestBody = writer.writeValueAsString(directorIO);
            mvc.perform(MockMvcRequestBuilders.post("/movies/directors").contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody))
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_add_director_returns_bad_request_for_empty_name(){
        DirectorIO directorIO = new DirectorIO();
        directorIO.setName("");
        try {
            ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String requestBody = writer.writeValueAsString(directorIO);
            mvc.perform(MockMvcRequestBuilders.post("/movies/directors").contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody))
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_add_director_returns_bad_request_for_no_movie_list(){
        DirectorIO directorIO = new DirectorIO();
        directorIO.setName("Guy Ritchie");
        try {
            ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String requestBody = writer.writeValueAsString(directorIO);
            mvc.perform(MockMvcRequestBuilders.post("/movies/directors").contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody))
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_add_director_returns_bad_request_for_empty_movie_list(){
        DirectorIO directorIO = new DirectorIO();
        directorIO.setName("Guy Ritchie");
        directorIO.setMovies(new ArrayList<>());
        try {
            ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String requestBody = writer.writeValueAsString(directorIO);
            mvc.perform(MockMvcRequestBuilders.post("/movies/directors").contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody))
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_add_add_director_calls_add_director_in_service_for_valid_body(){
        DirectorIO directorIO = new DirectorIO();
        directorIO.setName("Guy Ritchie");
        directorIO.setMovies(Arrays.asList("Snatch", "Lock, Stock and Two Smoking Barrels"));
        try {
            ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String requestBody = writer.writeValueAsString(directorIO);
            mvc.perform(MockMvcRequestBuilders.post("/movies/directors").contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody))
                    .andExpect(status().isOk());
            Mockito.verify(service, Mockito.times(1)).addDirector(directorIO);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_add_director_returns_bad_request_when_service_throws_illegal_argument_exception(){
        DirectorIO directorIO = new DirectorIO();
        directorIO.setName("Guy Ritchie");
        directorIO.setMovies(Arrays.asList("Snatch", "Lock, Stock and Two Smoking Barrels"));
        Mockito.doAnswer(invocation -> {
            throw new IllegalArgumentException();
        }).when(service).addDirector(directorIO);
        try {
            ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String requestBody = writer.writeValueAsString(directorIO);
            mvc.perform(MockMvcRequestBuilders.post("/movies/directors").contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody))
                    .andExpect(status().isBadRequest());
            Mockito.verify(service, Mockito.times(1)).addDirector(directorIO);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }


}
