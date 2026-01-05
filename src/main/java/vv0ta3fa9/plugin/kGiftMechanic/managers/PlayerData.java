package vv0ta3fa9.plugin.kGiftMechanic.managers;

import java.util.HashSet;
import java.util.Set;

public class PlayerData {
    protected String playerName;
    protected Set<String> collectedGifts;
    protected boolean rewardClaimed;

    public PlayerData() {
        this.collectedGifts = new HashSet<>();
        this.rewardClaimed = false;
    }

    public PlayerData(String playerName) {
        this.playerName = playerName;
        this.collectedGifts = new HashSet<>();
        this.rewardClaimed = false;
    }

    public PlayerData(String playerName, Set<String> collectedGifts, boolean rewardClaimed) {
        this.playerName = playerName;
        this.collectedGifts = collectedGifts != null ? collectedGifts : new HashSet<>();
        this.rewardClaimed = rewardClaimed;
    }

    public String getPlayerKey() {
        return playerName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Set<String> getCollectedGifts() {
        return collectedGifts;
    }

    public boolean hasCollectedGift(String giftKey) {
        return collectedGifts.contains(giftKey);
    }

    public void addCollectedGift(String giftKey) {
        collectedGifts.add(giftKey);
    }

    public int getCollectedGiftsCount() {
        return collectedGifts.size();
    }

    public boolean isRewardClaimed() {
        return rewardClaimed;
    }

    public void setRewardClaimed(boolean rewardClaimed) {
        this.rewardClaimed = rewardClaimed;
    }
}
