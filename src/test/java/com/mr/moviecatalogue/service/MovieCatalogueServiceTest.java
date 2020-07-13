package com.mr.moviecatalogue.service;

import com.mr.moviecatalogue.domain.Catalogue;
import com.mr.moviecatalogue.domain.Movie;
import com.mr.moviecatalogue.inputobject.MovieIO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@RunWith(SpringJUnit4ClassRunner.class)
public class MovieCatalogueServiceTest {

    @Mock
    DatabaseService database;

    @InjectMocks
    MovieCatalogueService service;

    private Map<String, Movie> movieMap;

    @Before
    public void setup() {
        movieMap = new HashMap<>();
        movieMap.put("Hot Fuzz", new Movie(Optional.of("Edgar Wright"), Optional.of(Float.valueOf((float) 5.0))));
        movieMap.put("Shaun of the Dead", new Movie(Optional.of("Edgar Wright"), Optional.of(Float.valueOf((float) -1.0))));
        movieMap.put("Tropic Thunder", new Movie(Optional.of("Ben Stiller"), Optional.of(Float.valueOf((float) 5.0))));
    }

    @Test
    public void test_get_current_catalogue_handles_empty_map_from_database() {
        Mockito.when(database.getAllMovies()).thenReturn(new HashMap<>());
        Catalogue catalogue = service.getCurrentCatalogue();
        assertEquals(new HashMap<>(), catalogue.getMovies());
    }

