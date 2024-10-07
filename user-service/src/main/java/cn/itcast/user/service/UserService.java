package cn.itcast.user.service;

import cn.itcast.user.mapper.UserMapper;
import cn.itcast.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Value("${server.port}")
    private Integer port;
    @Autowired
    private UserMapper userMapper;

    public User queryById(Long id) {
        User user = userMapper.findById(id);
        user.setUsername(user.getUsername()+":"+port);
        return user;
    }
}