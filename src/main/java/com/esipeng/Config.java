package com.esipeng;

import de.tototec.cmdoption.CmdOption;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by stiffme on 2015/7/11.
 */
public class Config{
    @CmdOption(names={"--help", "-h"}, description="Show this help.",isHelp=true)
    public boolean help;

    @CmdOption(names={"--from", "-f"}, description="Before time.",maxCount=1,minCount=0,args="201412220104")
    public String from="";

    @CmdOption(names={"--to", "-t"}, description="After time.",maxCount=1,minCount=0,args="201412220104")
    public String to="";

    @CmdOption(names={"--filter","-l"}, description = "Regular expression filters to apply",maxCount = -1,minCount = 0,args="Hss")
    List<String> filters = new ArrayList<String>();

    @CmdOption(args = { "location" }, description = "PMF file names to process or directory", minCount = 1, maxCount = -1)
    List<String> names = new LinkedList<String>();


}