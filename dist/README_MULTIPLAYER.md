# How to Play Multiplayer (Cross-PC)

## 1. Prerequisites
- Both computers must have **Java 21 (or higher)** installed.
- Both computers must be connected to the **same network** (Wi-Fi or LAN).
  - *Note: If you are not on the same network, you will need to use a VPN tool like Hamachi, ZeroTier, or Radmin VPN to simulate a LAN connection.*

## 2. Hosting the Game (Player 1)
1. Run `PlaySquabbles.bat`.
2. Enter your Name and click **Submit Name**.
3. Click **Multiplayer**.
4. Click **Generate Unique Game ID (Host)**.
5. The game will display your **Host IP** and **Game ID (Port)**.
   - Example: `Host IP: 192.168.1.5 | Game ID: 54321`
6. Click **Copy Host Details** and send this information to your friend (via Discord, WhatsApp, etc.).
7. Wait for your friend to join.

## 3. Joining the Game (Player 2)
1. Run `PlaySquabbles.bat`.
2. Enter your Name and click **Submit Name**.
3. Click **Multiplayer**.
4. In the **Join Section**:
   - **Host IP**: Enter the IP address Player 1 gave you (e.g., `192.168.1.5`).
   - **Game ID**: Enter the Port number Player 1 gave you (e.g., `54321`).
5. Click **Join Game**.

## Troubleshooting
- **"Connection Failed"**: 
  - Ensure both computers are on the same network.
  - **Windows Firewall** might be blocking the connection. 
    - Try turning off the firewall temporarily on the Host computer, or allow "Java(TM) Platform SE binary" through the firewall.
- **"Address already in use"**: 
  - Restart the game and try hosting again to generate a new port.
