package com.monad.talep.service;

import com.monad.talep.entity.AppUser;
import com.monad.talep.entity.Role;
import com.monad.talep.entity.RoleName;
import com.monad.talep.repository.AppUserRepository;
import com.monad.talep.repository.RoleRepository;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final AppUserRepository userRepo;
    private final RoleRepository roleRepo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(AppUserRepository userRepo, RoleRepository roleRepo) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
    }

    /** Musteri self-servis kayit */
    public AppUser registerCustomer(String name, String email, String rawPassword) {
        if (userRepo.findByEmail(email).isPresent())
            throw new IllegalArgumentException("Bu e-posta ile kayitli kullanici var");
        Role customerRole = roleRepo.findByRoleName(RoleName.CUSTOMER).orElseThrow();
        AppUser u = new AppUser();
        u.setNameSurname(name);
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(rawPassword));
        u.setRole(customerRole);
        return userRepo.save(u);
    }

    /** Sifre sifirlama: gecici sifre uretir, kaydeder, geri doner */
    public Optional<String> resetPassword(String email) {
        return userRepo.findByEmail(email).map(u -> {
            String temp = generateTempPassword();
            u.setPasswordHash(encoder.encode(temp));
            userRepo.save(u);
            return temp;
        });
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHJKLMNPRSTUVYZabcdefghjkmnprstuvyz23456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random r = new java.util.Random();
        for (int i = 0; i < 8; i++) sb.append(chars.charAt(r.nextInt(chars.length())));
        return sb.toString();
    }

    public Optional<AppUser> login(String email, String rawPassword) {
        return userRepo.findByEmail(email)
                .filter(AppUser::isActive)
                .filter(u -> encoder.matches(rawPassword, u.getPasswordHash()));
    }

    public String hash(String rawPassword) { return encoder.encode(rawPassword); }

    public void storeInSession(AppUser user) {
        VaadinSession.getCurrent().setAttribute(AppUser.class, user);
    }

    public AppUser currentUser() {
        VaadinSession session = VaadinSession.getCurrent();
        return session == null ? null : session.getAttribute(AppUser.class);
    }

    public void logout() {
        VaadinSession.getCurrent().setAttribute(AppUser.class, null);
        VaadinSession.getCurrent().close();
    }
}
