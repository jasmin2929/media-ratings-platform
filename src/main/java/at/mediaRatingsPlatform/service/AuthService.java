package at.mediaRatingsPlatform.service;

import at.mediaRatingsPlatform.dao.ProfileDao;
import at.mediaRatingsPlatform.dao.UserDao;
import at.mediaRatingsPlatform.model.User;
import at.mediaRatingsPlatform.util.PasswordUtil;

public class AuthService {
    private final UserDao userDao;
    private final ProfileDao profileDao;

    public AuthService(UserDao userDao, ProfileDao profileDao){
        this.userDao = userDao;
        this.profileDao = profileDao;
    }

    public User register(String username, String password){
        if (userDao.getByUsername(username)!=null)
            throw new RuntimeException("Username exists");

        // Hash the password before storing it
        String passwordHash = PasswordUtil.hash(password);
        User user = userDao.create(username, passwordHash);

        // Automatically create a profile for the new user
        profileDao.create(user.getId());

        return user;
    }

    public String login(String username, String password){
        User u = userDao.getByUsername(username);

        if (u == null || !PasswordUtil.verify(password, u.getPasswordHash()))
            throw new RuntimeException("Invalid credentials");

        return userDao.storeToken(u);
    }

    public User getUserByToken(String token){
        User u = userDao.getByToken(token);
        if (u==null)
            throw new RuntimeException("unauthorized");
        return u;
    }

}
