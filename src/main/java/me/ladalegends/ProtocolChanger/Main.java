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
import java.nio.file.StandardCopyOption;
import java.util.Properties;

@Plugin(
        id = "protocolchanger",
        name = "ProtocolChanger",
        version = "1.2.1",
        description = "Lightweight Velocity plugin for spoofing protocol version and server name in server pings.",
        authors = {"LadaLegends"}
)
public class Main {

    private static final String DEFAULT_NAME = "Velocity Server";
    private static final int DEFAULT_PROTOCOL = 767;
    private static final String CONFIG_FILE_NAME = "config.properties";
    private static final String KEY_NAME = "name";
    private static final String KEY_PROTOCOL = "protocol";

    private final Logger logger;
    private final Path dataDir;
    private final ProxyServer proxy;

    private volatile String serverName = DEFAULT_NAME;
    private volatile int protocolId = DEFAULT_PROTOCOL;

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
            logger.error("Failed to load configuration, falling back to defaults ({}={}, {}={})",
                    KEY_NAME, DEFAULT_NAME, KEY_PROTOCOL, DEFAULT_PROTOCOL, e);
            serverName = DEFAULT_NAME;
            protocolId = DEFAULT_PROTOCOL;
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

        Path configFile = dataDir.resolve(CONFIG_FILE_NAME);
        Properties props = new Properties();

        if (Files.notExists(configFile)) {
            props.setProperty(KEY_NAME, DEFAULT_NAME);
            props.setProperty(KEY_PROTOCOL, String.valueOf(DEFAULT_PROTOCOL));
            writeConfigAtomically(configFile, props);
        } else {
            try (var reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
                props.load(reader);
            }
        }

        String loadedName = props.getProperty(KEY_NAME, DEFAULT_NAME);
        int loadedProtocol;
        try {
            loadedProtocol = Integer.parseInt(props.getProperty(KEY_PROTOCOL, String.valueOf(DEFAULT_PROTOCOL)));
        } catch (NumberFormatException e) {
            logger.warn("Invalid '{}' value in config, falling back to {}", KEY_PROTOCOL, DEFAULT_PROTOCOL);
            loadedProtocol = DEFAULT_PROTOCOL;
        }

        if (loadedProtocol < 0) {
            logger.warn("'{}' value {} is out of range, falling back to {}", KEY_PROTOCOL, loadedProtocol, DEFAULT_PROTOCOL);
            loadedProtocol = DEFAULT_PROTOCOL;
        }

        serverName = loadedName;
        protocolId = loadedProtocol;
    }

    private void writeConfigAtomically(Path target, Properties props) throws IOException {
        Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
        try (var writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
            props.store(writer, "ProtocolChanger config");
        }
        Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    @Subscribe(order = PostOrder.LAST)
    public void onProxyPing(ProxyPingEvent event) {
        ServerPing ping = event.getPing();
        if (ping == null) return;

        String name = serverName;
        int protocol = protocolId;

        event.setPing(ping.asBuilder()
                .version(new ServerPing.Version(protocol, name))
                .build());
    }

    private class ReloadCommand implements SimpleCommand {

        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();
            if (!(source instanceof ConsoleCommandSource)) return;

            try {
                loadConfiguration();
                source.sendMessage(Component.text(
                        "ProtocolChanger configuration reloaded: protocol=" + protocolId + ", name=" + serverName));
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
