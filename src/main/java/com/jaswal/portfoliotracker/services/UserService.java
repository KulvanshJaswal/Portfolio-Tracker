package com.jaswal.portfoliotracker.services;

import com.jaswal.portfoliotracker.entities.User;
import com.jaswal.portfoliotracker.repositories.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final PortfolioService portfolioService;
    UserRepository userRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository, PortfolioService portfolioService){
        this.userRepository = userRepository;
        this.portfolioService = portfolioService;
    }

    public User createUsername(String username, String email, String password){
        if(usernameExists(username)){
            throw new RuntimeException("The user with the username " + username + " already exists");
        }
        User user = new User();

        user.setUsername(username);

        user.setEmail(email);

        user.setPassword(encoder.encode(password));
        return userRepository.save(user);
    }

    public User checkPassword(String username, String email, String password){
        Optional<User> user;
        if(username != null){
            user = userRepository.findByUsername(username);
        } else if(email != null){
            user = userRepository.findByEmail(email);
        } else {
            throw new IllegalArgumentException("Username and email cannot be empty");
        }

        if(user.isEmpty()){
            throw new RuntimeException("The username or email address is invalid");
        }

        if(encoder.matches(password, user.get().getPassword())){
            return user.get();
        }
        throw new RuntimeException("Invalid password");
    }

    public void deleteUser(Long user_id){
        User user = userRepository.findById(user_id).orElseThrow(()->
                new RuntimeException("The user with the id " + user_id + " does not exist"));
        if(!portfolioService.findUsersPortfolios(user_id).isEmpty()) {
            throw new IllegalStateException("Cannot delete user with existing portfolios");
        }
        userRepository.delete(user);
    }
    public User getUser(Long user_id){
        return userRepository.findById(user_id).orElseThrow(() ->
                new RuntimeException("The user with the id " + user_id + " does not exist"));
    }

    public User updateUser(Long user_id, String username) {
        if(username == null || username.trim().isEmpty()){
        throw new IllegalArgumentException("The username can't be empty");
    }

        User user = userRepository.findById(user_id).orElseThrow(()->
                new RuntimeException("The user with the id " + user_id + " does mot exist"));
        if(usernameExists(username)){
            throw new RuntimeException("The user with the username " + username + " already exists");
        }
        user.setUsername(username);
        return userRepository.save(user);
    }

    public User updateEmail(Long user_id, String email){
        if(email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("The email can't be empty" + email);
        }
        User user = userRepository.findById(user_id).orElseThrow(() ->
                new RuntimeException("The user with the id " + user_id + " does mot exist"));
        if(emailExists(email)){
            throw new RuntimeException("There exists a user with the email " + email);
        }
        user.setEmail(email);
        return userRepository.save(user);
    }
    public boolean emailExists(String email){
        if(email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("The email can't be empty" + email);
        }
        Optional<User> user = userRepository.findByEmail(email);
        return user.isPresent();

    }

    public boolean usernameExists(String username){
        if(username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("The username can't be empty");
        }
        Optional<User> user = userRepository.findByUsername(username);
        return user.isPresent();
    }



}
