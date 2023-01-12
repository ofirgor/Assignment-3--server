package bgu.spl.net.impl.stomp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * class Manager contains and responsible for all the different hashMaps we need
 */
public class Manager {
    private ConcurrentHashMap<Integer, HashMap<String,Integer>> usersChannels = new ConcurrentHashMap<>();//key is client id
    private ConcurrentHashMap<String, String> userNameAndPass = new ConcurrentHashMap<>();
    public HashMap<Integer, HashMap<String,Integer>> getUsersChannelsMap(){
        return new HashMap<>(usersChannels);
    }
    public void addUser(Integer user) {
        usersChannels.put(user, new HashMap<String, Integer>());
    }
    /**
     * adds channel to user's channels list
     */
    public void subscribeUser(Integer user, String channel, Integer subscriptionId) throws FrameException{
        if (usersChannels.get(user) != null) {
            usersChannels.get(user).put(channel,subscriptionId);
        }
        else
            throw new FrameException("user name doesn't exist");
    }

    /**
     * removes channel from user's channels list
     */
    public void unsubscribeUser(Integer user, String subscriptionId, StompFrame frame) throws FrameException{
        boolean removed = false;
        Integer subId = parseToInt(subscriptionId,frame);
        HashMap<String, Integer> topics = usersChannels.get(user);
        for (Map.Entry<String, Integer> topic : topics.entrySet()) {
            if (topic.getValue().equals(subId)) {
                topics.remove(topic.getKey());
                removed = true;
                break;
            }
        }
        if (!removed)
            throw new FrameException("user is not subscribed to that channel", frame);
    }


    /**
     *
     * empty the user's topics and removes it user from all topics
     */
    public void emptyUserChannels(Integer user){
        usersChannels.get(user).clear();
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

    /**
     *
     * adds new channel to the list and returns the subscription I'd
     */
    public Integer addNewChannel(Integer user,String channelName, StompFrame frame) throws FrameException{
        Integer subId = parseToInt(channelName,frame);
        HashMap<String, Integer> ch = new HashMap<>();
        ch.put(channelName, subId);
        usersChannels.putIfAbsent(user,ch);
        Set<Integer> users = new HashSet<>();
        users.add(user);
        return subId;
    }
    public Integer parseToInt(String str, StompFrame frame) throws FrameException {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                throw new FrameException("not valid id", frame);
            }
        }
        return Integer.parseInt(str);
    }
    public Integer getSubscriptionId(Integer user, String channelName, StompFrame frame) throws FrameException{
        if(usersChannels.get(user).get(channelName) != null)
            return usersChannels.get(user).get(channelName);
        throw new FrameException("user is not subscribed to that channel", frame);
    }

}
