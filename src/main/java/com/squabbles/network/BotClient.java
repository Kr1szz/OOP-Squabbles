package com.squabbles.network;

import com.squabbles.model.Icon;

import java.util.List;
import java.util.Random;

public class BotClient extends ServerClient {

    private boolean running = false;
    private Random random = new Random();
    private Thread botThread;
    private int difficultyLevel = 1; // 1: Simple, 2: Advanced

    public BotClient(int playerId, LobbyServer lobby, int difficulty) {
        super(null, playerId, lobby); // No socket for bot
        this.difficultyLevel = difficulty;
    }

    @Override
    public void sendMessage(String message) {
        // Bot receives message from server (GameRoom)
        // Parse state updates to know what cards are in play
        if (message.startsWith(NetworkProtocol.MSG_UPDATE_CARDS)) {
            // Trigger bot reaction logic
        } else if (message.startsWith(NetworkProtocol.MSG_GAME_OVER)) {
            stopBotLogic();
        }
    }

    public void startBotLogic() {
        running = true;
        botThread = new Thread(this::botLoop);
        botThread.start();
    }

    public void stopBotLogic() {
        running = false;
        if (botThread != null) {
            botThread.interrupt();
        }
    }

    private void botLoop() {
        while (running) {
            try {
                // Reaction time based on difficulty
                int reactionTime = (difficultyLevel == 1) ? 2000 + random.nextInt(2000) : 800 + random.nextInt(1000);
                Thread.sleep(reactionTime);

                if (!running)
                    break;

                attemptMatch();

            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void attemptMatch() {
        // Logic to find a match
        // Since BotClient runs on the server, it has direct access to its cards via
        // getters in ServerClient
        // BUT, ServerClient logic usually relies on GameRoom to hold the state.
        // In my design, ServerClient holds 'currentCard'. GameRoom holds 'centerCard'.
        // Bot needs access to GameRoom to see center card?
        // Or we can rely on the state sent via sendMessage (which we'd need to parse).

        // Better approach: Give BotClient access to GameRoom (it has it via
        // setGameRoom)
        // But GameRoom fields are private.

        // Let's cheat a bit for the bot and assume it can "see" the cards if we expose
        // them or if we parse the last message.
        // For simplicity, let's make the bot try to find a match on its current card vs
        // center card.
        // We need a way to get the center card from GameRoom or store it when received.

        // Actually, let's just make the bot pick a random icon from its card and try
        // it.
        // If it's a valid match, it works. If not, it fails (maybe penalty).
        // A smart bot would find the intersection.

        if (getCurrentCard() == null)
            return;

        // To do this properly without changing GameRoom too much, let's assume the bot
        // is "perfect" but slow,
        // or "random" and spammy.

        // Let's implement a "Perfect but Slow" bot for now.
        // It needs to know the center card.
        // I'll add a field to BotClient to store the center card, updated via
        // sendMessage -> parse.

        // Wait, I can't easily parse the string back to objects without a parser.
        // Let's just make the bot pick a random icon from its own card.
        // If it happens to match, great.

        List<Icon> myIcons = getCurrentCard().getIcons();
        if (myIcons.isEmpty())
            return;

        Icon selected = myIcons.get(random.nextInt(myIcons.size()));

        // Simulate sending the match attempt
        // We can call processMessage directly or call gameRoom.handleMatch directly.
        // Since we are on the server, we can call gameRoom.handleMatch directly if we
        // have access.
        // ServerClient has a 'processMessage' method but it expects a string.

        // Let's use the string interface to be consistent.
        String msg = NetworkProtocol.MSG_MATCH_ATTEMPT + " " + selected.getId();
        // We need to inject this into the processing logic.
        // ServerClient.processMessage is private.
        // Let's make it protected or public, or just override run() (but run is for
        // socket loop).

        // Actually, I can just call a method on myself that mimics receiving a message
        // from the "client" side of the bot.
        super.processMessage(msg);
    }

    // Need to expose processMessage to subclass or make it protected in
    // ServerClient
    // I will update ServerClient to make processMessage protected.
}
