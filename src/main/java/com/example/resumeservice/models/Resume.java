package com.example.resumeservice.models;

import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.List;

@Document(indexName = "resume-index")
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class Resume {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private String id ;
    private String name;
    private String surname;
    private String gender;
    private List<String> skills;

}
