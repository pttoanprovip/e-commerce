package com.example.demo.service.Authentication;

import java.text.ParseException;

import org.springframework.security.oauth2.core.user.OAuth2User;

import com.example.demo.dto.req.Authentication.AuthenticationRequest;
import com.example.demo.dto.req.Authentication.IntrospectRequest;
import com.example.demo.dto.req.Authentication.LogoutRequest;
import com.example.demo.dto.res.Authentication.AuthenticationResponse;
import com.example.demo.dto.res.Authentication.IntrospectResponse;
import com.nimbusds.jose.JOSEException;

public interface AuthenticationService {
     AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest);

     IntrospectResponse introspect(IntrospectRequest introspectRequest) throws JOSEException, ParseException;

     void logout(LogoutRequest request) throws JOSEException, ParseException;

     AuthenticationResponse authenticateOAuth2(OAuth2User oAuth2User);
}
