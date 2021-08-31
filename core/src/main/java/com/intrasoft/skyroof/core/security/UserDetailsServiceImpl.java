package com.intrasoft.skyroof.core.security;

import com.intrasoft.skyroof.core.persistence.dao.UserDao;
import com.intrasoft.skyroof.core.persistence.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserDao userDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userDao.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("User '" + username + "' not found"));

        return new UserPrincipal(user);

    }
}