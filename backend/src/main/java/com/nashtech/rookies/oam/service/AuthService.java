package com.nashtech.rookies.oam.service;

import com.nashtech.rookies.oam.dto.request.LoginRequest;
import com.nashtech.rookies.oam.dto.response.LoginResponse;
import com.nashtech.rookies.oam.model.User;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);
    User getAuthenticatedUser();
}
