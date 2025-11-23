package com.example.dev_impact.service;

import com.example.dev_impact.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserDetailsImp implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsImp.class);

    private final UserRepository userRepository;

    public UserDetailsImp(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Loading user details for username: {}", username);
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User Not Found"));
    }
}