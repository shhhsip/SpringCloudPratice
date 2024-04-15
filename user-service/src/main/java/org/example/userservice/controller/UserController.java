package org.example.userservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.userservice.dto.UserDto;
import org.example.userservice.entity.UserEntity;
import org.example.userservice.service.UserService;
import org.example.userservice.vo.Greeting;
import org.example.userservice.vo.RequestUser;
import org.example.userservice.vo.ResponseUser;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/")
public class UserController {
    private Environment env;
    private Greeting greeting;
    private UserService userService;

    public UserController(Environment env, Greeting greeting, UserService userService) {
        this.env = env;
        this.greeting = greeting;
        this.userService = userService;
    }


    @GetMapping("/health_check")
    public String status() {
        return "It's Working in user service on Port : ( " + env.getProperty("local.server.port") + " ), " +
                "port(server.port) : ( " + env.getProperty("server.port") + " ), " +
                "token secret : ( " + env.getProperty("token.secret") + " ), " +
                "token expiration time : ( " + env.getProperty("token.expiration_time") + " )";

    }

    @GetMapping("/welcome")
    public String welcome() {
        return greeting.getMessage();
    }

    @PostMapping("/users")
    public ResponseEntity<ResponseUser> createUser(@RequestBody RequestUser user) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserDto userDto = mapper.map(user, UserDto.class);
        userService.createUser(userDto);

        ResponseUser responseUser = mapper.map(userDto, ResponseUser.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseUser);
    }

    @GetMapping("/users")
    public ResponseEntity<List<ResponseUser>> getUsers() {
        Iterable<UserEntity> userList = userService.getUserByAll();
        List<ResponseUser> result = new ArrayList<>();

        userList.forEach((v) -> {
            result.add(new ModelMapper().map(v, ResponseUser.class));
        });

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ResponseUser> getUsers(@PathVariable("userId") String userId) {
        UserDto userByUserId = userService.getUserByUserId(userId);

        ResponseUser result = new ModelMapper().map(userByUserId, ResponseUser.class);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
