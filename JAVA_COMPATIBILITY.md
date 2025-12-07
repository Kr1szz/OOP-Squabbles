# Java Version Compatibility

## Current Configuration
This project is now compiled with **Java 17** for maximum compatibility.

## Requirements
- **Minimum Java Version**: Java 17 or higher
- **Recommended**: Java 17 LTS or Java 21 LTS

## Running the Game

### If you have Java 17+:
```bash
java -jar target/matcher-v2.jar
```

### If you have an older Java version:
You'll need to upgrade to at least Java 17. Download from:
- Oracle JDK: https://www.oracle.com/java/technologies/downloads/
- OpenJDK: https://adoptium.net/

## Multiplayer Over Different Networks

### For Same WiFi (LAN):
1. Host: Click "Multiplayer" → "Start Server"
2. Host: Share the **Local IP** and **Port** with your friend
3. Friend: Enter the Local IP and Port, then click "Join Game"

### For Different WiFi/Networks (WAN):
1. Host: Click "Multiplayer" → "Start Server"
2. Host: Configure **Port Forwarding** on your router:
   - Forward the displayed Port (e.g., 53901) to your Local IP
   - Instructions vary by router model - search "[your router model] port forwarding"
3. Host: Share your **Public IP** and **Port** with your friend
4. Friend: Enter the Public IP and Port, then click "Join Game"

## Troubleshooting

### "UnsupportedClassVersionError"
This means your Java version is too old. The error message shows:
- `class file version 61.0` = Compiled with Java 17
- `only recognizes ... up to 64.0` = Your Java supports up to Java 20

**Solution**: Upgrade to Java 17 or higher.

### Connection Failed
- **Same WiFi**: Check that both computers are on the same network
- **Different WiFi**: Ensure port forwarding is configured correctly on the host's router
- **Firewall**: Make sure your firewall allows the game through

## Build Information
- Compiled with: Java 17
- JavaFX Version: 21
- Build Tool: Maven 3.9+
