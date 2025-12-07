# How to Play Multiplayer (Cross-PC)

## 1. Prerequisites
- Both computers must have **Java 17 (or higher)** installed.
- For **same network** play: Both computers must be connected to the same Wi-Fi/LAN.
- For **different network** play: Host must configure port forwarding on their router.

## 2. Same Network (LAN) Multiplayer

### Hosting the Game (Player 1)
1. Run `PlaySquabbles.bat` or `java -jar matcher-v2.jar`.
2. Enter your Name and click **Submit Name**.
3. Click **Multiplayer**.
4. Click **Start Server**.
5. The game will display:
   - **Local IP**: Your IP on the local network (e.g., `192.168.1.5`)
   - **Port**: The game port (e.g., `54321`)
6. Click **Copy Local Info** and send this to your friend.
7. Wait for your friend to join.

### Joining the Game (Player 2)
1. Run `PlaySquabbles.bat` or `java -jar matcher-v2.jar`.
2. Enter your Name and click **Submit Name**.
3. Click **Multiplayer**.
4. In the **Join Section**:
   - **Host IP**: Enter the Local IP Player 1 gave you (e.g., `192.168.1.5`).
   - **Port**: Enter the Port number Player 1 gave you (e.g., `54321`).
5. Click **Join Game**.

## 3. Different Network (WAN) Multiplayer

### Hosting the Game (Player 1)
1. Run `PlaySquabbles.bat` or `java -jar matcher-v2.jar`.
2. Enter your Name and click **Submit Name**.
3. Click **Multiplayer**.
4. Click **Start Server**.
5. Wait for the **Public IP** to appear (may take a few seconds).
6. **Configure Port Forwarding** on your router:
   - Log into your router (usually `192.168.1.1` or `192.168.0.1`)
   - Find "Port Forwarding" or "Virtual Server" settings
   - Forward the displayed **Port** (e.g., `54321`) to your **Local IP**
   - Save the settings
7. Click **Copy Public Info** and send this to your friend.
8. Wait for your friend to join.

### Joining the Game (Player 2)
1. Run `PlaySquabbles.bat` or `java -jar matcher-v2.jar`.
2. Enter your Name and click **Submit Name**.
3. Click **Multiplayer**.
4. In the **Join Section**:
   - **Host IP**: Enter the Public IP Player 1 gave you.
   - **Port**: Enter the Port number Player 1 gave you.
5. Click **Join Game**.

## Troubleshooting

### "Connection Failed"
- **Same Network**: Ensure both computers are on the same Wi-Fi/LAN.
- **Different Network**: 
  - Verify port forwarding is configured correctly
  - Check that the host's firewall allows the port
- **Windows Firewall**: Allow "Java(TM) Platform SE binary" through the firewall.

### "Address already in use"
- Restart the game and try hosting again to generate a new port.

### "UnsupportedClassVersionError"
- Your Java version is too old. Install Java 17 or higher from:
  - Oracle JDK: https://www.oracle.com/java/technologies/downloads/
  - OpenJDK: https://adoptium.net/

### Port Forwarding Help
- Search "[your router model] port forwarding tutorial" on YouTube
- Common router login addresses: `192.168.1.1`, `192.168.0.1`, `10.0.0.1`
- Default credentials are often on a sticker on your router

## Alternative for Different Networks
If port forwarding is too complex, use a VPN tool to simulate a LAN connection:
- **Hamachi** (easiest, free for up to 5 people)
- **ZeroTier** (more advanced, unlimited)
- **Radmin VPN** (simple, unlimited)

With VPN: Both players connect to the same VPN network, then use the VPN IP addresses for "Same Network" play.

