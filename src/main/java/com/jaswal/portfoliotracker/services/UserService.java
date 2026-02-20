package com.jaswal.portfoliotracker.services;

import com.jaswal.portfoliotracker.entities.User;
import com.jaswal.portfoliotracker.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public User createUsername(String username, String email){
        User user = new User();

        user.setUsername(username);

        user.setEmail(email);

        return userRepository.save(user);
    }

    public void deleteUser(Long user_id){
        User user = userRepository.findById(user_id).orElseThrow(()->
                new RuntimeException("The user with the id" + user_id + "does not exist"));
        userRepository.delete(user);
    }
    public User getUser(Long user_id){
        return userRepository.findById(user_id).orElseThrow(() ->
                new RuntimeException("The user with the id" + user_id + "does not exist"));
    }

    public User updateUser(Long user_id, String username) {
        if(username == null || username.trim().isEmpty()){
        throw new IllegalArgumentException("The username can't be empty");
    }

        User user = userRepository.findById(user_id).orElseThrow(()->
                new RuntimeException("The user with the id" + user_id + "does mot exist"));
        user.setUsername(username);
        return userRepository.save(user);
    }

    public User updateEmail(Long user_id, String email){
        if(email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("The email can't be empty" + email);
        }
        User user = userRepository.findById(user_id).orElseThrow(() ->
                new RuntimeException("The user with the id" + user_id + "does mot exist"));
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
