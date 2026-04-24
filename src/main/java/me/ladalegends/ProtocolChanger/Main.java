package me.ladalegends.ProtocolChanger;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@Plugin(
        id = "protocolchanger",
        name = "ProtocolChanger",
        version = "1.1.0",
        description = "Lightweight Velocity plugin for spoofing protocol version and server name in server pings.",
        authors = {"LadaLegends"}
)
public class Main {

    private final Logger logger;
    private final Path dataDir;
    private final ProxyServer proxy;

    private String serverName;
    private int protocolId;

    @Inject
    public Main(@DataDirectory Path dataDir, Logger logger, ProxyServer proxy) {
        this.dataDir = dataDir;
        this.logger = logger;
        this.proxy = proxy;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        try {
            loadConfiguration();
        } catch (IOException e) {
            logger.error("Failed to load configuration", e);
        }

        proxy.getCommandManager().register(
                proxy.getCommandManager().metaBuilder("protocolchanger")
                        .aliases("pc")
                        .build(),
                new ReloadCommand()
        );
    }

    private void loadConfiguration() throws IOException {
        Files.createDirectories(dataDir);

        Path configFile = dataDir.resolve("config.properties");
        Properties props = new Properties();

        if (Files.notExists(configFile)) {
            props.setProperty("name", "Velocity Server");
            props.setProperty("protocol", "767");
            Files.writeString(configFile, propertiesToString(props), StandardCharsets.UTF_8);
        } else {
            try (var reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
                props.load(reader);
            }
        }

        serverName = props.getProperty("name", "Velocity Server");

        try {
            protocolId = Integer.parseInt(props.getProperty("protocol", "767"));
        } catch (NumberFormatException e) {
            logger.warn("Invalid protocol value in config, falling back to 767");
            protocolId = 767;
        }
    }

    private String propertiesToString(Properties props) {
        StringBuilder sb = new StringBuilder();
        props.forEach((k, v) -> sb.append(k).append("=").append(v).append("\n"));
        return sb.toString();
    }

    @Subscribe(order = PostOrder.LAST)
    public void onProxyPing(ProxyPingEvent event) {
        ServerPing ping = event.getPing();
        if (ping == null) return;

        event.setPing(ping.asBuilder()
                .version(new ServerPing.Version(protocolId, serverName))
                .build());
    }

    private class ReloadCommand implements SimpleCommand {

        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();
            if (!(source instanceof ConsoleCommandSource)) return;

            try {
                loadConfiguration();
                source.sendMessage(Component.text("ProtocolChanger configuration reloaded."));
            } catch (IOException e) {
                logger.error("Failed to reload configuration", e);
                source.sendMessage(Component.text("Failed to reload configuration. Check console for details."));
            }
        }

        @Override
        public boolean hasPermission(Invocation invocation) {
            return invocation.source() instanceof ConsoleCommandSource;
        }
    }
}