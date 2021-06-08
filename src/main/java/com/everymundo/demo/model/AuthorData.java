package com.everymundo.demo.model;

import java.time.LocalDate;

import lombok.Data;

@Data
public class AuthorData {

    private String firstName;
    private String middleName;
    private String lastName;
    private LocalDate birthDate;

}
