package ru.onshin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VkBotApplication implements CommandLineRunner {

    @Autowired
    private VkLongPollHandler vkLongPollHandler;

    public static void main(String[] args) {
        SpringApplication.run(VkBotApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        vkLongPollHandler.start();
    }
}