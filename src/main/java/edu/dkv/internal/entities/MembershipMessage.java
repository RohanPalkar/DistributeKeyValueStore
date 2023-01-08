package edu.dkv.internal.entities;

import edu.dkv.internal.common.Utils;

import java.io.Serializable;
import java.util.Set;

import static edu.dkv.internal.common.Utils.printMembershipList;

public class MembershipMessage implements Serializable {

    public static final long serialVersionUID = 42L;

    private final MessageType messageType;
    private final EndPoint endPoint;
    private final long heartbeat;
    private final int sizeOfMembershipList;
    private final Set<MemberListEntry> membershipList;

    public MembershipMessage(MembershipMessageBuilder builder) {
        this.messageType = builder.messageType;
        this.endPoint = builder.endPoint;
        this.heartbeat = builder.heartbeat;
        this.sizeOfMembershipList = builder.sizeOfMembershipList;
        this.membershipList = builder.membershipList;
    }

    public static MembershipMessageBuilder createMessage(){
        return new MembershipMessageBuilder();
    }

    public static class MembershipMessageBuilder {
        private MessageType messageType;
        private EndPoint endPoint;
        private long heartbeat;
        private int sizeOfMembershipList;
        private Set<MemberListEntry> membershipList;

        public MembershipMessageBuilder setMessageType(MessageType messageType){
            this.messageType = messageType;
            return this;
        }

        public MembershipMessageBuilder setEndPoint(EndPoint endPoint){
            this.endPoint = endPoint;
            return this;
        }

        public MembershipMessageBuilder setHeartbeat(long heartbeat){
            this.heartbeat = heartbeat;
            return this;
        }

        public MembershipMessageBuilder setSizeOfMembershipList(int sizeOfMembershipList){
            this.sizeOfMembershipList = sizeOfMembershipList;
            return this;
        }

        public MembershipMessageBuilder setMembershipList(Set<MemberListEntry> membershipList){
            this.membershipList = membershipList;
            return this;
        }

        public MembershipMessage build(){
            return new MembershipMessage(this);
        }
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public EndPoint getEndPoint() {
        return endPoint;
    }

    public long getHeartbeat() {
        return heartbeat;
    }

    public int getSizeOfMembershipList() {
        return sizeOfMembershipList;
    }

    public Set<MemberListEntry> getMembershipList() {
        return membershipList;
    }

    @Override
    public String toString() {
        return "MembershipMessage {" +
                "\n messageType=" + messageType +
                "\n, endPoint=" + endPoint +
                "\n, heartbeat=" + heartbeat +
                "\n, sizeOfMembershipList=" + sizeOfMembershipList +
                "\n, membershipList=" + printMembershipList(membershipList) +
                "\n}";
    }
}
