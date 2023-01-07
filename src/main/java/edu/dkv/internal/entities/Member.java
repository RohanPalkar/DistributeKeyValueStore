package edu.dkv.internal.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Member {

    // Member's address that includes hostname:port
    private EndPoint endPoint;

    // Indicates if the member is up
    private boolean inited;

    // Indicates if the member is in the group
    private boolean inGroup;

    // Indicates if the member has failed.
    private boolean isFailed;

    // Number of the neighbors of the current member.
    private int neighborCount;

    // Member's own heartbeat.
    private long heartbeat;

    // Counter for next ping.
    private long pingCounter;

    // Counter for ping timeout.
    private int timeOutCounter;

    // MembershipTable
    private Map<EndPoint, MemberListEntry> membershipList;

    public EndPoint getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(EndPoint endPoint) {
        this.endPoint = endPoint;
    }

    public boolean isInited() {
        return inited;
    }

    public void setInited(boolean inited) {
        this.inited = inited;
    }

    public boolean isInGroup() {
        return inGroup;
    }

    public void setInGroup(boolean inGroup) {
        this.inGroup = inGroup;
    }

    public boolean isFailed() {
        return isFailed;
    }

    public void setFailed(boolean failed) {
        isFailed = failed;
    }

    public int getNeighborCount() {
        return neighborCount;
    }

    public void setNeighborCount(int neighborCount) {
        this.neighborCount = neighborCount;
    }

    public long getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(long heartbeat) {
        this.heartbeat = heartbeat;
    }

    public void incrementHeartbeat(){
        ++heartbeat;
    }

    public long getPingCounter() {
        return pingCounter;
    }

    public void decrementPingCounter(){
        --pingCounter;
    }

    public void setPingCounter(long pingCounter) {
        this.pingCounter = pingCounter;
    }

    public int getTimeOutCounter() {
        return timeOutCounter;
    }

    public void setTimeOutCounter(int timeOutCounter) {
        this.timeOutCounter = timeOutCounter;
    }

    public Map<EndPoint, MemberListEntry> getMembershipList() {
        return membershipList;
    }

    public Set<MemberListEntry> getMembershipListForMessage(){
        return new HashSet<>(membershipList.values());
    }

    public MemberListEntry getMemberListEntry(EndPoint endPoint){
        return endPoint != null && membershipList != null ? membershipList.get(endPoint) : null;
    }

    public void setMembershipList(Map<EndPoint, MemberListEntry> membershipList) {
        this.membershipList = membershipList;
    }

    public void addToMembershipList(EndPoint endPoint, MemberListEntry entry){
        if(this.membershipList == null)
            this.membershipList = new ConcurrentHashMap<>();
        this.membershipList.put(endPoint, entry);
    }

    public boolean containsMemberListEntry(EndPoint endPoint){
        return membershipList != null && membershipList.containsKey(endPoint);
    }

    public String printMembershipList(){
        if(membershipList == null || membershipList.isEmpty()){
            return "";
        }

        StringBuffer sb = new StringBuffer();
        sb.append("{\n");
        membershipList.forEach((k,v) -> {
            sb.append(" [ ");
            sb.append(k);
            sb.append(" :: " + v.getHeartbeat());
            sb.append(" :: " + v.getTimestamp());
            sb.append(" ]\n");
        });
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return endPoint.equals(member.endPoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endPoint);
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates and returns a copy of this object.
     *
     * @return a clone of this instance.
     * @throws CloneNotSupportedException if the object's class does not
     *                                    support the {@code Cloneable} interface. Subclasses
     *                                    that override the {@code clone} method can also
     *                                    throw this exception to indicate that an instance cannot
     *                                    be cloned.
     * @see Cloneable
     */
    @Override
    protected Member clone() throws CloneNotSupportedException {
        return (Member) super.clone();
    }
}
