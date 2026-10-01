package com.metas.web;

import com.metas.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final JwtService jwt;
    private final String claveAdmin;

    public AuthController(JwtService jwt, @Value("${app.admin-password}") String claveAdmin) {
        this.jwt = jwt;
        this.claveAdmin = claveAdmin;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        if (req.password() == null || !req.password().equals(claveAdmin)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Contraseña incorrecta");
        }
        return new LoginResponse(jwt.generar());
    }
}
