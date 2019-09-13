package com.google.firebase.quickstart.fcm;

import java.util.List;

public class PostResponse {

    public PostResponse(int count, List<String> ids) {
        this.count = count;
        this.ids = ids;
    }

    int count;
    List<String> ids;
}
