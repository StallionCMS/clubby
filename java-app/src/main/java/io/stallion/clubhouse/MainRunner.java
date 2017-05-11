package io.stallion.clubhouse;

import io.stallion.clubhouse.ClubhousePlugin;

public class MainRunner
{
    public static void main( String[] args ) throws Exception {
        System.setProperty("java.awt.headless", "true");
        io.stallion.boot.MainRunner.mainWithPlugins(args, new ClubhousePlugin());
    }
}
