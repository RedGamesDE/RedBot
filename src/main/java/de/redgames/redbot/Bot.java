package de.redgames.redbot;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import de.redgames.redbot.channelmanager.ChannelManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

public final class Bot extends Module {
    private final Module[] modules = {
            new ChannelManager()
    };

    @Override
    public void onEnable() {
        for (Module module : modules) {
            module.enable(this);
        }

        System.out.println("Bot enabled!");
    }

    @Override
    public void onDisable() {
        for (Module module : modules) {
            module.disable();
        }

        System.out.println("Bot disabled!");
    }

    public void shutdown() {
        disable();
        getQuery().exit();
        System.exit(0);
    }

    public static Bot create() {
        Bot bot = new Bot();

        Credentials credentials = bot.loadConfiguration(Paths.get("config", "credentials.json"),
                Credentials.class, Credentials::new);

        TS3Config config = new TS3Config();
        config.setHost(credentials.hostname);
        config.setQueryPort(credentials.port);
//        config.setFloodRate(TS3Query.FloodRate.UNLIMITED);

        TS3Query query = new TS3Query(config);
        query.connect();

        TS3Api api = query.getApi();
        api.login(credentials.username, credentials.password);
        api.selectVirtualServerById(credentials.virtualServer);

        bot.enable(bot, query, api, query.getAsyncApi());

        return bot;
    }

    public static void main(String[] args) {
        Bot bot = Bot.create();
        bot.getApi().setNickname("RedBot");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String command;
            while ((command = reader.readLine()) != null) {
                switch (command.trim().toLowerCase()) {
                    case "shutdown":
                    case "exit":
                    case "stop":
                    case "quit":
                        bot.shutdown();
                        break;
                    case "help":
                    case "?":
                        System.out.println("List of available commands:");
                        System.out.println("\thelp - Displays this message");
                        System.out.println("\tshutdown - Stops the bot");
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error in REPL");
            e.printStackTrace(System.err);
        }
    }
}
