package com.example.jarchess.online;

import android.util.Log;

import com.example.jarchess.JarAccount;
import com.example.jarchess.RemoteOpponentInfoBundle;
import com.example.jarchess.match.styles.YellowBlackYellowCircleAvatarStyle;
import com.example.jarchess.online.move.DatapackageQueue;
import com.example.jarchess.online.networking.GameIO;

import org.json.JSONException;
import org.json.JSONObject;

public class OnlineMatchInfoBundle {
    private RemoteOpponentInfoBundle remoteOpponentInfoBundle;
    private RemoteOpponentInfoBundle localOpponentInfoBundle;
    private JSONObject responseObject;
    private DatapackageQueue datapackageQueue;
    private String gameToken;
    private GameIO gameIO;
    private static final String TAG = "OnlineMatchInfoBundle";

    public OnlineMatchInfoBundle(JSONObject responseObject) {
        this.responseObject = responseObject;
        this.datapackageQueue = new DatapackageQueue();
        setRemoteOpponentInfoBundle();
        // TODO create and set the datapackage queue

        // establish connection
        this.gameIO = new GameIO(datapackageQueue, gameToken, remoteOpponentInfoBundle);
    }

    private void setRemoteOpponentInfoBundle(){
        String playerOne = "";
        String playerTwo = "";
        String gameToken = "";
        String playerOneColor = "";
        String playerTwoColor = "";
        String playerOneIp = "";
        String playerTwoIp = "";
        int playerOnePort = 0;
        int playerTwoPort = 0;
        try {
            playerOne = (String) responseObject.get("player_one");
            playerTwo = (String) responseObject.get("player_two");
            gameToken = (String) responseObject.get("game_token");
            playerOneColor = (String) responseObject.get("player_one_color");
            playerTwoColor = (String) responseObject.get("player_one_color");
            playerOneIp = (String) responseObject.get("player_one_ip");
            playerTwoIp = (String) responseObject.get("player_one_ip");
            playerOnePort = (int) responseObject.get("player_one_port");
            playerTwoPort = (int) responseObject.get("player_one_port");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(JarAccount.getInstance().getName().equals(playerOne)){
            remoteOpponentInfoBundle = new RemoteOpponentInfoBundle(playerTwo,YellowBlackYellowCircleAvatarStyle.getInstance(),playerOneColor, playerOneIp, playerOnePort);
            localOpponentInfoBundle = new RemoteOpponentInfoBundle(playerOne, YellowBlackYellowCircleAvatarStyle.getInstance(), playerOneColor, playerOneIp, playerOnePort);
        }else if(JarAccount.getInstance().getName().equals(playerTwo)){
            remoteOpponentInfoBundle = new RemoteOpponentInfoBundle(playerOne, YellowBlackYellowCircleAvatarStyle.getInstance(), playerOneColor, playerOneIp, playerOnePort);
            localOpponentInfoBundle = new RemoteOpponentInfoBundle(playerTwo,YellowBlackYellowCircleAvatarStyle.getInstance(),playerOneColor, playerOneIp, playerOnePort);
        }

        this.gameToken = gameToken;
    }

    public RemoteOpponentInfoBundle getBlackOpponentInfoBundle() {
        if(localOpponentInfoBundle.getColor().equals("black")){
            return localOpponentInfoBundle;
        }else{
            return remoteOpponentInfoBundle;
        }
    }

    public DatapackageQueue getDatapackageQueue() {
        Log.d(TAG, "getDatapackageQueue() called");
        Log.d(TAG, "getDatapackageQueue is running on thread: " + Thread.currentThread().getName());
        Log.d(TAG, "getDatapackageQueue() returned: " + datapackageQueue);
        return datapackageQueue;
    }

    public String getMatchToken() {
        return gameToken;
    }

    public RemoteOpponentInfoBundle getWhiteOpponentInfoBundle() {
        if(localOpponentInfoBundle.getColor().equals("white")){
            return localOpponentInfoBundle;
        }else{
            return remoteOpponentInfoBundle;
        }
    }
}
