package com.mr.moviecatalogue.inputobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Input object class to be used for RequestBody in post and patch requests for directors
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DirectorIO {
    private String name;
    private List<String> movies;
}
