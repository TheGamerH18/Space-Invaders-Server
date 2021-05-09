package server;

import com.blogspot.debukkitsblog.net.Datapackage;
import com.blogspot.debukkitsblog.net.Executable;
import com.blogspot.debukkitsblog.net.Server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class MainServer extends Server {

    String[] players = {"", ""};


    ArrayList<int[]> shots = new ArrayList<>();
    ArrayList<int[]> aliens = new ArrayList<>();
    // 0 = x, 1 = y
    public int[][] playerpos = {{270, 280}, {50, 280}};

    private int direction = -1;

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
                System.out.println(Arrays.deepToString(playerpos));
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

    private boolean checkuser() {
        return getClientCount() == 2;
    }

    public void AlienMovement() {
        // Alien Movement
        for(int i = 0; i < aliens.size(); i ++) {
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
        for(int[] alien : aliens) {
            alien[0] += direction;
        }
    }

    public void run() throws InterruptedException {
        while(!checkuser()){
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
                aliens.add(new int[]{x, y});
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

        while(checkuser()) {
            Thread.sleep(10);

            // Securing Player Movement
            for(int[] pos : playerpos){
                if(pos[1] != 280) pos[1] = 280;
                if(pos[0] <= 2 ) pos[0] = 2;
                if(pos[0] >= 328) pos[0] = 328;
            }

            // Shot Movement and Removing, when Hitting Top
            for(int i = 0; i < shots.size(); i ++) {
                shots.get(i)[1] -= 4;
                if(shots.get(i)[1] <= 0) {
                    //noinspection SuspiciousListRemoveInLoop
                    shots.remove(i);
                }
            }

            broadcastMessage(new Datapackage("ALIENS", this.aliens));
            broadcastMessage(new Datapackage("SHOTS", this.shots));
            broadcastMessage(new Datapackage("POS", this.playerpos[0][0], this.playerpos[0][1], this.playerpos[1][0], this.playerpos[1][1]));
            System.out.println(Arrays.deepToString(this.playerpos));
        }
        broadcastMessage(new Datapackage("GAME_INFO", 2));
        // noinspection InfiniteLoopStatement
        while (true) {

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
