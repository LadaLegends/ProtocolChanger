![Banner](https://cdn.modrinth.com/data/cached_images/e700249f2d411e3ead477cbf8539c839adad5d0d.png)

A lightweight plugin for Velocity that allows you to spoof the protocol version and server name when sending pings.

## Features

- **Protocol Version** — Display any protocol identifier (e.g., 774 for version 1.21.11, 776 for version 26.2) in pings, regardless of the actual backend version.
- **Server Name** — Replaces the default “Velocity Server” text with your brand name
- Simple configuration in a single file, without unnecessary elements or parameters.
- Update the configuration and apply changes instantly using the restart command in the console.
- Zero resource allocation per ping; compatible with high-tick-rate configurations

## Installation

1. Download the plugin’s JAR file.
2. Place it in the `plugins/` directory of your Velocity installation.
3. Restart the proxy.
4. Edit the `plugins/ProtocolChanger/config.properties` file.
5. Reload using the `/pc reload` command.

## Configuration

**File:** `plugins/ProtocolChanger/config.properties`

```properties
# The name of your server displayed in the ping response
name=My server

# Protocol identifier (version code) displayed to clients
# Example: 774 corresponds to version 1.21.11
protocol=774
```

### Common Protocol Identifiers

| Version | Protocol ID |
|---------|-------------|
| Java Edition 26.3 | 777 |
| Java Edition 26.2 | 776 |
| Java Edition 1.21.11  | 774 |
| Java Edition 1.21.4 | 769 |
| Java Edition 1.20.4 | 765 |
| Java Edition 1.20.1 | 763 |
| Java Edition 1.19.4 | 762 |
| Java Edition 1.16.5 | 754 |
| Java Edition 1.12.2 | 340 |

Full list on [minecraft.wiki](https://minecraft.wiki/w/Protocol_version#List_of_protocol_versions).

## Commands

* ```/pc reload```

## License

GNU General Public License v3.0 — See LICENSE file for details.

---

❤ by LadaLegends
