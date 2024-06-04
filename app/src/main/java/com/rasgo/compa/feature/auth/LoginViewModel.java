package com.rasgo.compa.feature.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.rasgo.compa.data.Repository;
import com.rasgo.compa.model.auth.AuthResponse;


public class LoginViewModel extends ViewModel {
    private Repository repository;

    public LoginViewModel(Repository repository){
        this.repository = repository;
    }

    public LiveData<AuthResponse> login(LoginActivity.UserInfo userInfo){
        return repository.login(userInfo);
    }
}
