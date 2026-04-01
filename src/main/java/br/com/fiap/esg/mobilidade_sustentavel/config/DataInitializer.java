package br.com.fiap.esg.mobilidade_sustentavel.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import br.com.fiap.esg.mobilidade_sustentavel.model.Usuario;
import br.com.fiap.esg.mobilidade_sustentavel.repository.UsuarioRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
         
        String adminEmail = "admin@ecotrack.com";
        if (usuarioRepository.findByEmail(adminEmail).isEmpty()) {
            Usuario adminUser = new Usuario();
            adminUser.setNome("Admin User");
            adminUser.setEmail(adminEmail);
            adminUser.setSenha(passwordEncoder.encode("admin123"));  
            adminUser.setRoles("ROLE_USER,ROLE_ADMIN");  
            adminUser.setEnabled(true);  
            adminUser.setFailedLoginAttempts(0);  
            adminUser.setAccountLockedUntil(null);  
            adminUser.setAccountExpirationDate(null);  
            usuarioRepository.save(adminUser);
            System.out.println("Created admin user: " + adminEmail);
        }

         
        String regularUserEmail = "user@ecotrack.com";
        if (usuarioRepository.findByEmail(regularUserEmail).isEmpty()) {
            Usuario regularUser = new Usuario();
            regularUser.setNome("Regular User");
            regularUser.setEmail(regularUserEmail);
            regularUser.setSenha(passwordEncoder.encode("user123"));
            regularUser.setRoles("ROLE_USER");
            regularUser.setEnabled(true);  
            regularUser.setFailedLoginAttempts(0);  
            regularUser.setAccountLockedUntil(null);  
            regularUser.setAccountExpirationDate(null);  
            usuarioRepository.save(regularUser);
            System.out.println("Created regular user: " + regularUserEmail);
        }
    }
} 