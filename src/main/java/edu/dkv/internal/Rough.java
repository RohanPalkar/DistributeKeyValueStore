package edu.dkv.internal;

public class Rough {

    public static void main(String[] args) {
        int i = (int) (0.2 * 60 * 1000);
        System.out.println(i);
        long l2 = i;
        System.out.println(l2);
        System.out.println(getRunningTime());
    }

    public static long getRunningTime() {
        return (long) 0.2 * 60 * 1000;
    }
}
