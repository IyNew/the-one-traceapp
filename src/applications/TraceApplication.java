/*
 * Traceability application.
 */

package applications;

import java.lang.reflect.Method;
import java.util.Random;

import report.TraceAppReporter;
import core.Application;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import core.SimScenario;
import core.World;
import routing.MessageRouter;

public class TraceApplication extends Application {

    public static final String TRACE_ISTRACING = "istracing";
    public static final String TRACE_NUMBER_OF_NODES = "numofnodes";
    public static final String TRACE_INTERVAL = "interval";
    public static final String TRACE_PACE = "pace";
    public static final String TRACE_NUMBER_OF_TA = "numofTA";
    public static final String TRACE_RESHARE = "isreshare";
    /** Application ID */
    public static final String APP_ID = "my.TraceApplication";

    // Private vars
    private int isTracing = 2; // 0: no trace, 1: only logReceived, 2: logSent and logReceived
    private double lastShare = 0;
    private int interval = 100;
    private int seed = 0;
    private int numNodes = 100;
    private Random rng;
    private int shareSize = 1000;
    private int shareSizeWithTrace = 1000;
    private int logSize = 50;
    private int dataSize;
    private double sendPace = 0.5;
    private int numTA = 10;
    private boolean isReshare = true;


    // Constructor with settings
    public TraceApplication(Settings s) {
        // super(s);
        if (s.contains(TRACE_ISTRACING)) {
            this.isTracing = s.getInt(TRACE_ISTRACING);
            if (isTracing > 0) {
                this.dataSize = shareSizeWithTrace;
            }
            else {
                this.dataSize = shareSize;
            }
        }
        if (s.contains(TRACE_NUMBER_OF_NODES)) {
            this.numNodes = s.getInt(TRACE_NUMBER_OF_NODES);
        }
        if (s.contains(TRACE_INTERVAL)) {
            this.interval = s.getInt(TRACE_INTERVAL);
        }
        if (s.contains(TRACE_PACE)) {
            this.sendPace = s.getDouble(TRACE_PACE);
        }
        if (s.contains(TRACE_NUMBER_OF_TA)) {
            this.numTA = s.getInt(TRACE_NUMBER_OF_TA);
        }
        if (s.contains(TRACE_RESHARE)) {
            this.isReshare = s.getBoolean(TRACE_RESHARE);
        }
        rng = new Random(this.seed);
        super.setAppID(APP_ID);
    }
    
    // Copy-constructor
    public TraceApplication(TraceApplication a) {
        super(a);
        this.isTracing = a.isTracing();
        this.numNodes = a.numNodes();
        this.interval = a.interval();
        this.sendPace = a.sendPace();
        this.numTA = a.numTA();
        this.isReshare = a.isReshare();
        this.rng = new Random(this.seed);
    }

    @Override
    public Message handle(Message msg, DTNHost host) {
        String type = (String) msg.getProperty("type");

        // at host 0, collect all logs
        if (type == "Log" && host.getAddress() < this.numTA) {
            super.sendEventToListeners("GotLog", (String) msg.getId(), host);
            // drop the message
            // host.deleteMessage(msg.getId(), false);
        }
        if (type == "probLog" && host.getAddress() < this.numTA) {
            super.sendEventToListeners("GotProbLog", (String) msg.getId(), host);
            // drop the message
            // host.deleteMessage(msg.getId(), false);
        }

        // when a data is received
        if (type == "Data" && msg.getTo() == host) {
            // send event to the reporter
            super.sendEventToListeners("GotData", msg.getId(), host);
            // todo: check hop count
            // print isTracing
            // System.out.println("isTracing: " + isTracing);
            if (isTracing > 0) {
                String logReceivedId = "logReceived-" + SimClock.getIntTime() + "-" + msg.getId();
                Message log = new Message(host, randomTAHost(), logReceivedId, logSize);
                log.addProperty("type", "Log");
                log.setAppID(APP_ID);
                host.createNewMessage(log);
                super.sendEventToListeners("SentLog", logReceivedId, host);
            }

            // System.out.println("isReshare: " + isReshare);
            // initiate reshare if needed
            if (isReshare) {
                DTNHost reshareTo = randomHost();
                String reshareDataId = msg.getId() + "-rs" + SimClock.getIntTime() + "-" + reshareTo.getAddress();
                Message reshareData = new Message(host, reshareTo, reshareDataId, dataSize);
                reshareData.addProperty("type", "Data");
                reshareData.setAppID(APP_ID);
                host.createNewMessage(reshareData);
                super.sendEventToListeners("SentData", reshareDataId, host);
                
                if (isTracing >= 2) {
                    String logSentId = "logSent-" + SimClock.getIntTime() + "-" + reshareDataId;
                    Message logSentReshare = new Message(host, randomTAHost(), logSentId, logSize);
                    logSentReshare.addProperty("type", "Log");
                    logSentReshare.setAppID(APP_ID);
                    host.createNewMessage(logSentReshare);
                    super.sendEventToListeners("SentLog", logSentId, host);
                }
            }
            
        }

        if (type == "Data" && msg.getTo() != host && this.isTracing == 3 ) {
            // 
            int hostAddr = host.getAddress();
            if (probReportSampler(hostAddr)) {
                // get the probability of the delivery
                MessageRouter rt = host.getRouter();
                Class<?> prophetClass = rt.getClass();
                // System.out.println("Class: " + prophetClass);
                try{
                    Method md = prophetClass.getMethod("getPredFor", DTNHost.class);
                    double pred = (double) md.invoke(rt, msg.getTo());
                    // System.out.println("Pred to " + msg.getTo().getAddress() + " with pred: " +
                    // pred);
                    String probReportID = msg.getId() +  "-host" + hostAddr + "-probReport-" + String.valueOf(pred);
                    Message probReport = new Message(host, randomTAHost(), probReportID, logSize);
                    probReport.addProperty("type", "probLog");
                    probReport.setAppID(APP_ID);
                    host.createNewMessage(probReport);
                    super.sendEventToListeners("SentProbLog", probReportID, host);
                }
                catch (Exception e) {
                    System.out.println("Error: " + e);
                }
            }
        }
        // print current preds for the target host
        // use reflection to get the private field preds
        // if (type == "Data") {
        //     // print message id
        //     System.out.println("Message id: " + msg.getId());
        //     MessageRouter rt = host.getRouter();
        //     Class<?> prophetClass = rt.getClass();
        //     // System.out.println("Class: " + prophetClass);
        //     try{
        //         Method md = prophetClass.getMethod("getPredFor", DTNHost.class);
        //         double pred = (double) md.invoke(rt, msg.getTo());
        //         System.out.println("Pred to " + msg.getTo().getAddress() + " with pred: " + pred);
        //     }
        //     catch (Exception e) {
        //         System.out.println("Error: " + e);
        //     }
           
        // }
        
        
        return msg;
    }

