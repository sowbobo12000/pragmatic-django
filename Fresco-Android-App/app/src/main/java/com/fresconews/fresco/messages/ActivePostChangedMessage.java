package com.fresconews.fresco.messages;

/**
 * This message is sent whenever the active post has been changed (meaning that a video should start or stop playing)
 */
public class ActivePostChangedMessage {
    public String postId;

    public ActivePostChangedMessage(String postId) {
        this.postId = postId;
    }
}
