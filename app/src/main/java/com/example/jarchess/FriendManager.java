package com.example.jarchess;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.jarchess.jaraccount.JarAccount;
import com.example.jarchess.online.JSONCompiler.JSONAccount;
import com.example.jarchess.online.networking.DataSender;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;


public class FriendManager extends Fragment {

    private FriendManagerCommunicator callback;
    private ArrayAdapter<String> listAdapter;
    private Button removeFriendButton;
    private Button addFriendButton;
    private ListView friendList;
    private int indexOfLastSelected;
    private boolean listIsEmpty;
    private ArrayList<String> friendInfo;
    private ArrayList<String> mainList;
    private ArrayList<String> friendsList;
    private ArrayList<String> requestsList;
    private String TAG = "FriendManager";


    public FriendManager() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_friend_manager, container, false);


        removeFriendButton = view.findViewById(R.id.button_remove);
        addFriendButton = view.findViewById(R.id.button_add);
        friendList = view.findViewById(R.id.list_friend);
        indexOfLastSelected = -1;
        ArrayList<String> supposedlyEmptyList = new ArrayList<>();
        listAdapter = new ArrayAdapter<>
                (getContext(), R.layout.list_item_style, supposedlyEmptyList);
        mainList = new ArrayList<>();

        friendsToAdd();
        populateList();

        friendList.setAdapter(listAdapter);
        setupListeners();

        return view;
    }

    private void setupListeners() {
        final int duration = Toast.LENGTH_SHORT;
        friendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                indexOfLastSelected = position;
                int rSize = requestsList.size();
                if (indexOfLastSelected < rSize) { removeFriendButton.setText("Remove Friend Request");
                } else {removeFriendButton.setText("Remove Friend");}
            }
        });





        removeFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rSize = requestsList.size();
                int fSize = friendsList.size();
                if (fSize + rSize == 0) {
                    Toast toast = Toast.makeText(v.getContext(),
                            "Cannot Remove From Empty List", duration);
                    toast.show();
                } else if (indexOfLastSelected < 0) {
                    Toast toast = Toast.makeText(v.getContext(),
                            "Please Select a Friend to Remove", duration);
                    toast.show();
                } else {
                    String name = null;
                    if (indexOfLastSelected < rSize) { name = requestsList.get(indexOfLastSelected);
                    } else { name = friendsList.get(indexOfLastSelected - rSize); }

                    if (callback.onRemoveFriend(name)) {

                        Toast toast = Toast.makeText(v.getContext(),
                                "Friend Removed", duration);
                        toast.show();
                        requestsList.clear();
                        friendsList.clear();
                        mainList.clear();
                        friendsToAdd();
                        populateList();

                    } else {
                        Toast toast = Toast.makeText(v.getContext(),
                                "Friend Removal Failed", duration);
                        toast.show();
                    }
                }
            }
        });

        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rSize = requestsList.size();
                int fSize = friendsList.size();
                if (indexOfLastSelected < 0 || indexOfLastSelected >= rSize) {
                    FragmentTransaction transaction = MainActivity.fragmentManager.beginTransaction();
                    FriendAdder friendAdder = new FriendAdder();
                    transaction.replace(R.id.fragmentHole, friendAdder);
                    transaction.addToBackStack("FriendAdder");
                    transaction.commit();
                } else {
                    String name = requestsList.get(indexOfLastSelected);

                    if (callback.onAcceptFriendRequest(name)) {
                        Toast toast = Toast.makeText(v.getContext(),
                                "Friend Request Accepted", duration);
                        toast.show();
                        requestsList.clear();
                        friendsList.clear();
                        mainList.clear();
                        friendsToAdd();
                        populateList();
                    }  else {
                        Toast toast = Toast.makeText(v.getContext(),
                                "Accept Friend Request Failed", duration);
                        toast.show();
                    }
                }
            }
        });


    }

    private void populateList() {
        friendsList = new ArrayList<>();

        JSONObject requestObject = new JSONObject();
        JSONObject data = null;
        JSONObject user = null;

        DataSender sender = new DataSender();

        try {
            requestObject = sender.send(new JSONAccount().getFriendsList(JarAccount.getInstance().getName()));
            Log.i(TAG, requestObject.toString());
            data = new JSONObject(requestObject.getString("friends"));
        } catch (IOException e1) {
            e1.printStackTrace();
        }catch (JSONException e2){
            e2.printStackTrace();
        }
        int count = 0;
        try {
            count = Integer.parseInt(requestObject.getString("count"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < count; i++){

            try {
                user = new JSONObject(data.getString("friend" + i));
                String username = user.getString("username");
                String displayString = (i + 1) + ")    " + username;
                friendsList.add(username);
                mainList.add(displayString);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        listAdapter.clear();
        listAdapter.addAll(mainList);
        listAdapter.notifyDataSetChanged();


    }

    private void friendsToAdd() {
        requestsList = new ArrayList<>();

        JSONObject requestObject = new JSONObject();
        JSONObject data = null;
        JSONObject user = null;

        DataSender sender = new DataSender();

        try {
            requestObject = sender.send(new JSONAccount().getFriendRequests(JarAccount.getInstance().getName()));
            Log.i(TAG, requestObject.toString());
            data = new JSONObject(requestObject.getString("friends"));
        } catch (IOException e1) {
            e1.printStackTrace();
        }catch (JSONException e2){
            e2.printStackTrace();
        }
        int count = 0;
        try {
            count = Integer.parseInt(requestObject.getString("count"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < count; i++){

            try {
                user = new JSONObject(data.getString("friend" + String.valueOf(i)));
                String username = user.getString("username");
                String displayString = " - Friend Request ->  " + username;
                requestsList.add(username);
                mainList.add(displayString);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        listAdapter.clear();
        listAdapter.addAll(mainList);
        listAdapter.notifyDataSetChanged();

    }



    public void setCommunicator(FriendManagerCommunicator callback) {
        this.callback = callback;
    }

    public interface FriendManagerCommunicator {
        ArrayList<String> onManagerLoad();
        boolean onRemoveFriend(String name);
        boolean onAcceptFriendRequest(String name);

    }


}

