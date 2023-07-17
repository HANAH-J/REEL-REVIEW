package com.reelreview.service;

import com.reelreview.api.domain.MovieDetailsDTO;
import com.reelreview.api.domain.MovieUpcommingDTO;
import com.reelreview.api.repo.ApiMovieDetailRepo;
import com.reelreview.api.repo.ApiMovieUpcommingRepo;
import com.reelreview.api.service.TMDBMovieDataManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class MainService {
    @Autowired
    private ApiMovieDetailRepo mdetail;
    @Autowired
    private ApiMovieUpcommingRepo mUpcomming;
    public List<MovieDetailsDTO> getBoxOfficeToday() {
        List<Integer> ranky = new ArrayList<>();
        for(int i = 1 ; i < 11 ; i++){
            ranky.add(i);
        }
        List<MovieDetailsDTO> todaylist =  mdetail.findByRankIn(ranky);
        return todaylist;
    }
    public static String getCurrentDateInStringFormat() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return currentDate.format(formatter);
    }

    public List<MovieUpcommingDTO> getUpcommingToday() {

        List<MovieUpcommingDTO> upcommingList = mUpcomming.findByUpcommingDownloadDate(getCurrentDateInStringFormat());
        return upcommingList;
    }

    public List<MovieDetailsDTO> getMovieListFromDirector(String name) throws IOException, InterruptedException, ParseException {
        String query = URLEncoder.encode(name,"UTF-8");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.themoviedb.org/3/search/person?include_adult=false&language=ko"))
                .header("accept", "application/json")
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI1ZmZhYTU1NDc2ZTRjYTdjNzI3Nzg4ZjlmOTMwZDY0NCIsInN1YiI6IjY0OTk0OWQ1NjJmMzM1MDEyNzQ3MzI2YSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.FiFcp5Wrby8LZtoc_h9tQ2v6yOKyKwO2B8pqzavLsW0")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(response.body());
        JSONObject jobj = (JSONObject) obj;
        JSONArray jary = (JSONArray) jobj.get("results");
        //임시로 0번만
        JSONObject data = (JSONObject) jary.get(0);
        int personId = (int)data.get("id");

        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(URI.create("https://api.themoviedb.org/3/person/"+personId+"/movie_credits?language=ko"))
                .header("accept", "application/json")
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI1ZmZhYTU1NDc2ZTRjYTdjNzI3Nzg4ZjlmOTMwZDY0NCIsInN1YiI6IjY0OTk0OWQ1NjJmMzM1MDEyNzQ3MzI2YSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.FiFcp5Wrby8LZtoc_h9tQ2v6yOKyKwO2B8pqzavLsW0")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response2 = HttpClient.newHttpClient().send(request2, HttpResponse.BodyHandlers.ofString());
        Object obj2 = parser.parse(response2.body());
        JSONObject jobj2 = (JSONObject) obj2;
        JSONArray jary2 = (JSONArray) jobj2.get("crew");
        List<MovieDetailsDTO> directorSearchDataList = new ArrayList<>();
        for(int i = 0 ; i < jary2.size() ; i++){
            JSONObject moviesSearchByDirector = (JSONObject)jary2.get(i);
            TMDBMovieDataManager t = new TMDBMovieDataManager();
            JSONObject jData = t.TMDBMovieJsonObjectToNeededDataJsonObject(moviesSearchByDirector);
            MovieDetailsDTO m = t.JSONObjectToMovieDetailsDTO(jData);
            directorSearchDataList.add(m);
        }

        return directorSearchDataList;
    }
}
