package com.github.masahitojp.botan.handlers.google.images.pojo;

import lombok.Data;

import java.util.List;

@Data
public class GoogleImageResponse {
    public ResponseData responseData;

    public class Result {
        public String unescapedUrl;
    }

    public class ResponseData {
        public List<Result> results;
    }
}