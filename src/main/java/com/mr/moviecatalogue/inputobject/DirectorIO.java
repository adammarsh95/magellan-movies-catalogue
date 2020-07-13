package com.mr.moviecatalogue.inputobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DirectorIO {
    private String name;
    private List<String> movies;
}
