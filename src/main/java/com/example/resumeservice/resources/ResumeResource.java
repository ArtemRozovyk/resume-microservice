package com.example.resumeservice.resources;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.IOUtils;
import com.example.resumeservice.models.Resume;
import com.example.resumeservice.services.ResumeService;
import com.google.gson.JsonObject;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

@RestController
@RequestMapping("/v1/api/resumes")
public class ResumeResource {

    @Autowired
    private ResumeService resumeService;

    @GetMapping(params = {"name"})
    public ResponseEntity<List<Resume>> getResumeByName(@RequestParam("name") String name) throws IOException {
        return ResponseEntity.ok(resumeService.getResumes(name));
    }


    @GetMapping()
    public ResponseEntity<List<Resume>> getResumes() throws IOException {
        return ResponseEntity.ok(resumeService.getResumes());
    }


    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping(params = {"skills"})
    public ResponseEntity<List<Resume>> getResumesBySkills(@RequestParam("skills") String skills) throws Exception {
        return ResponseEntity.ok(resumeService.getResumesBySkils(skills.split(" ")));
    }

    public JSONObject JsonFromFile() {

        try {
            JSONObject array = JSON.parseObject(Files.readString(Paths.get("src/main/resources/res.json")));
            return array;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping()
    public ResponseEntity<Resume> uploadFile(@RequestParam("file") MultipartFile file) {

        byte[] buffer = new byte[0];
        try {
            InputStream initialStream = file.getInputStream();
            buffer = new byte[initialStream.available()];
            initialStream.read(buffer);

            File targetFile = new File("src/main/resources/targetFile.pdf");
            try (OutputStream outStream = new FileOutputStream(targetFile)) {
                outStream.write(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            System.out.println("Parsing..");
            Process p = Runtime.getRuntime().exec("./parse.sh");
            p.waitFor();
            System.out.println("Done..");
            JSONObject d = JsonFromFile();
            HashMap<String, ArrayList<String>> re = findUsefulInfoInJson(d);
            Resume resume = new Resume();
            resume.setName(re.get("firstName").get(0));
            resume.setGender(re.get("gender").get(0));
            resume.setSurname(re.get("surname").get(0));
            if (re.containsKey("skills")){
                String skills = re.get("skills").get(0);
                String[] skillstr = (skills == null) ? new String[0] : skills.split(";|,|[.]|•");
                resume.setSkills(Arrays.asList(skillstr));
            }
            resumeService.saveNewResume(resume);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(new Resume(), HttpStatus.OK);


    }

    public void parseTree(Object obj, HashMap<String, ArrayList<String>> results) {

        if (obj instanceof JSONObject) {
            for (Map.Entry<String, Object> joEntry : ((JSONObject) obj).entrySet()) {
                if (joEntry.getValue() instanceof String) {
                    if (results.containsKey(joEntry.getKey())) {
                        results.get(joEntry.getKey()).add((String) joEntry.getValue());
                    } else {
                        ArrayList<String> l = new ArrayList<>();
                        l.add((String) joEntry.getValue());
                        results.put(joEntry.getKey(), l);
                    }
                }
                if (joEntry.getValue() instanceof JSONObject) {
                    parseTree(joEntry.getValue(), results);
                }
            }
        }
        if (obj instanceof JSONArray) {
            for (JSONObject jo : ((JSONArray) obj).toJavaList(JSONObject.class)) {
                parseTree(jo, results);
            }
        }

    }

    private void fillList(Object obj, List<String> lst) {
        if (obj instanceof JSONObject) {
            for (Map.Entry<String, Object> joEntry : ((JSONObject) obj).entrySet()) {
                if (joEntry.getValue() instanceof String) {
                    lst.add((String) joEntry.getValue());
                }
                if (joEntry.getValue() instanceof JSONObject) {
                    fillList(joEntry.getValue(), lst);
                }
            }
        }
        if (obj instanceof JSONArray) {
            for (JSONObject jo : ((JSONArray) obj).toJavaList(JSONObject.class)) {
                String jstr = jo.toJSONString();
                lst.add(jstr);
            }
        }
    }

    private void generalInfoParse(String key, Object obj, HashMap<String, ArrayList<String>> results) {
        ArrayList<String> lst = new ArrayList<>();
        fillList(obj, lst);
        results.put(key, lst);

    }

    public HashMap<String, ArrayList<String>> findUsefulInfoInJson(JSONObject object) {
        HashMap<String, ArrayList<String>> results = new HashMap<>();
        for (Map.Entry<String, Object> entry : object.entrySet()) {
            generalInfoParse(entry.getKey(), entry.getValue(), results);
            if (entry.getValue() instanceof String) {
                ArrayList<String> l = new ArrayList<>();
                l.add((String) entry.getValue());
                results.put(entry.getKey(), l);
            } else {
                parseTree(entry.getValue(), results);
            }
        }
        return results;

    }



}

