package com.example.resumeservice.services;

import com.example.resumeservice.models.Resume;
import com.example.resumeservice.repositories.ResumeRepository;
import com.google.gson.Gson;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.elasticsearch.index.query.QueryBuilders.*;

@Service
public class ResumeService {

    @Autowired
    private ResumeRepository resumeRepository;
    @Autowired
    private RestHighLevelClient restHighLevelClient;


    @Autowired
    private Gson gson;


    @GetMapping
    public List<Resume> getResumes(String name) throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .size(5000)
                .query(matchQuery("name", name));
        return executeQuery(searchSourceBuilder);
    }


    @GetMapping
    public List<Resume> getResumes() throws IOException {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .size(5000)
                .query(QueryBuilders.matchAllQuery());

        return executeQuery(searchSourceBuilder);
    }

    private List<Resume> executeQuery(SearchSourceBuilder searchSourceBuilder) throws IOException {
        SearchRequest searchRequest = new SearchRequest("resume-index");
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        return Stream.of(searchResponse.getHits().getHits())
                .map(hit -> hit.getSourceAsString())
                .map(resume -> gson.fromJson(resume, Resume.class))
                .collect(toList());
    }

    @PostMapping
    public Resume saveNewResume(Resume resume)  {
        return resumeRepository.save(resume);
    }

    @PostMapping
    public  List<Resume> getResumesBySkils(String [] skills) throws IOException {
        QueryBuilder qb = new BoolQueryBuilder();
        for(String skill : skills ){
            qb = boolQuery().should(matchQuery("skills",skill));
        }
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .size(5000)
                .query(qb);
        return executeQuery(searchSourceBuilder);
    }
}
