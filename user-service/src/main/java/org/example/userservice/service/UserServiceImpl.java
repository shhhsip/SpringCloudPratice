package org.example.userservice.service;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.example.userservice.client.OrderServiceClient;
import org.example.userservice.dto.UserDto;
import org.example.userservice.entity.UserEntity;
import org.example.userservice.repository.UserRepository;
import org.example.userservice.vo.ResponseOrder;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements UserService{
    private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder;

    private Environment env;
    private RestTemplate restTemplate;

    private OrderServiceClient orderServiceClient;

    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder,
                           Environment env, RestTemplate restTemplate, OrderServiceClient orderServiceClient) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.env = env;
        this.restTemplate = restTemplate;
        this.orderServiceClient = orderServiceClient;
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        userDto.setUserId(UUID.randomUUID().toString());
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserEntity userEntity = mapper.map(userDto, UserEntity.class);
        userEntity.setEncryptedPwd(passwordEncoder.encode(userDto.getPwd()));
        userRepository.save(userEntity);

        return mapper.map(userDto, UserDto.class);
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        Optional<UserEntity> userEntity = userRepository.findByUserId(userId);

        if(userEntity.isEmpty()){
            throw new UsernameNotFoundException("User not found");
        }

        UserDto userDto = new ModelMapper().map(userEntity.get(), UserDto.class);

        // Using as rest Template - 1
       /* String orderUrl = String.format(env.getProperty("order_service.url"), userId);
        ResponseEntity<List<ResponseOrder>> orderListResponse =
                restTemplate.exchange(orderUrl, HttpMethod.GET, null,
                                        new ParameterizedTypeReference<List<ResponseOrder>>() {});
        List<ResponseOrder> orderList = orderListResponse.getBody();*/

        /* Using Feign client and Exception handling */
        /*List<ResponseOrder> orderList = null;
        try {
            orderList = orderServiceClient.getOrders(userId);
        } catch (FeignException exception) {
            log.error(exception.getMessage());
        }*/

        /* ErrorDecoder handling */
        List<ResponseOrder> orderList = orderServiceClient.getOrders(userId);


        userDto.setOrders(orderList);

        return userDto;
    }

    @Override
    public Iterable<UserEntity> getUserByAll() {
        return userRepository.findAll();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(username);
        if(user == null)
            throw new UsernameNotFoundException(username);

        return new User(user.getEmail()
                , user.getEncryptedPwd(), true, true, true, true,
                new ArrayList<>());
    }

    @Override
    public UserDto getUserDetailsByEmail(String email) {
        return new ModelMapper().map(userRepository.findByEmail(email), UserDto.class);
    }
}
