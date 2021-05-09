package server;

import com.blogspot.debukkitsblog.net.Datapackage;
import com.blogspot.debukkitsblog.net.Executable;
import com.blogspot.debukkitsblog.net.Server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

@SuppressWarnings("BusyWait")
public class MainServer extends Server {

    String[] players = {"", ""};
    private boolean ingame = false;

    ArrayList<int[]> shots = new ArrayList<>();
    ArrayList<int[]> aliens = new ArrayList<>();
    // 0 = x, 1 = y, 2 = aliennummer, 3 = visible
    ArrayList<int[]> bombs = new ArrayList<>();
    // 0 = x, 1 = y
    public int[][] playerpos = {{270, 280, 1}, {50, 280, 1}};

    private int direction = -1;
    private int deaths = 0;

    public MainServer() {
        super(25598, true, true, false, true);
    }

    @Override
    public void preStart() {

        registerMethod("NEW_SHOT", new Executable() {
            @Override
            public void run(Datapackage pack, Socket socket) {
                // Pack Content: 1 = x Position, 2 = y Position
                if(checkshotamount((int) pack.get(3))) {
                    shots.add(new int[]{(int) pack.get(1), (int) pack.get(2), (int) pack.get(3)});
                }
                sendReply(socket, "Received");
            }
        });

        registerMethod("AUTH", new Executable() {
            @Override
            public void run(Datapackage pack, Socket socket) {
                // Pack Content: 1 = Username
                int index = -1;
                for(int i = 0; i < players.length; i ++){
                    if(players[i].isEmpty()){
                        players[i] = (String) pack.get(1);
                        System.out.println(i);
                        index = i;
                        break;
                    }
                }
                sendReply(socket, index);
            }
        });

        registerMethod("POS", new Executable() {
            @Override
            public void run(Datapackage pack, Socket socket) {
                // Pack Content: 1 = PlayerID, 2 = X Cord, 3 = Y Cord
                int playerid = (int) pack.get(1);
                playerpos[playerid][0] = (int) pack.get(2);
                playerpos[playerid][1] = (int) pack.get(3);
                sendReply(socket, "Received");
            }
        });
    }

    @Override
    public void onClientRemoved(RemoteClient remoteClient) {
        System.out.println(remoteClient.getId());
        for(String player : players) {
            if(player.equals(remoteClient.getId())) {
                player = null;
                break;
            }
        }
    }

    private boolean checkshotamount(int userid) {
        int count = 0;
        for(int[] shot : shots) {
            if(shot[2] == userid) count ++;
        }
        return count <= 5;
    }

    private int checkuser() {
        if(getClientCount() == 2) {
            ingame = true;
            return 1;
        } else if(playerpos[0][2] == 1 && playerpos[1][2] == 1) {
            return 3;
        } else if(getClientCount() != 2 && ingame) {
            return 2;
        } else {
            return 0;
        }
    }

    public void AlienMovement() {
        // Alien Movement
        for(int i = 0; i < aliens.size(); i ++) {
            if((aliens.get(i)[0] != -1) && (aliens.get(i)[1] != -1)){
                int x = aliens.get(i)[0];
                if(x >= Commons.BOARD_WIDTH - Commons.BORDER_RIGHT && direction != -1) {
                    direction = -1;
                    for(int[] alien : aliens) {
                        alien[1] += Commons.GO_DOWN;
                    }
                }
                if(x <= Commons.BORDER_LEFT && direction != 1) {
                    direction = 1;
                    for(int[] alien : aliens) {
                        alien[1] += Commons.GO_DOWN;
                    }
                }
            }
        }
        for(int[] alien : aliens) {
            alien[0] += direction;
        }
    }

    public void ClientSync() {
        broadcastMessage(new Datapackage("BOMBS", this.bombs));
        broadcastMessage(new Datapackage("ALIENS", this.aliens, this.shots));
        broadcastMessage(new Datapackage("POS", this.playerpos[0][0], this.playerpos[0][1], this.playerpos[1][0], this.playerpos[1][1]));
    }

