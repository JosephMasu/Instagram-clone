package com.masu.user_service.config;

import io.github.cdimascio.dotenv.Dotenv;

import java.nio.file.Files;
import java.nio.file.Path;

public final class EnvLoader {

    private EnvLoader() {
    }

    public static void load(String moduleDirectory) {
        Path envFile = resolveEnvFile(moduleDirectory);
        if (envFile == null) {
            return;
        }

        Path directory = envFile.getParent() != null
                ? envFile.getParent()
                : Path.of(".");

        Dotenv dotenv = Dotenv.configure()
                .directory(directory.toAbsolutePath().toString())
                .filename(".env")
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry -> {
            if (System.getenv(entry.getKey()) == null
                    && System.getProperty(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });
    }

    private static Path resolveEnvFile(String moduleDirectory) {
        Path[] candidates = {
                Path.of(".env"),
                Path.of(moduleDirectory, ".env")
        };

        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
        }

        return null;
    }
}
