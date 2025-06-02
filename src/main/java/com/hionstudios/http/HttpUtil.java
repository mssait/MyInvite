package com.hionstudios.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.hionstudios.ListResponse;
import com.hionstudios.MapResponse;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class HttpUtil {
    public static String fetch(String url) throws IOException {
        return fetch(url, "GET", null, null, null);
    }

    public static MapResponse toMap(String url) throws IOException {
        return new MapResponse(new JSONObject(fetch(url)));
    }

    public static ListResponse toList(String url) throws IOException {
        return new ListResponse(new JSONArray(fetch(url)));
    }

    public static String fetch(
            String url,
            String method,
            HashMap<String, String> params,
            JSONObject body,
            HashMap<String, String> headers) throws IOException {
        return fetch(url, method, params, body, headers, (FileParam) null);
    }

    public static String fetch(
            String url,
            String method,
            HashMap<String, String> params,
            JSONObject body,
            HashMap<String, String> headers,
            FileParam file) throws IOException {
        List<FileParam> fileParams;
        if (file == null) {
            fileParams = null;
        } else {
            fileParams = new ArrayList<>(1);
            fileParams.add(file);
        }
        return fetch(url, method, params, body, headers, fileParams);
    }

    public static String fetch(
            String url,
            String method,
            HashMap<String, String> params,
            JSONObject body,
            HashMap<String, String> headers,
            List<FileParam> files) throws IOException {
        Request.Builder builder = new Request.Builder();
        if (params != null && params.size() > 0) {
            if ("get".equalsIgnoreCase(method)) {
                HttpUrl httpUrl = HttpUrl.parse(url);
                assert httpUrl != null;
                HttpUrl.Builder urlBuilder = httpUrl.newBuilder();
                params.forEach(urlBuilder::addQueryParameter);
                builder.url(urlBuilder.build());
            } else {
                builder.url(url);
                MultipartBody.Builder multipartBuilder = new MultipartBody.Builder();
                params.forEach(multipartBuilder::addFormDataPart);
                if (files != null) {
                    files.forEach(
                            file -> multipartBuilder.addFormDataPart(file.param, file.filename, file.requestBody));
                }
                builder.method(method, multipartBuilder.build());
            }
        } else {
            builder.url(url);
        }
        if (headers != null) {
            headers.forEach(builder::header);
        }
        if (body != null) {
            builder.method(method, RequestBody.create(body.toString(), MediaType.parse("application/json")));
        }
        ResponseBody responseBody = new OkHttpClient().newBuilder().build().newCall(builder.build()).execute().body();
        return responseBody != null ? responseBody.string() : null;
    }

    public static class FileParam {
        public FileParam(String param, String filename, byte[] content, String mime) {
            this.param = param;
            this.filename = filename;
            this.requestBody = RequestBody.create(content, MediaType.parse(mime));
        }

        public final String param;
        public final String filename;
        public final RequestBody requestBody;
    }
}