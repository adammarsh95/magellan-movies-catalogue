package com.mr.moviecatalogue.service;

import com.mr.moviecatalogue.domain.Movie;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class DatabaseServiceTest {

    @InjectMocks
    private DatabaseService databaseService;

    private ResultSet mockResultSet;

    private String movieTitleOne = "Tropic Thunder";
    private String movieTitleTwo = "Snatch";
    private String movieDirectorOne = "Ben Stiller";
    private String movieDirectorTwo = "Guy Ritchie";
    private Float movieRatingOne = new Float(5.0);
    private Float movieRatingTwo = new Float(4.5);

    @Before
    public void setup(){
         mockResultSet = Mockito.mock(ResultSet.class);
    }

    //SneakyThrows annotation is used to avoid compiler issues with calling ResultSet methods,
    //although these are never actually called so this exception cannot actually be thrown.
    @Test @SneakyThrows(SQLException.class)
    public void test_get_movie_map_from_result_set(){
        Mockito.when(mockResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        Mockito.when(mockResultSet.getString("TITLE")).thenReturn(movieTitleOne).thenReturn(movieTitleTwo);
        Mockito.when(mockResultSet.getString("DIRECTOR")).thenReturn(movieDirectorOne).thenReturn(movieDirectorTwo);
        Mockito.when(mockResultSet.getFloat("RATING")).thenReturn(movieRatingOne).thenReturn(movieRatingTwo);

        Map<String, Movie> movieMap = databaseService.getMovieMapFromResultSet(mockResultSet);
        assertTrue(movieMap.containsKey(movieTitleOne));
        assertTrue(movieMap.containsKey(movieTitleTwo));

        assertEquals(movieDirectorOne, movieMap.get(movieTitleOne).getDirector().get());
        assertEquals(movieDirectorTwo, movieMap.get(movieTitleTwo).getDirector().get());
        assertEquals(movieRatingOne, movieMap.get(movieTitleOne).getRating().get());
        assertEquals(movieRatingTwo, movieMap.get(movieTitleTwo).getRating().get());
    }

    @Test @SneakyThrows(SQLException.class)
    public void test_get_movie_map_from_result_set_with_null_values() {
        Mockito.when(mockResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        Mockito.when(mockResultSet.getString("TITLE")).thenReturn(movieTitleOne).thenReturn(movieTitleTwo);
        Mockito.when(mockResultSet.getString("DIRECTOR")).thenReturn(null).thenReturn(movieDirectorTwo);
        Mockito.when(mockResultSet.getFloat("RATING")).thenReturn(movieRatingOne).thenReturn(new Float(-1.0));

        Map<String,Movie> movieMap = databaseService.getMovieMapFromResultSet(mockResultSet);
        assertTrue(movieMap.containsKey(movieTitleOne));
        assertTrue(movieMap.containsKey(movieTitleTwo));

        assertFalse(movieMap.get(movieTitleOne).getDirector().isPresent());

        assertEquals(movieDirectorTwo, movieMap.get(movieTitleTwo).getDirector().get());
        assertEquals(movieRatingOne, movieMap.get(movieTitleOne).getRating().get());
        assertEquals(new Float(-1.0), movieMap.get(movieTitleTwo).getRating().get());
    }
}
