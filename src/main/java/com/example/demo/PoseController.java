/* Copyright 2022 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
*/

package com.example.demo;

import com.google.cloud.spring.data.firestore.FirestoreReactiveOperations;
import com.google.cloud.spring.data.firestore.FirestoreTemplate;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import com.google.cloud.Timestamp;
import com.google.cloud.Date;
import org.springframework.core.ParameterizedTypeReference;
import java.net.URI;


import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.RequestEntity;
import org.springframework.http.HttpMethod;

import org.json.JSONObject;  
import org.json.JSONArray;  
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

/*
 Controller class for invoking Server REST APIs 
 Example controller demonstrating usage of Spring Data Repositories for Firestore. 
*/

@Controller
class Posecontroller {
private static final String get_pose_by_id = "https://firestore.googleapis.com/v1beta1/";
private static final String param_url = "projects/<<Your project>>/databases/(default)/documents/poses/";
private static final String queryString = "{\"structuredQuery\":{\"select\":{\"fields\":[{\"fieldPath\":\"name\"}]},\"from\":[{\"collectionId\":\"poses\"}]}}";
private static final String runQuery = "projects/<<Your Project>>/databases/(default)/documents:runQuery";
RestTemplate restTemplate = new RestTemplate();


  /*
        Method that is invoked on search, to return the searchpose HTML page 
  */
    @GetMapping("/search")
    public String searchForm(Pose pose) {
        return "searchpose";
    }

    @GetMapping("/createpose")
    public String createForm(Pose pose) {
        return "createpose";
    }

    /*
        Method to invoke API that retrieves a specific pose
    */
    @GetMapping("/showpose")
    public String callPoseByIdAPI(Pose pose){
        try {
            String nameString = pose.getName();

            String endpoint = "https://data.mongodb-api.com/app/<<YOUR_APP>>/endpoint/data/beta";
            String action = "/action/findOne";
            String uri = endpoint + action;

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", "<<YOUR_API_KEY>>");

            JsonObject properties = new JsonObject();
            properties.addProperty("dataSource", "Test");
            properties.addProperty("database", "SpringGCP");
            properties.addProperty("collection", "poses");

            JsonObject filter = new JsonObject();
            filter.addProperty("name", nameString);
            properties.add("filter", filter);

            HttpEntity<?> entity = new HttpEntity<Object>(properties.toString(), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(uri, entity, String.class);

            String result = response.getBody();
            JSONObject jsonObject = new JSONObject(result);
            JSONObject document = jsonObject.getJSONObject("document");
            String breath = document.getString("breath");
            String posture = document.getString("posture"); 

            pose.setName(nameString);
            pose.setBreath(breath);
            pose.setPosture(posture);
            
            return "showmessage";
        } catch(Exception e){
            System.out.println("Exception in Search Request: " + e);
            return "searchpose";
        }
    }


@PostMapping("/editpose")
public String callPatch(Pose pose) {
    HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
    requestFactory.setConnectTimeout(1000000);
    requestFactory.setReadTimeout(1000000);
    restTemplate.setRequestFactory(requestFactory);
   try{
    String nameString = pose.getName();
    String idParam = param_url + nameString;
    pose.setUrlname(idParam);
    String paramString = get_pose_by_id + idParam;
    String jsonString = pose.toString();
    System.out.println("JSON: " + jsonString);
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    HttpEntity<String> entity = new HttpEntity<>(jsonString, headers);
    ResponseEntity<String> result = restTemplate.exchange(paramString, HttpMethod.PATCH, entity, String.class);
    }catch(Exception e){
        System.out.println("EXCEPTION in edit" + e);
        return "searchpose";
    }
   return "showmessage";
}


@PostMapping("/deletepose")
private String removePoseByName(Pose pose) {
    restTemplate = new RestTemplate();
    try{
    String nameString = pose.getName();
    String idParam = param_url + nameString;
    pose.setUrlname(idParam);
    String paramString = get_pose_by_id + idParam;
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    //HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
    HttpEntity<String> entity = new HttpEntity<>(headers);
    ResponseEntity<String> resultString = restTemplate.exchange(paramString, HttpMethod.DELETE, entity, String.class);
    String result = resultString.toString();
    System.out.println("RESULT: " + result);
    }catch(Exception e){
        System.out.println("EXCEPTION in delete" + e);
        return "searchpose";
    }
   return "searchpose";
}
/*
    Method that is invoked on home page, to return the HomePage HTML page 
*/
@GetMapping("/home")
public String homeForm(Pose pose) {
    return "pose";
}

/*
    Method that is invoked on home page, to return the HomePage HTML page 
*/
@PostMapping("/run")
public String runForm(Pose pose) {
    try{
        String paramString = get_pose_by_id + runQuery;
        //String jsonString = queryString;
        String jsonString = pose.getQuery();
        System.out.println("paramString: " + paramString);
        System.out.println("JSON: " + jsonString);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(jsonString, headers);
        ResponseEntity<String> result = restTemplate.exchange(paramString, HttpMethod.POST, entity, String.class);
        System.out.println(result);
        pose.setResponse(result.toString());
        return "ryoq";
        }catch(Exception e){
            System.out.println("EXCEPTION in edit" + e);
            return "errmessage";
        }
}


  @GetMapping("/run_your_own_query")
  private String runQueryForm(Pose pose) {
    return "ryoq";
  }


}
