package bgu.spl.net.impl.stomp;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * class Manager contains and responsible for all the different hashMaps we need
 */
public class Manager {
    private HashMap<String, Set<Integer>> channels; //key is the name of the topic, value is all clients subscribed to that topic
    private HashMap<Integer, Set<String>> usersChannels;//key is client id
    private HashMap<String, String> userNameAndPass;


    public Manager(HashMap<String,Set<Integer>> channels, HashMap<Integer, Set<String>> usersChannels){
        this.channels = channels;
        this.usersChannels = usersChannels;
    }
    public HashMap<String, Set<Integer>> getChannelsMap(){
        return channels;
    }
    public HashMap<Integer, Set<String>> getUsersChannelsMap(){
        return usersChannels;
    }
    public void addChannel(String channel){
        channels.put(channel,null);
    }
    public void addUser(Integer user) {
        usersChannels.put(user, null);
    }
    /**
     * adds channel to user's channels list
     */
    public void setUsersChannels(Integer user, String channel){
        usersChannels.get(user).add(channel);
        channels.get(channel).add(user);
    }

    /**
     * removes channel from user's channels list
     */
    public void removeChannel(Integer user, String channel){
        usersChannels.get(user).remove(channel);
        channels.get(channel).remove(user);
    }
    public boolean isUserInChannel(Integer user, String channel){
        return channels.get(channel).contains(user);
    }

    /**
     *
     * empty the user's topics and removes it user from all topics
     */
    public void emptyUserChannels(Integer user){
        usersChannels.get(user).clear();
        for(Map.Entry<String,Set<Integer>> entry: channels.entrySet()){
            Set<Integer> connectionIds = entry.getValue();
            connectionIds.remove(user);
        }
    }
    public void addUserNameAndPass(String userName, String pass){
        userNameAndPass.put(userName,pass);
    }
    public boolean isUserNameExist(String userName){
        return userNameAndPass.containsKey(userName);
    }

    public boolean isCorrectPass(String userName, String pass){
        return userNameAndPass.get(userName).equals(pass);
    }

}