    // update is called for every node at every time cycle
    @Override
    public void update(DTNHost host) {
        // skip host 0
        if (host.getAddress() != 0) {
            double curTime = SimClock.getTime();
            if (curTime - this.lastShare >= this.interval) {
                if (paceSampler() == false) {
                    this.lastShare = curTime;
                    return ;
                }
                // send a message
                DTNHost dest = randomHost();
                String msgId = "Data-" + SimClock.getIntTime() + "-" + host.getAddress() + "-" + dest.getAddress();
                // System.out.println("dest" + dest.getAddress() + "host" + host.getAddress());
                Message m = new Message(host, dest, msgId, this.dataSize);
                m.addProperty("type", "Data");
                m.setAppID(APP_ID);
                host.createNewMessage(m);
                this.lastShare = curTime;
                super.sendEventToListeners("SentData", msgId, host);

                if (this.isTracing >= 2) {
                    // send a log message to host 0
                    String logSentId = "logSent-" + SimClock.getIntTime() + "-" + msgId;
                    Message logSent = new Message(host, randomTAHost(), logSentId,
                            logSize);
                    logSent.addProperty("type", "Log");
                    logSent.setAppID(APP_ID);
                    host.createNewMessage(logSent);
                    super.sendEventToListeners("SentLog", logSentId, host);
                }
            }
        
        }
        
    }

    // get a random host
    private DTNHost randomHost() {
        int destaddr = 0;
        destaddr = numTA + new Random().nextInt(numNodes - 1);
        // System.out.println("destaddr: " + destaddr);
        World w = SimScenario.getInstance().getWorld();
        // System.out.println("ok");
        return w.getNodeByAddress(destaddr);
    }

    // get n random TA hosts.
    // hosts 0 - numTA are TA hosts
    private DTNHost randomTAHost() {
        int destTAaddr = 0;
        destTAaddr = new Random().nextInt(numTA);
        World w = SimScenario.getInstance().getWorld();
        return w.getNodeByAddress(destTAaddr);
    }

    // roll a dice to decide whether to send a message
    private boolean paceSampler() {
        double r = new Random().nextDouble();
        // System.out.println("r: " + r);
        if (r < this.sendPace) {
            return true;
        }
        else {
            return false;
        }
    }

    // roll a dice to decide wether to report prob
    private boolean probReportSampler(int hostAddr) {
        // 30% chance to report
        if (hostAddr % 3 == 0) {
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public Application replicate() {
        return new TraceApplication(this);
    }

    public int isTracing() {
        return this.isTracing;
    }

    public int numNodes() {
        return this.numNodes;
    }
    
    public int interval() {
        return this.interval;
    }

    public double sendPace() {
        return this.sendPace;
    }

    public int numTA() {
        return this.numTA;
    }
    
    public boolean isReshare() {
        return this.isReshare;
    }

    // public void setTracing(boolean isTracing) {
    //     this.isTracing = isTracing;
    // }

}