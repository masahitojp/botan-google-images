package com.github.masahitojp.botan.handlers.google.images.pojo;

import lombok.Data;
import java.util.List;

@Data
public class GoogleImageResponse {
    public List<Link> items;

    public class Link {
        public String link;
    }
}