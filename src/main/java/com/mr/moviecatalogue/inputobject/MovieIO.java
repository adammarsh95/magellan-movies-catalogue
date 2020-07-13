package com.mr.moviecatalogue.inputobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Input object class to be used for RequestBody in post and patch requests for movies
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MovieIO {
    private String title;
    private String director;
    private Float rating;
}
