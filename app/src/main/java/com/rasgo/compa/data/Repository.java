package com.rasgo.compa.data;

//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//
//import com.google.gson.Gson;
//import com.rasgo.compa.data.remote.ApiError;
//import com.rasgo.compa.data.remote.ApiService;
//import com.rasgo.compa.feature.auth.LoginActivity;
//import com.rasgo.compa.model.auth.AuthResponse;
//
//import java.io.IOException;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;

//public class Repository {
//    private static Repository instance = null;
//    private final ApiService apiService;
//
//    private Repository(ApiService apiService) {
//        this.apiService = apiService;
//    }
//
//    public static Repository getRepository(ApiService apiService) {
//        if (instance == null) {
//            instance = new Repository(apiService);
//        }
//        return instance;
//    }
//
//    public LiveData<AuthResponse>login(LoginActivity.UserInfo userInfo){
//        MutableLiveData<AuthResponse> auth = new MutableLiveData<>();
//        Call<AuthResponse> call = apiService.login(userInfo);
//        call.enqueue(new Callback<AuthResponse>() {
//            @Override
//            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
//                if (response.isSuccessful()) {
//                    auth.postValue(response.body());
//                }else{
//                    Gson gson = new Gson();
//                    AuthResponse authResponse = null;
//                    try {
//                        authResponse = gson.fromJson(response.errorBody().string(), AuthResponse.class);
//                    }catch (IOException e) {
//                        ApiError.ErrorMessage errorMessage = ApiError.getErrorFromException(e);
//                        authResponse =new AuthResponse(errorMessage.message, errorMessage.status);
//                    }
//                    auth.postValue(authResponse);
//                }
//
//            }
//
//            @Override
//            public void onFailure(Call<AuthResponse> call, Throwable t) {
//                ApiError.ErrorMessage errorMessage = ApiError.getErrorFromThrowable(t);
//                AuthResponse authResponse = new AuthResponse(errorMessage.message, errorMessage.status);
//                auth.postValue(authResponse);
//
//            }
//        });
//
//        return auth;
//    }
//}
