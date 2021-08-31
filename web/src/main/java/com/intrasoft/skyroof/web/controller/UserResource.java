package com.intrasoft.skyroof.web.controller;

import com.intrasoft.skyroof.web.RestConfig;
import com.intrasoft.skyroof.web.dto.LoginRequestDTO;
import com.intrasoft.skyroof.web.dto.SignUpRequestDTO;
import com.intrasoft.skyroof.web.dto.SignUpResponseDTO;
import com.intrasoft.skyroof.web.dto.TokenResponseDTO;
import com.intrasoft.skyroof.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping(path = RestConfig.API_URI + "/users")
public class UserResource {

    @Autowired
    private UserService userService;

    @PostMapping(value = "/login")
    @PreAuthorize("permitAll()")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody LoginRequestDTO request){
        String token = userService.login(request.getUsername(), request.getPassword());
        TokenResponseDTO tokenResponseDTO = new TokenResponseDTO();
        tokenResponseDTO.setToken(token);
        return ResponseEntity.ok(tokenResponseDTO);
    }

    @PostMapping(value = "/signup")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SignUpResponseDTO> signUp(@RequestBody SignUpRequestDTO request) {

        userService.signup(request.getUsername(), request.getPassword(), request.getRole());

        SignUpResponseDTO response = new SignUpResponseDTO();
        response.setMessage("Successful signup. You may proceed to login.");

        return ResponseEntity.ok(response);
    }

}