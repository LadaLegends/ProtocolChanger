# ProtocolChanger

**Lightweight Velocity plugin for spoofing protocol version and server name in server pings.**

Change how your server appears in server lists and to port scanners without any performance overhead.

## Features

- **Protocol Version Spoofing** — Display any protocol ID (e.g., 767 for 1.21.1, 340 for 1.12.2) in pings, regardless of actual backend version
- **Custom Server Name** — Replace the default "Velocity Server" text with your brand
- **Simple Configuration** — Single `config.properties` file, no bloat
- **Hot Reload** — Update config and apply changes instantly via console command
- **Production-Ready** — Zero allocation per ping, compatible with high-tick-rate setups

## Installation

1. Download the plugin JAR
2. Place it in your Velocity `plugins/` directory
3. Restart the proxy (or use plugin reload)
4. Edit `plugins/ProtocolChanger/config.properties`
5. Reload with `/pc reload`

## Configuration

**File:** `plugins/ProtocolChanger/config.properties`

```properties
# Display name of your server in the ping response
name=My cool server

# The Protocol ID (version code) shown to clients
# Example: 767 is for version 1.21.1
protocol=767
```

### Common Protocol IDs

| Version | Protocol ID |
|---------|-------------|
| 1.21.1  | 767         |
| 1.21    | 766         |
| 1.20.5  | 765         |
| 1.20.1  | 763         |
| 1.19.3  | 761         |
| 1.19    | 759         |
| 1.18.2  | 758         |
| 1.12.2  | 340         |

## Commands

* ```/pc reload``` — Reloads the configuration file instantly.

## License

GNU General Public License v3.0 — See LICENSE file for details.

---

**Made by LadaLegendss**