    public void run() throws InterruptedException {
        while(checkuser() == 0){
            System.out.println("Not enough players");
            broadcastMessage(new Datapackage("GAME_INFO", 0));
            Thread.sleep(1000);
        }
        System.out.println("Enough Players");
        broadcastMessage(new Datapackage("GAME_INFO", 1));

        for(int i = 0; i < 4; i ++) {
            for(int j = 0; j < 6; j ++) {
                int x = Commons.ALIEN_INIT_X + 18 * j;
                int y = Commons.ALIEN_INIT_Y + 18 * i;
                aliens.add(new int[]{x, y, 0});
                bombs.add(new int[]{x, y, 0});
            }
        }

        Thread Aliens = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    AlienMovement();
                    try {
                        Thread.sleep(Commons.DELAY);
                    } catch (InterruptedException ignored) {}
                }
            }
        });
        Aliens.start();

        Thread Sync = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    ClientSync();
                    try {
                        Thread.sleep(Commons.DELAY);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        Sync.start();

        while(checkuser() == 1) {
            Thread.sleep(10);

            // Securing Player Movement
            for(int[] pos : playerpos){
                if(pos[1] != 280) pos[1] = 280;
                if(pos[0] <= 2 ) pos[0] = 2;
                if(pos[0] >= 328) pos[0] = 328;
            }

            // Shot Movement and Removing, when Hitting Top
            if(shots.size() != 0) {
                for(int i = 0; i < shots.size(); i ++) {
                    int[] shot = shots.get(i);
                    shot[1] -= 4;
                    boolean removed = false;
                    for(int[] alien : aliens) {
                        if(!removed && alien[2] != 1
                                && shot[0] >= alien[0]
                                && shot[0] <= (alien[0] + Commons.ALIEN_WIDTH)
                                && shot[1] >= alien[1]
                                && shot[1] <= (alien[1] + Commons.ALIEN_HEIGHT))
                        {
                            alien[2] = 1;
                            deaths ++;
                            shots.remove(i);
                            removed = true;
                        }
                    }
                    if(!removed && shots.get(i)[1] <= 0) {
                        //noinspection SuspiciousListRemoveInLoop
                        shots.remove(i);
                    } else if(!removed) {
                        shots.set(i, shot);
                    }
                }
            }

            Random generator = new Random();

            for(int i = 0; i < aliens.size(); i ++ ) {
                int[] bomb = bombs.get(i);
                if(generator.nextInt(80) == Commons.CHANCE && aliens.get(i)[2] == 0 && bombs.get(i)[2] == 0) {
                    bomb[2] = 1;
                    bomb[0] = aliens.get(i)[0];
                    bomb[1] = aliens.get(i)[1];
                }
                int bombX = bomb[0];
                int bombY = bomb[1];
                // Check if bomb is destroying a player
                for(int[] player : playerpos) {
                    if(player[2] == 1
                            && bomb[2] == 1
                            && bombX >= player[0]
                            && bombX <= (player[0] + Commons.PLAYER_WIDTH)
                            && bombY >= player[1]
                            && bombY <= (player[1] + Commons.PLAYER_HEIGHT))
                    {
                        bomb[2] = 0;
                        player[2] = 0;
                    }
                }
                // Move Bomb
                if(bomb[2] == 1) {
                    bomb[1] += 1;
                    if(bomb[1] >= Commons.GROUND - Commons.BOMB_HEIGHT) bomb[2] = 0;
                }
                bombs.set(i, bomb);
            }
        }
        // noinspection InfiniteLoopStatement
        while (true) {
            Sync.stop();
            broadcastMessage(new Datapackage("GAME_INFO", checkuser()));
        }
    }

    public static void main(String[] args) {
        MainServer server = new MainServer();
        try {
            server.run();
        } catch (InterruptedException ignored) {

        }
    }
}
