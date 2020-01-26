package de.redgames.redbot;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3ApiAsync;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.function.Supplier;

public abstract class Module {
    private static final Gson configGson = new GsonBuilder().setPrettyPrinting().create();

    private Bot bot;
    private TS3Query query;
    private TS3Api api;
    private TS3ApiAsync asyncApi;

    final void enable(Bot bot, TS3Query query, TS3Api api, TS3ApiAsync asyncApi) {
        this.bot = bot;
        this.query = query;
        this.api = api;
        this.asyncApi = asyncApi;

        onEnable();
    }

    final void enable(Bot bot) {
        this.bot = bot;
        this.query = bot.getQuery();
        this.api = bot.getApi();
        this.asyncApi = bot.getAsyncApi();

        onEnable();
    }

    public void onEnable() {}

    final void disable() {
        onDisable();
    }

    public void onDisable() {}

    public final Bot getBot() {
        return bot;
    }

    public final TS3Query getQuery() {
        return query;
    }

    public final TS3Api getApi() {
        return api;
    }

    public final TS3ApiAsync getAsyncApi() {
        return asyncApi;
    }

    public final <T> T loadConfiguration(Path path, Class<T> type, Supplier<T> configSupplier) {
        try {
            Files.createDirectories(path.getParent());

            T configuration;

            if (Files.exists(path)) {
                try (BufferedReader reader = Files.newBufferedReader(path)) {
                    configuration = configGson.fromJson(reader, type);
                }
            } else {
                configuration = configSupplier.get();
                Files.createFile(path);
            }

            Files.write(path, Collections.singleton(configGson.toJson(configuration)));

            return configuration;
        } catch (IOException e) {
            throw new IOError(e);
        }
    }
}
