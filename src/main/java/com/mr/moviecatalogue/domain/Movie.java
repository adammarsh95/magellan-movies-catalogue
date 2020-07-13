package com.mr.moviecatalogue.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Optional;

/**
 * Movie class that contains 2 optionals, director and rating.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Movie implements Serializable {
    private Optional<String> director;
    private Optional<Float> rating;
}
