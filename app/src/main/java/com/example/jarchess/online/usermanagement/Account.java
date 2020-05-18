package com.example.jarchess.online.usermanagement;

import android.util.Log;

import com.example.jarchess.jaraccount.JarAccount;
import com.example.jarchess.jaraccount.JarAccountSetting;
import com.example.jarchess.online.JSONCompiler.JSONAccount;
import com.example.jarchess.online.networking.DataSender;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Account {
    //    private String username;
//    private String password;
    private static final String TAG = "Account";
    public static final int MAX_USERNAME_LENGTH = 10;
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 64;
    private JSONAccount jsonAccount;
    private DataSender dataSender;

    public Account() {
        this.jsonAccount = new JSONAccount();
        this.dataSender = new DataSender();
    }

    protected Account(JSONAccount jsonAccount, DataSender dataSender) {
        this.jsonAccount = jsonAccount;
        this.dataSender = dataSender;
    }

    public boolean changePassword(String oldPass, String newPass) {
        boolean status = false;

        if (!isValidPassword(newPass)){
            return false;
        }

        String hashedOldPass = getPasswordHash(oldPass);
        String hashedNewPass = getPasswordHash(newPass);

        if (hashedOldPass == null || hashedNewPass == null) {
            return false;
        }

        JSONObject jsonObject = jsonAccount.changePassword(JarAccount.getInstance().getName(),
                hashedOldPass, hashedNewPass);

        try {
            JSONObject jsonResponse = dataSender.send(jsonObject);
            String statusResp = (String) jsonResponse.get("status");
            Log.i(TAG, "Status response: " + statusResp);
            if (statusResp.equals("success")) {
                status = true;
                Log.i(TAG, "Password change status: " + (String) jsonResponse.get("success"));
            } else {
                status = false;
                Log.i(TAG, "Password change status: " + (String) jsonResponse.get("success"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            status = false;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return status;

    }

    //TODO needs server implementation
    public <T> T getAccountInfo(JarAccountSetting<T> jas, String username, String signonToken) {
        try {

            if (username == null || username.isEmpty() || signonToken == null || signonToken.isEmpty() || jas == null) {
                return jas.getValue();
            }
            JSONObject jsonObject = jsonAccount.getAccountInfo(jas.getKey(), jas.getValue(), jas.getType(), username, signonToken);
            Log.d(TAG, "getAccountInfo: jsonObject = " + jsonObject.toString());

            JSONObject jsonResponse = dataSender.send(jsonObject);

            Log.d(TAG, "getAccountInfo: response = " + jsonResponse);
            if (jsonResponse.get(jas.getKey()) == null) {
                Log.d(TAG, "getAccountInfo: " + jas.getKey() + " was null ");
                return jas.getValue();
            }
            Object returnValue = jas.getValue();
            switch (jas.getType()) {

                case STRING:
                    // T is String
                    returnValue = jsonResponse.getString(jas.getKey());
                    break;
                case BOOLEAN:
                    returnValue = jsonResponse.getBoolean(jas.getKey());
                    break;
                case INTEGER:
                    returnValue = jsonResponse.getInt(jas.getKey());
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + jas.getType());
            }

            return (T) returnValue;

        } catch (Exception e) {
            Log.e(TAG, "getAccountInfo: ", e);
            return jas.getValue();
        }
    }

    public ArrayList<String> getFriendsList() {
        ArrayList<String> friendsList = new ArrayList<>();
        JSONObject reqobj = new JSONAccount().getFriendRequests(JarAccount.getInstance().getName());
        DataSender sender = new DataSender();
        JSONObject responseObj = null;
        JSONObject friends = null;
        JSONObject user = null;
        int count = 0;
        try {
            responseObj = sender.send(reqobj);
            count = Integer.parseInt(responseObj.getString("count"));
            friends = new JSONObject(responseObj.getString("friends"));
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < count; i++) {

            try {
                user = new JSONObject(friends.getString("friend" + i));
                String username = user.getString("username");
                String displayString = (i + 1) + ")    " + username;
                friendsList.add(username);
                //friendsList.add(displayString);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return friendsList;
    }

    private String getPasswordHash(String password) {
        String generatedPassword = null;
        try {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            //Add password bytes to digest
            md.update(password.getBytes());
            //Get the hash's bytes
            byte[] bytes = md.digest();
            //This bytes[] has bytes in decimal format;
            //Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            //Get complete hashed password in hex format
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    private boolean isValidPassword(String input) {
        String specialChars = "~`!@#$%^&*()-_=+\\|[{]};:'\",<.>/?";
        char currentCharacter;
        boolean numberPresent = false;
        boolean upperCasePresent = false;
        boolean lowerCasePresent = false;
        boolean specialCharacterPresent = false;

        if (input == null) {
            return false;
        }

        for (int i = 0; i < input.length(); i++) {
            currentCharacter = input.charAt(i);
            if (Character.isDigit(currentCharacter)) {
                numberPresent = true;
            } else if (Character.isUpperCase(currentCharacter)) {
                upperCasePresent = true;
            } else if (Character.isLowerCase(currentCharacter)) {
                lowerCasePresent = true;
            } else if (specialChars.contains(String.valueOf(currentCharacter))) {
                specialCharacterPresent = true;
            }

            if (((lowerCasePresent ? 1 : 0) + (upperCasePresent ? 1 : 0) + (specialCharacterPresent ? 1 : 0) + (numberPresent ? 1 : 0)) >= 3) {
                Log.i(TAG, "Password is valid");
                return true;
            }
        }
        return false;
    }

    public boolean registerAccount(String username, String password) {
        boolean status = false;

        boolean result = isValidPassword(password);
        if (password == null) {
            Log.i(TAG, "password was null");
            return false;
        } else {
            Log.i(TAG, "String result = " + result);
            Log.i(TAG, "password length = " + password.length());
        }

        if (username == null || username.length() < MIN_USERNAME_LENGTH || username.length() > MAX_USERNAME_LENGTH) {
            Log.i(TAG, "registerAccount: username length of " + username.length() + " not in [" + MIN_USERNAME_LENGTH + ", " + MAX_USERNAME_LENGTH + "].");
            return false;
        }
        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            Log.i(TAG, "registerAccount: password length of " + password.length() + " not in [" + MIN_PASSWORD_LENGTH + ", " + MAX_PASSWORD_LENGTH + "].");
            return false;
        }
        if (!isValidPassword(password)){
            Log.i(TAG, "registerAccount: password was invalid");
            return false;
        }

        String hashedPass = getPasswordHash(password);
        if (hashedPass == null) {
            return false;
        }

        JSONObject jsonObject = jsonAccount.registerAccount(username, hashedPass);
        try {
            JSONObject jsonResponse = dataSender.send(jsonObject);
            Log.i(TAG, "registerAccount: sending request: " + jsonObject.toString());
            String statusResp = (String) jsonResponse.get("status");
            Log.i(TAG, "Status response: " + statusResp);
            if (statusResp.equals("success")) {
                status = true;
                Log.i(TAG, "Registration Status: " + (String) jsonResponse.get("success"));
            } else {
                status = false;
                Log.i(TAG, "Registration Status: " + (String) jsonResponse.get("success"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            status = false;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return status;
    }

    //TODO build and test
    public <T> boolean saveAccountInfo(JarAccountSetting<T> jas, String username, String signonToken) {

        try {

            if (username == null || username.isEmpty() || signonToken == null || signonToken.isEmpty() || jas == null) {
                return false;
            }
            JSONObject jsonObject = jsonAccount.saveAccountInfo(jas.getKey(), jas.getValue(), jas.getType(), username, signonToken);

            Log.d(TAG, "saveAccountInfo: jsonObject = " + jsonObject.toString());
            JSONObject jsonResponse = dataSender.send(jsonObject);
            String statusResp = null;
            statusResp = (String) jsonResponse.get("status");
            Log.i(TAG, "Status response: " + statusResp);
            if (statusResp.equals("success")) {
                Log.d(TAG, "signonTokenIsValid() returned: " + true);
                return true;
            } else {
                Log.d(TAG, "signonTokenIsValid() returned: " + false);
                return false;
            }

        } catch (Exception e) {
            Log.e(TAG, "saveAccountInfo: ", e);
            return false; // failed save
        }

    }

    public boolean signin(String username, String password) {
        Log.d(TAG, "signin() called with: username = [" + username + "], password = [" + password + "]");
        Log.d(TAG, "signin is running on thread: " + Thread.currentThread().getName());
        boolean status = false;

        String hashedPass = getPasswordHash(password);
        if (hashedPass == null) {
            return false;
        }

        if (username == null) {
            Log.i(TAG, "signin: username was null");
            Log.d(TAG, "signin() returned: " + false);
            return false;
        } else if (username.equals("")) {
            Log.i(TAG, "signin: username was empty string");
            Log.d(TAG, "signin() returned: " + false);
            return false;
        }
        JSONObject jsonObject = jsonAccount.signin(username, hashedPass);


        try {
            JSONObject jsonResponse = dataSender.send(jsonObject);
            String statusResp = jsonResponse == null ? "" : (String) jsonResponse.get("status");
            if (statusResp.equals("success")) {
                status = true;
                final JarAccount account = JarAccount.getInstance();
                account.setSignonToken(jsonResponse.get("token").toString());
                account.setName(username);
                account.loadAccountFromJson(jsonResponse);
                account.setPasswordHash(hashedPass);
                Log.i("Signin", (String) jsonResponse.get("status"));

            } else {
                status = false;
                Log.i("Signin", "Logon failure");
            }
        } catch (IOException e) {
            e.printStackTrace();
            status = false;
        } catch (JSONException e) {
            e.printStackTrace();
            status = false;
        }

        return status;
    }

    public boolean signinWithHash(String username, String hashedPass) {
        Log.d(TAG, "signinWithHash() called with: username = [" + username + "], hash = [" + hashedPass + "]");
        Log.d(TAG, "signinWithHash is running on thread: " + Thread.currentThread().getName());
        boolean status = false;

        if (hashedPass == null) {
            return false;
        }

        if (username.equals("")) {
            Log.i(TAG, "signinWithHash: username was empty string");
            Log.d(TAG, "signinWithHash() returned: " + false);
            return false;
        } else if (username == null) {
            Log.i(TAG, "signinWithHash: username was null");
            Log.d(TAG, "signinWithHash() returned: " + false);
            return false;
        }
        JSONObject jsonObject = jsonAccount.signin(username, hashedPass);


        try {
            JSONObject jsonResponse = dataSender.send(jsonObject);
            String statusResp = jsonResponse == null ? "" : (String) jsonResponse.get("status");
            if (statusResp.equals("success")) {
                status = true;
                JarAccount account = JarAccount.getInstance();
                account.setSignonToken((String) jsonResponse.get("token"));
                account.setName(username);
                Log.i("signinWithHash", (String) jsonResponse.get("status"));
            } else {
                status = false;
                Log.i("signinWithHash", "Logon failure");
            }
        } catch (IOException e) {
            e.printStackTrace();
            status = false;
        } catch (JSONException e) {
            e.printStackTrace();
            status = false;
        }

//        JarAccount.getInstance().loadAccountFromServer();
        return status;
    }

    public boolean signout(String username, String signonToken) {
        Log.d(TAG, "signout() called with: username = [" + username + "], signonToken = [" + signonToken + "]");
        Log.d(TAG, "signout is running on thread: " + Thread.currentThread().getName());
        boolean status = false;
        if (username == null) {
            Log.i(TAG, "signout: username was null");
            Log.d(TAG, "signout() returned: " + false);
            return false;
        } else if (username.equals("")) {
            Log.i(TAG, "signout: username was empty string");
            Log.d(TAG, "signout() returned: " + false);
            return false;
        }
        JSONObject jsonObject = jsonAccount.signout(username, signonToken);

        JarAccount.getInstance().getJsonForLogout(jsonObject);

        try {
            JSONObject jsonResponse = dataSender.send(jsonObject);
            String statusResp = (String) jsonResponse.get("status");
            if (statusResp.equals("success")) {
                status = true;
                JarAccount.getInstance().setName(username);
                Log.i("signout", (String) jsonResponse.get("status"));
            } else {
                status = false;
                Log.i("Signout", "Logon failure");
            }
        } catch (IOException e) {
            e.printStackTrace();
            status = false;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return status;
    }

    public boolean verifySignin(String username, String hashedPass) {
        return signinWithHash(username, hashedPass);
    }

}
