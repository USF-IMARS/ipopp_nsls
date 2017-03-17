/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
/******************************************************************************
*
*  NISGS/NSLS
*
*  History:
*
*  29-Jul-05, 	Original version.
*  31-Jan-07, 	Thread handling.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.test;
import gov.nasa.gsfc.nisgs.nsls.*;
import gov.nasa.gsfc.nisgs.nsls.util.*;
import java.util.*;
// import java.io.*;

public class SpeedTest1 implements Runnable {
  private String serverHost;
  private int serverPort;
  private static final double slow = 0.25;
    private static int loopcount;
  public static void main (String[] args) {
    if (args.length != 2) {
      System.out.println("Usage: SpeedTest1 <host:port> <loopcount>");
      System.exit(0);
    }
    StringTokenizer byColon = new StringTokenizer(args[0],":");
    String host = byColon.nextToken();
    int port = Integer.parseInt(byColon.nextToken());
    loopcount = Integer.parseInt(args[1]);
    Util.startThread(new SpeedTest1(host,port));
  }
  public SpeedTest1 (String serverHost, int serverPort) {
    this.serverHost = serverHost;
    this.serverPort = serverPort;
  }
  public void run () {
    try {
      // Log log = new Log(true);
      // Log log = new Log(new File("test.log"));
      Log log = new Log(serverHost,serverPort,"./logs/tmp");
      log.setToStdErr(true);
      for (int loops = 0; loops < loopcount; ++loops) {
        log.info(new NSLS.NCS("Station4"),"started up");
        // Util.sleep(slow);
        log.warning(new NSLS.NCS("Station4","Group7","Job12"),
    	            new NSLS.ProductParameters("product1","granule2","pass3",null,null,null),
		    "I'm running out of memory!");
        // Util.sleep(slow);
        log.info(new NSLS.IS("FileCopy"),new NSLS.FileParameters("../5.pds"),"file copy: starting");
        // Util.sleep(slow);
        // log.error(new NSLS.IS("FileCopy"),new NSLS.FileParameters("../5.pds"),"file copy: failed",new Exception("oops"));
        // Util.sleep(slow);
        log.error(new NSLS.NCS("Station4","Group7","Job12"),
    	          new NSLS.ProductParameters("product1","granule2","pass3","type5",new Date(),new Date()),
		  "error while opening file: test1.dat\nsdfasdfdsfljds as dsdfasdf asd fasd fasdf asd as df asdf asdfasdfasdf asdf asdfasdfasdfa dfasdfasfdasdfasfd asdfasfdasfdasd f asdfasfdasfdasfdasdfasfdasfdasfd asdf asdfasfdasfdasdf asdf asfdasdfasfd asdfasdfasdfasdf asdfasfasfdasdfasdfasdfasfd asdfasfdasfdasfdasdf asdfasfdasfdasdfasfd asdfasdfasfdasfdasdfasfd asdf asdfasfdasdfasfdasfas asfdas fd asdf asd asd asdfas",
    	          new Exception("no such file: test1.dat",new Exception("help me")));
        log.error(new NSLS.NCS("Station4","Group7","Job12"),
    	          new NSLS.ProductParameters("product1","granule2","pass3","type5",new Date(),new Date()),
		  "error while opening file: test1.dat\nsdf ddsddddddddddddddddddddddddddddddddddddkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrnnnnnnnnnnnnnnnnnnnnnnnnnnnnnneeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeooooooooooooooooooooooooooooooooooooooooxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnneeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeekkkkkkkkkkkkkkkkkkkkkkkkqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqpppppppppppppppppppppppppppppppppppppssssssssssssssssssssssss",
    	          new Exception("no such file: test1.dat",new Exception("HELP ME")));
        log.error(new NSLS.NCS("Station4","Group7","Job12"),"HELP ME");
        if (false) {
          // return;
          System.exit(0);
          // Runtime.getRuntime().halt(0);
        }
        // Util.sleep(slow);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
