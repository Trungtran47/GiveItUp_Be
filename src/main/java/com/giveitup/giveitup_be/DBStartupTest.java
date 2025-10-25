package com.giveitup.giveitup_be;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DBStartupTest implements CommandLineRunner {


    @Override
    public void run(String... args) {
        System.out.println("📌 DB Test START");
        System.out.println("📌 DB Test END");
    }
}
