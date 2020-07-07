package com.mr.moviecatalogue.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Catalogue implements Serializable {
    //Map of movies with keys as the title
    private HashMap<String, Movie> movies;
}
