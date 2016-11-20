package io.stallion.clubhouse;

import io.stallion.clubhouse.ClubhousePlugin;

public class MainRunner
{
    public static void main( String[] args ) throws Exception {
        io.stallion.boot.MainRunner.mainWithPlugins(args, new ClubhousePlugin());
    }
}
