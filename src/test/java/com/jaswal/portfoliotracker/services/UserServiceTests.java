package com.jaswal.portfoliotracker.services;

import com.jaswal.portfoliotracker.PortfolioTrackerApplication;
import com.jaswal.portfoliotracker.entities.Portfolio;
import com.jaswal.portfoliotracker.entities.User;
import com.jaswal.portfoliotracker.repositories.PortfolioRepository;
import com.jaswal.portfoliotracker.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@SpringBootTest(classes = PortfolioTrackerApplication.class)
@Transactional
public class UserServiceTests {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");
        testUser = userRepository.save(testUser);
    }


    @Test
    void testCreateUsername_Success() {
        User created = userService.createUsername("newuser", "new@test.com");

        assertNotNull(created.getUserId());
        assertEquals("Username match", "newuser", created.getUsername());
        assertEquals("Email match", "new@test.com", created.getEmail());
    }

    @Test
    void testCreateUsername_VerifyPersistedInDatabase() {
        User created = userService.createUsername("persistuser", "persist@test.com");

        User fromDb = userRepository.findById(created.getUserId()).orElseThrow();
        assertEquals("DB username match", "persistuser", fromDb.getUsername());
        assertEquals("DB email match", "persist@test.com", fromDb.getEmail());
    }


    @Test
    void testGetUser_Success() {
        User found = userService.getUser(testUser.getUserId());

        assertEquals("Username match", "testuser", found.getUsername());
        assertEquals("Email match", "test@test.com", found.getEmail());
    }

    @Test
    void testGetUser_NonExistentId_ThrowsException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userService.getUser(99999L)
        );
        assertTrue(exception.getMessage().contains("does not exist"));
    }


    @Test
    void testUpdateUser_Success() {
        User updated = userService.updateUser(testUser.getUserId(), "updatedname");

        assertEquals("Updated username", "updatedname", updated.getUsername());
    }

    @Test
    void testUpdateUser_NullUsername_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                userService.updateUser(testUser.getUserId(), null)
        );
        assertTrue(exception.getMessage().contains("can't be empty"));
    }

    @Test
    void testUpdateUser_EmptyUsername_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                userService.updateUser(testUser.getUserId(), "")
        );
        assertTrue(exception.getMessage().contains("can't be empty"));
    }

    @Test
    void testUpdateUser_BlankUsername_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                userService.updateUser(testUser.getUserId(), "   ")
        );
        assertTrue(exception.getMessage().contains("can't be empty"));
    }

    @Test
    void testUpdateUser_DuplicateUsername_ThrowsException() {
        User otherUser = new User();
        otherUser.setUsername("takenname");
        otherUser.setEmail("other@test.com");
        userRepository.save(otherUser);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userService.updateUser(testUser.getUserId(), "takenname")
        );
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    void testUpdateUser_NonExistentUser_ThrowsException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userService.updateUser(99999L, "somename")
        );
        assertTrue(exception.getMessage().contains("does mot exist"));
    }


    @Test
    void testUpdateEmail_Success() {
        User updated = userService.updateEmail(testUser.getUserId(), "updated@test.com");

        assertEquals("Updated email", "updated@test.com", updated.getEmail());
    }

    @Test
    void testUpdateEmail_NullEmail_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                userService.updateEmail(testUser.getUserId(), null)
        );
        assertTrue(exception.getMessage().contains("can't be empty"));
    }

    @Test
    void testUpdateEmail_EmptyEmail_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                userService.updateEmail(testUser.getUserId(), "")
        );
        assertTrue(exception.getMessage().contains("can't be empty"));
    }

    @Test
    void testUpdateEmail_DuplicateEmail_ThrowsException() {
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setEmail("taken@test.com");
        userRepository.save(otherUser);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userService.updateEmail(testUser.getUserId(), "taken@test.com")
        );
        assertTrue(exception.getMessage().contains("exists a user with the email"));
    }

    @Test
    void testUpdateEmail_NonExistentUser_ThrowsException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userService.updateEmail(99999L, "any@test.com")
        );
        assertTrue(exception.getMessage().contains("does mot exist"));
    }


    @Test
    void testDeleteUser_Success_NoPortfolios() {
        userService.deleteUser(testUser.getUserId());

        assertFalse(userRepository.findById(testUser.getUserId()).isPresent());
    }

    @Test
    void testDeleteUser_WithPortfolios_ThrowsException() {
        Portfolio portfolio = new Portfolio();
        portfolio.setCreatedBy(testUser);
        portfolio.setName("Test Portfolio");
        portfolioRepository.save(portfolio);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                userService.deleteUser(testUser.getUserId())
        );
        assertTrue(exception.getMessage().contains("Cannot delete user with existing portfolios"));
    }

    @Test
    void testDeleteUser_NonExistentUser_ThrowsException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userService.deleteUser(99999L)
        );
        assertTrue(exception.getMessage().contains("does not exist"));
    }


    @Test
    void testEmailExists_True() {
        assertTrue(userService.emailExists("test@test.com"));
    }

    @Test
    void testEmailExists_False() {
        assertFalse(userService.emailExists("nonexistent@test.com"));
    }

    @Test
    void testEmailExists_NullEmail_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                userService.emailExists(null)
        );
    }

    @Test
    void testEmailExists_EmptyEmail_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                userService.emailExists("")
        );
    }


    @Test
    void testUsernameExists_True() {
        assertTrue(userService.usernameExists("testuser"));
    }

    @Test
    void testUsernameExists_False() {
        assertFalse(userService.usernameExists("nonexistentuser"));
    }

    @Test
    void testUsernameExists_NullUsername_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                userService.usernameExists(null)
        );
    }

    @Test
    void testUsernameExists_EmptyUsername_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                userService.usernameExists("")
        );
    }
}