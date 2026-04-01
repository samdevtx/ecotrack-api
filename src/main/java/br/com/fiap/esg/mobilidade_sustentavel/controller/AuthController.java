package br.com.fiap.esg.mobilidade_sustentavel.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.esg.mobilidade_sustentavel.dto.auth.LoginRequestDto;
import br.com.fiap.esg.mobilidade_sustentavel.dto.auth.LoginResponseDto;
import br.com.fiap.esg.mobilidade_sustentavel.model.Usuario;
import br.com.fiap.esg.mobilidade_sustentavel.security.TokenService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequestDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.email(), loginRequestDto.senha()));

        Object principal = authentication.getPrincipal();
        String token;
        if (principal instanceof Usuario usuarioPrincipal) {
            token = tokenService.generateToken(usuarioPrincipal);
        } else if (principal instanceof UserDetails) {
             
             
            token = tokenService.generateToken(authentication); 
        } else {
             
            String principalClassName = (principal != null) ? principal.getClass().getName() : "null";
            log.error("Authentication principal is not an instance of UserDetails: {}", principalClassName);
            throw new IllegalStateException("Authentication principal is not an instance of UserDetails.");
        }
        
        return ResponseEntity.ok(new LoginResponseDto(token));
    }
} 