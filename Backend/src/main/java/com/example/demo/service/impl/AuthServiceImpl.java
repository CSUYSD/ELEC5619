package com.example.demo.service.impl;

import com.example.demo.Dao.UserDao;
import com.example.demo.utility.JwtUtil;
import com.example.demo.model.TransactionUsers;
import com.example.demo.model.UserDetail;
import com.github.alenfive.rocketapi.entity.vo.LoginVo;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class AuthServiceImpl {

    private final PasswordEncoder passwordEncoder;
    private final UserDao userDao;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthServiceImpl(PasswordEncoder passwordEncoder, UserDao userDao, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.passwordEncoder = passwordEncoder;
        this.userDao = userDao;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public void saveUser(TransactionUsers user) throws DataIntegrityViolationException {
        //检查用户名是否已存在
        if (userDao.findByUsername(user.getUsername()).isPresent()) {
            throw new DataIntegrityViolationException("User already exists");
        }
        // Encode the password before saving the user
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userDao.save(user);
    }

        //用户登录功能，接收前端传来的用户名和密码，进行身份验证
    public ResponseEntity<Map<String, Object>> login (LoginVo loginVo) {
        //通过用户名和密码生成一个UsernamePasswordAuthenticationToken对象
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(loginVo.getUsername(), loginVo.getPassword());

        //authenticate方法会调用UserDetails loadUserByUsername方法进行身份验证
        Authentication authenticate = authenticationManager.authenticate(usernamePasswordAuthenticationToken);

        if (Objects.isNull(authenticate)) {
            throw new ServiceException("error of username or password");
        }
        //校验成功，强转对象
        UserDetail userDetail = (UserDetail) authenticate.getPrincipal();
        TransactionUsers transactionUsers = userDetail.getTransactionUsers();

        //生成token
        String token = jwtUtil.generateToken(transactionUsers.getId(), transactionUsers.getUsername());
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        return ResponseEntity.ok(map);
    }
}