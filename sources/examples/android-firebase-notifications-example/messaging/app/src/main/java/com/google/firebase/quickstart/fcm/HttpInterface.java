package com.google.firebase.quickstart.fcm;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface HttpInterface {

    @Headers({"X-OP-APIKey: 424abf94bfd24667a76fa0cdae722a85", "Content-Type: application/json", "Accept: application/json"})
    @POST("v1/registerNotificationClientToken/")
    Call<PostResponse> postJson(@Body NativeNotifKeysOntology instance);

    @Headers({"X-OP-APIKey: 424abf94bfd24667a76fa0cdae722a85", "Content-Type: application/json", "Accept: application/json"})
    @PUT("v1/registerNotificationClientToken/{id}")
    Call<ResponseBody> putJson(@Path("id") String onesaitId, @Body NativeNotifKeysOntology instance);
}
