package com.intrasoft.skyroof.service;

import com.intrasoft.skyroof.misc.StringUtils;
import com.intrasoft.skyroof.core.persistence.dao.UserDao;
import com.intrasoft.skyroof.core.persistence.model.Role;
import com.intrasoft.skyroof.core.persistence.model.User;
import com.intrasoft.skyroof.core.security.JwtTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtils jwtTokenUtils;

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    public User findByUsername(String username){
        return userDao.findByUsername(username).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "User was not found"));
    }

    public String login(String username, String password) {
        try {

            //Check username and password validity
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            log.debug("Username and password are valid");

            User user = findByUsername(username);
            String token = jwtTokenUtils.createToken(username, Collections.singletonList(user.getRole()));

            log.debug("Token for user {} generated successfully", username);
            return token;
        } catch (AuthenticationException e) {
            log.debug("Invalid username or password given");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid username or password given.");
        }
    }

    public User signup(String username, String password, Role role) {

        // Min 1 lowercase - min 1 uppercase - min 1 number - min 1 special character - 8 to 15 length
        if(!password.matches("(?=^.{8,15}$)((?=.*\\d)(?=.*\\W+))(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$")){
            log.debug("Password does not match strength criteria. Signup aborted...}");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must contain at least 1 uppercase and 1 lowercase letter," +
                            " 1 number and 1 special character and must be between 8 and 15 characters");
        }

        if(!StringUtils.isAscii(username) || !StringUtils.isAscii(password)){
            log.debug("Invalid charset in request body");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Username or password contains invalid characters. Only ASCII encoding is allowed.");
        }

        if(userDao.existsByUsername(username)){
            log.debug("Username {} already exists. Signup aborted...", username);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Username already exists. Please try choosing an alternative one.");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        user = userDao.save(user);

        log.debug("User {} registered successfully", user.getUsername());
        return user;
    }

}