    @Test
    public void test_get_current_catalogue_returns_catalogue() {
        Mockito.when(database.getAllMovies()).thenReturn(movieMap);
        Catalogue catalogue = service.getCurrentCatalogue();
        assertTrue(catalogue.getMovies().containsKey("Hot Fuzz"));
        assertTrue(catalogue.getMovies().containsKey("Shaun of the Dead"));
        assertTrue(catalogue.getMovies().containsKey("Tropic Thunder"));

        assertEquals(new Movie(Optional.of("Edgar Wright"), Optional.of(Float.valueOf((float) 5.0))), catalogue.getMovies().get("Hot Fuzz"));
        assertEquals(new Movie(Optional.of("Edgar Wright"), Optional.empty()), catalogue.getMovies().get("Shaun of the Dead"));
        assertEquals(new Movie(Optional.of("Ben Stiller"), Optional.of(Float.valueOf((float) 5.0))), catalogue.getMovies().get("Tropic Thunder"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_add_movie_throws_illegal_argument_exception_if_rating_is_higher_than_acceptable_range() {
        service.addMovie(new MovieIO("Tropic Thunder", "Ben Stiller", Float.valueOf((float) 50.0)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_add_movie_throws_illegal_argument_exception_if_rating_is_lower_than_acceptable_range() {
        service.addMovie(new MovieIO("Tropic Thunder", "Ben Stiller", Float.valueOf((float) -1.0)));
    }

    @Test
    public void test_add_movie_sets_rating_to_negative_one_if_rating_is_null(){
        ArgumentCaptor<MovieIO> captor = ArgumentCaptor.forClass(MovieIO.class);
        service.addMovie(new MovieIO("Tropic Thunder", "Ben Stiller", null));
        Mockito.verify(database).addMovie(captor.capture());
        assertEquals(Float.valueOf((float) -1.0), captor.getValue().getRating());
    }

    @Test
    public void test_add_movie_sets_ratings_to_one_decimal_place_and_always_rounds_down(){
        ArgumentCaptor<MovieIO> captor = ArgumentCaptor.forClass(MovieIO.class);
        service.addMovie(new MovieIO("Tropic Thunder", "Ben Stiller", Float.valueOf((float) 4.99999)));
        Mockito.verify(database).addMovie(captor.capture());
        assertEquals(Float.valueOf((float) 4.9), captor.getValue().getRating());
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_edit_movie_throws_illegal_argument_exception_if_rating_is_higher_than_acceptable_range() {
        Mockito.when(database.getMovieByTitle("TrpicThonder")).thenReturn(new Movie(Optional.of("Ben Stiller"), Optional.of(Float.valueOf((float) 5.0))));
        service.editMovie("TrpicThonder", new MovieIO("Tropic Thunder", "Ben Stiller", Float.valueOf((float) 50.0)));
        Mockito.verify(database, Mockito.never()).updateRating(any(), any());
        Mockito.verify(database, Mockito.never()).updateDirector(any(), any());
        Mockito.verify(database, Mockito.never()).updateTitle(any(), any());
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_edit_movie_throws_illegal_argument_exception_if_rating_is_lower_than_acceptable_range() {
        Mockito.when(database.getMovieByTitle("TrpicThonder")).thenReturn(new Movie(Optional.of("Ben Stiller"), Optional.of(Float.valueOf((float) 5.0))));
        service.editMovie("TrpicThonder", new MovieIO("Tropic Thunder", "Ben Stiller", Float.valueOf((float) -1.0)));
        Mockito.verify(database, Mockito.never()).updateRating(any(), any());
        Mockito.verify(database, Mockito.never()).updateDirector(any(), any());
        Mockito.verify(database, Mockito.never()).updateTitle(any(), any());
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_edit_movie_throws_illegal_argument_exception_if_movie_does_not_exist_in_database(){
        Mockito.when(database.getMovieByTitle("TrpicThonder")).thenReturn(null);
        service.editMovie("TrpicThonder", new MovieIO("TrpicThonder", "Ben Stiller", Float.valueOf((float) 5.0)));
    }

    @Test
    public void test_database_not_called_when_edit_movie_data_is_the_same_as_already_present_data(){
        Mockito.when(database.getMovieByTitle("TrpicThonder")).thenReturn(new Movie(Optional.of("Ben Stiller"), Optional.of(Float.valueOf((float) 5.0))));
        service.editMovie("TrpicThonder", new MovieIO("TrpicThonder", "Ben Stiller", Float.valueOf((float) 5.0)));
        Mockito.verify(database, Mockito.never()).updateRating(any(), any());
        Mockito.verify(database, Mockito.never()).updateDirector(any(), any());
        Mockito.verify(database, Mockito.never()).updateTitle(any(), any());
    }

    @Test
    public void test_database_called_if_previous_values_are_empty(){
        Mockito.when(database.getMovieByTitle("TrpicThonder")).thenReturn(new Movie(Optional.empty(), Optional.of(Float.valueOf((float) -1.0))));
        service.editMovie("TrpicThonder", new MovieIO("TrpicThonder", "Ben Stiller", Float.valueOf((float) 5.0)));
        Mockito.verify(database, Mockito.times(1)).updateRating(any(), any());
        Mockito.verify(database, Mockito.times(1)).updateDirector(any(), any());
        Mockito.verify(database, Mockito.never()).updateTitle(any(), any());
    }

    @Test
    public void test_database_called_if_previous_values_are_present_but_different(){
        Mockito.when(database.getMovieByTitle("TrpicThonder")).thenReturn(new Movie(Optional.of("James Cameron"), Optional.of(Float.valueOf((float) 4.9))));
        service.editMovie("TrpicThonder", new MovieIO("Tropic Thunder", "Ben Stiller", Float.valueOf((float) 5.0)));
        Mockito.verify(database, Mockito.times(1)).updateRating(any(), any());
        Mockito.verify(database, Mockito.times(1)).updateDirector(any(), any());
        Mockito.verify(database, Mockito.times(1)).updateTitle(any(), any());
    }

    @Test
    public void test_get_movies_by_director_handles_empty_map_from_database() {
        Mockito.when(database.getMoviesByDirector("Ben Stiller")).thenReturn(new HashMap<>());
        Catalogue catalogue = service.getMoviesByDirector("Ben Stiller");
        assertEquals(new HashMap<>(), catalogue.getMovies());
    }

    @Test
    public void test_get_movies_by_director_returns_catalogue() {
        movieMap.remove("Tropic Thunder");
        Mockito.when(database.getMoviesByDirector("Edgar Wright")).thenReturn(movieMap);
        Catalogue catalogue = service.getMoviesByDirector("Edgar Wright");
        assertTrue(catalogue.getMovies().containsKey("Hot Fuzz"));
        assertTrue(catalogue.getMovies().containsKey("Shaun of the Dead"));

        assertEquals(new Movie(Optional.of("Edgar Wright"), Optional.of(Float.valueOf((float) 5.0))), catalogue.getMovies().get("Hot Fuzz"));
        assertEquals(new Movie(Optional.of("Edgar Wright"), Optional.empty()), catalogue.getMovies().get("Shaun of the Dead"));
    }

    @Test
    public void test_get_movies_above_rating_handles_empty_map_from_database() {
        Mockito.when(database.getMoviesAboveRating(Float.valueOf((float) 4.0))).thenReturn(new HashMap<>());
        Catalogue catalogue = service.getMoviesAboveRating(Float.valueOf((float) 4.0));
        assertEquals(new HashMap<>(), catalogue.getMovies());
    }

    @Test
    public void test_get_movies_above_rating_returns_catalogue() {
        movieMap.remove("Shaun of the Dead");
        Mockito.when(database.getMoviesAboveRating(Float.valueOf((float) 4.0))).thenReturn(movieMap);
        Catalogue catalogue = service.getMoviesAboveRating(Float.valueOf((float) 4.0));
        assertTrue(catalogue.getMovies().containsKey("Hot Fuzz"));
        assertTrue(catalogue.getMovies().containsKey("Tropic Thunder"));

        assertEquals(new Movie(Optional.of("Edgar Wright"), Optional.of(Float.valueOf((float) 5.0))), catalogue.getMovies().get("Hot Fuzz"));
        assertEquals(new Movie(Optional.of("Ben Stiller"), Optional.of(Float.valueOf((float) 5.0))), catalogue.getMovies().get("Tropic Thunder"));
    }

    @Test
    public void test_get_movies_by_director_above_rating_handles_empty_map_from_database() {
        Mockito.when(database.getMoviesByDirectorAboveRating("Edgar Wright", Float.valueOf((float) 4.0))).thenReturn(new HashMap<>());
        Catalogue catalogue = service.getMoviesByDirectorAboveRating("Edgar Wright", Float.valueOf((float) 4.0));
        assertEquals(new HashMap<>(), catalogue.getMovies());
    }

    @Test
    public void test_get_movies_by_director_above_rating_returns_catalogue() {
        movieMap.remove("Shaun of the Dead");
        movieMap.remove("Tropic Thunder");
        movieMap.put("The World's End", new Movie(Optional.of("Edgar Wright"), Optional.of(Float.valueOf((float) 4.0))));
        Mockito.when(database.getMoviesByDirectorAboveRating("Edgar Wright", Float.valueOf((float) 4.0))).thenReturn(movieMap);
        Catalogue catalogue = service.getMoviesByDirectorAboveRating("Edgar Wright", Float.valueOf((float) 4.0));
        assertTrue(catalogue.getMovies().containsKey("Hot Fuzz"));
        assertTrue(catalogue.getMovies().containsKey("The World's End"));

        assertEquals(new Movie(Optional.of("Edgar Wright"), Optional.of(Float.valueOf((float) 5.0))), catalogue.getMovies().get("Hot Fuzz"));
        assertEquals(new Movie(Optional.of("Edgar Wright"), Optional.of(Float.valueOf((float) 4.0))), catalogue.getMovies().get("The World's End"));
    }

    @Test
    public void test_get_movies_by_title_handles_empty_map_from_database() {
        Mockito.when(database.getMovieByTitle("Tropic Thunder")).thenReturn(null);
        Catalogue catalogue = service.getMovieByTitle("Tropic Thunder");
        assertEquals(new HashMap<>(), catalogue.getMovies());
    }

    @Test
    public void test_get_movies_by_title_returns_catalogue() {
        Mockito.when(database.getMovieByTitle("Tropic Thunder")).thenReturn(new Movie(Optional.of("Ben Stiller"), Optional.of(Float.valueOf((float) 5.0))));
        Catalogue catalogue = service.getMovieByTitle("Tropic Thunder");
        assertTrue(catalogue.getMovies().containsKey("Tropic Thunder"));
        assertEquals(1, catalogue.getMovies().size());

        assertEquals(new Movie(Optional.of("Ben Stiller"), Optional.of(Float.valueOf((float) 5.0))), catalogue.getMovies().get("Tropic Thunder"));
    }

    @Test
    public void test_database_not_called_if_previous_value_is_empty_when_deleting_director(){
        Mockito.when(database.getMovieByTitle("Tropic Thunder")).thenReturn(new Movie(Optional.empty(), Optional.of(Float.valueOf((float) -1.0))));
        service.deleteDirectorFromMovie("Tropic Thunder");
        Mockito.verify(database, Mockito.never()).updateDirector(any(), any());
    }

    @Test
    public void test_database_called_if_previous_values_is_present_when_deleting_director(){
        Mockito.when(database.getMovieByTitle("Tropic Thunder")).thenReturn(new Movie(Optional.of("James Cameron"), Optional.of(Float.valueOf((float) 4.9))));
        service.deleteDirectorFromMovie("Tropic Thunder");
        Mockito.verify(database, Mockito.times(1)).updateDirector(any(), any());
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_delete_director_throws_illegal_argument_exception_if_movie_does_not_exist_in_database(){
        Mockito.when(database.getMovieByTitle("TrpicThonder")).thenReturn(null);
        service.deleteDirectorFromMovie("TrpicThonder");
    }

    @Test
    public void test_database_not_called_if_previous_value_is_empty_when_deleting_rating(){
        Mockito.when(database.getMovieByTitle("Tropic Thunder")).thenReturn(new Movie(Optional.empty(), Optional.of(Float.valueOf((float) -1.0))));
        service.deleteRatingFromMovie("Tropic Thunder");
        Mockito.verify(database, Mockito.never()).updateRating(any(), any());
    }

    @Test
    public void test_database_called_if_previous_values_is_present_when_deleting_rating(){
        Mockito.when(database.getMovieByTitle("Tropic Thunder")).thenReturn(new Movie(Optional.of("James Cameron"), Optional.of(Float.valueOf((float) 4.9))));
        service.deleteRatingFromMovie("Tropic Thunder");
        Mockito.verify(database, Mockito.times(1)).updateRating(any(), any());
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_delete_rating_throws_illegal_argument_exception_if_movie_does_not_exist_in_database(){
        Mockito.when(database.getMovieByTitle("Tropic Thunder")).thenReturn(null);
        service.deleteRatingFromMovie("Tropic Thunder");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_delete_movie_throws_illegal_argument_exception_if_movie_does_not_exist_in_database(){
        Mockito.when(database.getMovieByTitle("Tropic Thunder")).thenReturn(null);
        service.deleteMovie("Tropic Thunder");
    }

    @Test
    public void test_database_called_if_previous_value_is_present_when_deleting_movie(){
        Mockito.when(database.getMovieByTitle("Tropic Thunder")).thenReturn(new Movie(Optional.of("James Cameron"), Optional.of(Float.valueOf((float) 4.9))));
        service.deleteMovie("Tropic Thunder");
        Mockito.verify(database, Mockito.times(1)).deleteMovie(any());
    }

    @Test
    public void test_delete_director_calls_database_service(){
        service.deleteDirector("Ben Stiller");
        Mockito.verify(database, Mockito.times(1)).deleteDirector("Ben Stiller");
    }
}