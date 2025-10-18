package at.mediaRatingsPlatform.service;

import at.mediaRatingsPlatform.dao.UserDao;
import at.mediaRatingsPlatform.model.User;
import at.mediaRatingsPlatform.util.PasswordUtil;

public class AuthService {
    private final UserDao userDao;

    public AuthService(UserDao dao){
        this.userDao = dao;
    }

    public User register(String username, String password){
        if (userDao.getByUsername(username)!=null)
            throw new RuntimeException("Username exists");

        // Hash the password before storing it
        String passwordHash = PasswordUtil.hash(password);
        return userDao.create(username, passwordHash);
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
