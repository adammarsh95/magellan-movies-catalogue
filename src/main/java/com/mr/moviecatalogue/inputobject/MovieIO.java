package com.mr.moviecatalogue.inputobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MovieIO {
    private String title;
    private String director;
    private Float rating;
}
