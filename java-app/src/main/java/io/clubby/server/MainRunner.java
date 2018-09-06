package io.clubby.server;

public class MainRunner
{
    public static void main( String[] args ) throws Exception {
        System.setProperty("java.awt.headless", "true");
        io.stallion.Main.mainWithPlugins(new ClubbyStallionApplication(), args);
    }
}
