package edu.dkv.internal.config;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@ConfigurationPropertiesScan
public class GossipConfig {

    @NotNull @Min(1)
    private long TRemove;

    @NotNull @Min(1)
    private long TFail;

    @NotNull @Min(1)
    private long TGossip;

    @NotNull @Min(3)
    private int Gossip;

    @NotNull
    private int KList;

    public long getTRemove() {
        return TRemove;
    }

    public void setTRemove(long TRemove) {
        this.TRemove = TRemove;
    }

    public long getTFail() {
        return TFail;
    }

    public void setTFail(long TFail) {
        this.TFail = TFail;
    }

    public long getTGossip() {
        return TGossip;
    }

    public void setTGossip(long TGossip) {
        this.TGossip = TGossip;
    }

    public int getKList() {
        return KList;
    }

    public void setKList(int KList) {
        this.KList = KList;
    }

    public int getGossip() {
        return Gossip;
    }

    public void setGossip(int gossip) {
        Gossip = gossip;
    }

    @Override
    public String toString() {
        return "GossipConfig {" +
                "\n  TRemove=" + TRemove +
                "\n, TFail=" + TFail +
                "\n, TGossip=" + TGossip +
                "\n, KList=" + KList +
                "\n}";
    }
}
