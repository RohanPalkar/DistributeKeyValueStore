package edu.dkv.internal;

import java.util.concurrent.*;

public class Rough {

    public static void main(String[] args) {
        ExecutorService e = Executors.newFixedThreadPool(2);
        CompletableFuture<Void> f1 = CompletableFuture.supplyAsync(() -> {
            /*System.out.println("Hello");
            for(int i = 0 ; i < 10 ; ++i){
                System.out.println(i);
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            System.out.println("Hello 3");*/
            Future<Void> f = e.submit(() -> {
                for(int i = 0 ; i < 10 ; ++i){
                    System.out.println(i);
                    Thread.sleep(6000);
                }
                return null;
            });
            try {
                f.get();
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            } catch (ExecutionException executionException) {
                executionException.printStackTrace();
            }
            return null;
        });

        CompletableFuture<Void> f2 = CompletableFuture.supplyAsync(() -> {
            /*System.out.println("Hello 2");
            for(int i = 11 ; i < 21 ; ++i){
                System.out.println(i);
            }*/
            Future<Void> f = e.submit(() -> {
                for(int i = 11 ; i < 21 ; ++i){
                    System.out.println(i);
                    Thread.sleep(2000);
                }
                return null;
            });
            try {
                f.get();
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            } catch (ExecutionException executionException) {
                executionException.printStackTrace();
            }
            return null;
        });

        CompletableFuture.allOf(f2,f1).join();

        e.shutdown();
    }


}
