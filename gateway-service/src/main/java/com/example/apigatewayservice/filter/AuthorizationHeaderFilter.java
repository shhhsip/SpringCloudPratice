package com.example.apigatewayservice.filter;

import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {
    private final Environment env;

    public AuthorizationHeaderFilter(Environment env) {
        super(Config.class);
        this.env = env;
    }

    public static class Config{

    }

    // login -> token -> users (with token) -> header(include token)
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if(!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION))
                return onError(exchange, "no authorization header", HttpStatus.UNAUTHORIZED);

            String authorizationHeader
                    = request.getHeaders().get(org.springframework.http.HttpHeaders.AUTHORIZATION).get(0);


            String jwt = StringUtils.delete(authorizationHeader, "Bearer");
            if(!isJwtValid(jwt))
                return onError(exchange, "JWT token is not valid", HttpStatus.UNAUTHORIZED);


            return chain.filter(exchange);
        };
    }

    private boolean isJwtValid(String jwt) {
        Boolean result = true;
        String subject = null;
        System.out.println("-----------------------------------"+env.getProperty("token.secret"));
        try {
            subject = Jwts.parser().setSigningKey(env.getProperty("token.secret"))
                    .parseClaimsJws(jwt).getBody()
                    .getSubject();
        } catch (Exception e) {
            result = false;
        }

        if(StringUtils.isEmpty(subject)){
            result = false;
        }

        return result;
    }

    // Mono(단일값), Flux(다중값) -> Spring WebFlux
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);

        log.error(message);
        return response.setComplete();
    }
}
