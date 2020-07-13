package com.mr.moviecatalogue.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Map;

/**
 * Catalogue class that contains a Map of movies in the catalogue, keyed by the title
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Catalogue {
    //Map of movies with keys as the title
    private Map<String, Movie> movies;
}
