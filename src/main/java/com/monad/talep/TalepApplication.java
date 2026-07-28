package com.monad.talep;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Theme("talep")
public class TalepApplication implements AppShellConfigurator {
    public static void main(String[] args) {
        SpringApplication.run(TalepApplication.class, args);
    }
}
