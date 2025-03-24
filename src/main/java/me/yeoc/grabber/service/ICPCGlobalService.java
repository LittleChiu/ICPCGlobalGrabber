package me.yeoc.grabber.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import me.yeoc.grabber.object.Contest;
import me.yeoc.grabber.object.Institution;
import me.yeoc.grabber.object.TeamObject;
import me.yeoc.grabber.object.User;
import okhttp3.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class ICPCGlobalService {
    private String auth;


    @SneakyThrows
    public User getUserInfo(){
        if (auth == null){
            return null;
        }
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url("https://icpc.global/api/person/info/basic")
                .addHeader("authorization", auth)
                .build();
        Response response = client.newCall(request).execute();
        String string = response.body().string();
        return JSONObject.parseObject(string, User.class);
    }
    @SneakyThrows
    public List<Contest> getContest(int id){
        if (auth == null){
            return null;
        }
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url("https://icpc.global/api/contest/site/tree/registration/id/"+id)
                .addHeader("authorization", auth)
                .build();
        Response response = client.newCall(request).execute();
        String string = response.body().string();
        return JSONArray.parseArray(string, Contest.class);
    }
    @SneakyThrows
    public List<Contest> getGlobal(int id){

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url("https://icpc.global/api/contest/public/regionals/"+id)
                .build();
        Response response = client.newCall(request).execute();
        String string = response.body().string();
        return JSONArray.parseArray(string, Contest.class);
    }
    @SneakyThrows
    public List<Institution> getInstitution(String id){
        if (auth == null){
            return null;
        }
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url("https://icpc.global/api/common/institutionunit/suggest?&page=1&size=25&name="+id)
                .addHeader("authorization", auth)
                .build();
        Response response = client.newCall(request).execute();
        String string = response.body().string();
        return JSONArray.parseArray(string, Institution.class);
    }
    @SneakyThrows
    public String createTeam(String teamName,int contestId,int institution){
        if (auth == null){
            return null;
        }
        TeamObject teamObject = new TeamObject(teamName, contestId, institution);
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType,JSONArray.toJSONString(Collections.singletonList(teamObject)));
        Request request = new Request.Builder()
                .url("https://icpc.global/api/team/register/bulk")
                .method("POST", body)
                .addHeader("authorization", auth)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
//        return JSONArray.parseArray(string, Institution.class);
//        return "";
    }

}
