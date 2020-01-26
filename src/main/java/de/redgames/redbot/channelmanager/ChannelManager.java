package de.redgames.redbot.channelmanager;

import com.github.theholywaffle.teamspeak3.api.ChannelProperty;
import com.github.theholywaffle.teamspeak3.api.wrapper.Channel;
import de.redgames.redbot.Module;

import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ChannelManager extends Module implements Runnable {
    private static final ChannelProperty[] channelProperties = {
            ChannelProperty.CHANNEL_FLAG_PERMANENT,
            ChannelProperty.CHANNEL_CODEC,
            ChannelProperty.CHANNEL_CODEC_QUALITY,
            ChannelProperty.CHANNEL_MAXCLIENTS,
            ChannelProperty.CHANNEL_TOPIC,
            ChannelProperty.CHANNEL_FLAG_MAXCLIENTS_UNLIMITED,
            ChannelProperty.CHANNEL_DESCRIPTION
    };

    private ChannelManagerConfiguration config;
    private List<ChannelHandler> channelHandlers;
    private Thread checkThread;

    @Override
    public void onEnable() {
        config = loadConfiguration(Paths.get("config", "channel_manager.json"),
                ChannelManagerConfiguration.class, ChannelManagerConfiguration::new);
        channelHandlers = config.channels.stream().map(ChannelHandler::fromConfigString).collect(Collectors.toList());

        checkThread = new Thread(this);
        checkThread.start();
    }

    @Override
    public void onDisable() {
        checkThread.interrupt();
    }

    @Override
    public void run() {
        while (true) {
            checkChannels();

            try {
                TimeUnit.MILLISECONDS.sleep(config.pollDelay);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private void checkChannels() {
        List<Channel> channels = getApi().getChannels();

        for (ChannelHandler channelHandler : channelHandlers) {
            Map<Integer, Channel> channelList = new HashMap<>();
            List<Integer> emptyChannels = new ArrayList<>();

            for (Channel channel : channels) {
                int number = channelHandler.deserialize(channel.getName());

                if (number != -1) {
                    channelList.put(number, channel);

                    if (channel.getTotalClients() == 0) {
                        emptyChannels.add(number);
                    }
                }
            }

            Channel baseChannel = channelList.get(1);

            // Search for empty channels
            while (emptyChannels.size() > 1) {
                Integer lastChannel = channelList.keySet().stream().max(Comparator.naturalOrder()).orElse(null);

                if (lastChannel != null && emptyChannels.contains(lastChannel)) {
                    Channel removeChannel = channelList.get(lastChannel);

                    if (removeChannel.equals(baseChannel)) {
                        throw new AssertionError("Tried to delete base channel!");
                    }

                    // Delete last channel
                    deleteChannel(removeChannel);

                    channelList.remove(lastChannel);
                    emptyChannels.remove(lastChannel);

                    // Rename base channel
                    if (channelList.size() == 1 && channelHandler.isOptional()) {
                        renameChannel(baseChannel, channelHandler.serialize(1, true));
                    }
                } else {
                    // Stop deleting channels since the last channel is not empty
                    break;
                }
            }

            if (emptyChannels.size() < 1) {
                // Rename base channel
                if (channelList.size() == 1) {
                    renameChannel(baseChannel, channelHandler.serialize(1, false));
                }

                // Create new channel
                int maxChannel = channelList.keySet().stream().max(Comparator.naturalOrder()).orElse(0);
                int nextChannel = channelList.keySet().stream().max(Comparator.naturalOrder()).orElse(0) + 1;
                createChannel(channelList.get(maxChannel), channelHandler.serialize(nextChannel, false));
            }
        }
    }

    private void createChannel(Channel baseChannel, String channelName) {
        Map<ChannelProperty, String> properties = new HashMap<>();

        for (ChannelProperty value : channelProperties) {
            properties.put(value, baseChannel.get(value));
        }

        properties.put(ChannelProperty.CHANNEL_ORDER, Integer.toString(baseChannel.getId()));

        getApi().createChannel(channelName, properties);
    }

    private void deleteChannel(Channel channel) {
        getApi().deleteChannel(channel.getId());
    }

    private void renameChannel(Channel channel, String newName) {
        if (!channel.getName().equalsIgnoreCase(newName)) {
            getApi().editChannel(channel.getId(), ChannelProperty.CHANNEL_NAME, newName);
        }
    }
}
