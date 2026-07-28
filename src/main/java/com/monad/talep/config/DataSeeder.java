package com.monad.talep.config;

import com.monad.talep.entity.*;
import com.monad.talep.repository.*;
import com.monad.talep.service.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * H2 icin demo veri. Oracle profilinde DB'de veri varsa dokunmaz.
 * Giris bilgileri (hepsi sifre: 1234):
 *   musteri@demo.com  -> CUSTOMER
 *   po@demo.com       -> PRODUCT_OWNER
 *   dev@demo.com      -> DEVELOPER
 *   admin@demo.com    -> ADMIN
 */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(RoleRepository roleRepo,
                           AppUserRepository userRepo,
                           ProjectRepository projectRepo,
                           AuthService authService,
                           RequestService requestService,
                           PrioritizationService prioritizationService,
                           TaskService taskService) {
        return args -> {
            if (roleRepo.count() > 0) return;

            Role customerRole = new Role(); customerRole.setRoleName(RoleName.CUSTOMER); roleRepo.save(customerRole);
            Role poRole = new Role(); poRole.setRoleName(RoleName.PRODUCT_OWNER); roleRepo.save(poRole);
            Role devRole = new Role(); devRole.setRoleName(RoleName.DEVELOPER); roleRepo.save(devRole);
            Role adminRole = new Role(); adminRole.setRoleName(RoleName.ADMIN); roleRepo.save(adminRole);

            AppUser musteri = user(userRepo, authService, "Ahmet Yilmaz", "musteri@demo.com", customerRole);
            AppUser musteri2 = user(userRepo, authService, "Zeynep Kaya", "musteri2@demo.com", customerRole);
            musteri2.setPlan(PlanType.PRO);
            userRepo.save(musteri2);
            AppUser po = user(userRepo, authService, "Mert Demir", "po@demo.com", poRole);
            AppUser dev = user(userRepo, authService, "Elif Celik", "dev@demo.com", devRole);
            AppUser dev2 = user(userRepo, authService, "Can Arslan", "dev2@demo.com", devRole);
            user(userRepo, authService, "Tugce Toprak", "topraktugce203@gmail.com", adminRole);
            user(userRepo, authService, "Demo Admin", "admin@demo.com", adminRole);

            Project crm = new Project(); crm.setProjectName("CRM Modernizasyon");
            crm.setDescription("Musteri iliskileri sistemi yenileme"); projectRepo.save(crm);
            Project mobil = new Project(); mobil.setProjectName("Mobil Uygulama");
            mobil.setDescription("iOS/Android musteri uygulamasi"); projectRepo.save(mobil);

            Request r1 = requestService.createRequest(musteri, crm, "Login ekrani hata veriyor",
                    "Kullanici girisinde 500 hatasi aliniyor, acil cozum gerekli.");
            Request r2 = requestService.createRequest(musteri, crm, "Rapor ekrani cok yavas",
                    "Aylik satis raporu 30 saniyede aciliyor.");
            Request r3 = requestService.createRequest(musteri2, mobil, "Push bildirim ozelligi",
                    "Mobil uygulamaya kampanya bildirimi eklensin.");
            Request r4 = requestService.createRequest(musteri2, mobil, "Karanlik mod istegi",
                    "Uygulamaya dark mode secenegi eklensin.");
            Request r5 = requestService.createRequest(musteri, crm, "Excel export butonu",
                    "Talep listesi Excel olarak indirilebilsin.");

            requestService.changeStatus(r2, RequestStatus.UNDER_REVIEW, po);

            // SIMPLE ve AHP ornekleri (Strategy Pattern)
            prioritizationService.prioritize(r3, 4, 5, "AHP", 3.0, po);
            prioritizationService.prioritize(r4, 2, 3, "SIMPLE", 0, po);
            prioritizationService.prioritize(r5, 5, 4, "SIMPLE", 0, po);

            // 1:N kaniti: ayni talepten 2 gorev
            taskService.convertToTask(r5, "Backend export servisi", dev, java.time.LocalDate.now().plusDays(3), po);
            taskService.convertToTask(r5, "UI export butonu", dev2, java.time.LocalDate.now().plusDays(7), po);
            taskService.convertToTask(r3, "Push bildirim entegrasyonu", dev, java.time.LocalDate.now().plusDays(10), po);

            requestService.addComment(r1, po, "Log kayitlarini inceliyoruz, hafta ici donus yapilacak.");
            requestService.addComment(r5, dev, "Export servisi yarin test ortaminda.");
        };
    }

    private AppUser user(AppUserRepository repo, AuthService auth, String name, String email, Role role) {
        AppUser u = new AppUser();
        u.setNameSurname(name);
        u.setEmail(email);
        u.setPasswordHash(auth.hash("1234"));
        u.setRole(role);
        return repo.save(u);
    }
}
