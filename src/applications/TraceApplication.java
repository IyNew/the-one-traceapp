/*
 * Traceability application.
 */

package applications;

import java.util.Random;

import report.TraceAppReporter;
import core.Application;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import core.SimScenario;
import core.World;
import core.Debug;

public class TraceApplication extends Application {

    public static final String TRACE_ISTRACING = "istracing";
    public static final String TRACE_NUMBER_OF_NODES = "numofnodes";
    public static final String TRACE_INTERVAL = "interval";
    public static final String TRACE_PACE = "pace";
    /** Application ID */
    public static final String APP_ID = "my.TraceApplication";

    // Private vars
    private boolean isTracing = true;
    private double lastShare = 0;
    private int interval = 100;
    private int seed = 0;
    private int numNodes = 100;
    private Random rng;
    private int shareSize = 100;
    private int shareSizeWithTrace = 1000;
    private int logSize = 50;
    private int dataSize;
    private double sendPace = 0.5;


    // Constructor with settings
    public TraceApplication(Settings s) {
        // super(s);
        if (s.contains(TRACE_ISTRACING)) {
            this.isTracing = s.getBoolean(TRACE_ISTRACING);
            if (isTracing) {
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
        rng = new Random(this.seed);
        super.setAppID(APP_ID);
    }

    public TraceApplication(TraceApplication a) {
        super(a);
        this.isTracing = a.isTracing();
        this.numNodes = a.numNodes();
        this.interval = a.interval();
        this.sendPace = a.sendPace();
        this.rng = new Random(this.seed);
    }

    @Override
    public Message handle(Message msg, DTNHost host) {
        String type = (String) msg.getProperty("type");

        // at host 0, collect all logs
        if (type == "Log" && host.getAddress() == 0) {
            super.sendEventToListeners("GotLog", (String) msg.getId(), host);
        }
        if (type == "Data" && msg.getTo() == host) {
            // send event to the reporter
            super.sendEventToListeners("GotData", msg.getId(), host);
            // print isTracing
            // System.out.println("isTracing: " + isTracing);
            if (isTracing) {
                String logReceivedId = "logReceived-" + SimClock.getIntTime() + "-" + msg.getId();
                Message log = new Message(host, SimScenario.getInstance().getHosts().get(0), logReceivedId, logSize);
                log.addProperty("type", "Log");
                log.setAppID(APP_ID);
                host.createNewMessage(log);
                super.sendEventToListeners("SentLog", logReceivedId, host);
            }
            
        }
        
        return msg;
    }

    // update is called for every node at every time cycle
    @Override
    public void update(DTNHost host) {
        // skip host 0
        if (host.getAddress() != 0) {
            double curTime = SimClock.getTime();
            if (curTime - this.lastShare >= this.interval) {
                if (rollDice() == false) {
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

                if (this.isTracing) {
                    // send a log message to host 0
                    String logSentId = "logSent-" + SimClock.getIntTime() + "-" + msgId;
                    Message logSent = new Message(host, SimScenario.getInstance().getHosts().get(0), logSentId,
                            logSize);
                    logSent.addProperty("type", "Log");
                    logSent.setAppID(APP_ID);
                    host.createNewMessage(logSent);
                    super.sendEventToListeners("SentLog", logSentId, host);
                }
            }
        
        }
        
        // // at time 10, host 1 sends a message to host 50
        // if (SimClock.getTime() == 10 && host.getAddress() == 1) {
        //     // System.out.println("host 1 sends a message to host 50 at time " + SimClock.getTime());
        //     Message m = new Message(host, SimScenario.getInstance().getWorld().getNodeByAddress(50), "trace", 100);
        //     m.setAppID(APP_ID);
        //     // m.addProperty(, value);
        //     host.createNewMessage(m);
        // }
    }

    // get a random host
    private DTNHost randomHost() {
        int destaddr = 0;
        destaddr = 1 + new Random().nextInt(numNodes - 1);
        // System.out.println("destaddr: " + destaddr);
        World w = SimScenario.getInstance().getWorld();
        // System.out.println("ok");
        return w.getNodeByAddress(destaddr);
    }

    // roll a dice to decide whether to send a message
    private boolean rollDice() {
        double r = new Random().nextDouble();
        // System.out.println("r: " + r);
        if (r < this.sendPace) {
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

    public boolean isTracing() {
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

    // public void setTracing(boolean isTracing) {
    //     this.isTracing = isTracing;
    // }

}