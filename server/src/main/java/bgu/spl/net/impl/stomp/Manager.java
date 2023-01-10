package bgu.spl.net.impl.stomp;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * class Manager contains and responsible for all the different hashMaps we need
 */
public class Manager {
    private HashMap<String, Set<Integer>> channels; //key is the name of the topic, value is all clients subscribed to that topic
    private HashMap<Integer, HashMap<String,Integer>> usersChannels;//key is client id
    private HashMap<String, String> userNameAndPass;


    public Manager(HashMap<String,Set<Integer>> channels, HashMap<Integer, HashMap<String,Integer>> usersChannels){
        this.channels = channels;
        this.usersChannels = usersChannels;
    }
    public HashMap<String, Set<Integer>> getChannelsMap(){
        return channels;
    }
    public HashMap<Integer, HashMap<String,Integer>> getUsersChannelsMap(){
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
    public void subscribeUser(Integer user, String channel, Integer subscriptionId) throws FrameException{
        if (channels.containsKey(channel)) {
            usersChannels.get(user).put(channel,subscriptionId);
            channels.get(channel).add(user);
        }
        else
            throw new FrameException("'" +channel+ "'" + "is not a valid topic");
    }
    public void removeUserFromChannels(Integer user){
        for (String ch: usersChannels.get(user).keySet()) {
            channels.get(ch).remove(user);
        }

    }
    /**
     * removes channel from user's channels list
     */
    public void removeChannel(Integer user, Integer subscriptionId, StompFrame frame) throws FrameException{
        String channel = "";
        HashMap<String,Integer> topics = usersChannels.get(user);
        for (Map.Entry<String,Integer> topic: topics.entrySet()){
            if (topic.getValue().equals(subscriptionId))
                channel= topic.getKey();
        }
        if (channel == "")
            throw new FrameException("channel doesn't exist", frame);
        else {
            usersChannels.get(user).remove(channel);
            channels.get(channel).remove(user);
        }
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
    public Integer getSubscriptionId(Integer user,String id, StompFrame frame) throws FrameException{
        Integer subId = parseToInt(id,frame);
        if (usersChannels.get(user).keySet().contains(subId))
            return subId;
        else
            throw new FrameException("subscription id doesn't exist", frame);
    }
    public Integer parseToInt(String str, StompFrame frame) throws FrameException {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                throw new FrameException("not valid id", frame);
            }
        }
        return Integer.parseInt(str);
    }

}
