package server;

import com.blogspot.debukkitsblog.net.Datapackage;
import com.blogspot.debukkitsblog.net.Executable;
import com.blogspot.debukkitsblog.net.Server;

import java.net.Socket;

public class MainServer extends Server {

    public MainServer() {
        super(25598, true, true, false, false);
    }

    @Override
    public void preStart() {
        registerMethod("POS", new Executable() {
            @Override
            public void run(Datapackage pack, Socket socket) {
                int playerid = (int) pack.get(1);

            }
        });
    }

    public static void main(String[] args) {
        MainServer server = new MainServer();
    }
}
