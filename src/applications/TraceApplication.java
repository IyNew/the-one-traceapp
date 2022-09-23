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
    /** Application ID */
    public static final String APP_ID = "my.TraceApplication";

    // Private vars
    private boolean isTracing = true;
    private double lastShare = 0;
    private double interval = 50;
    private int seed = 0;
    private Random rng;
    private int shareSize = 100;
    private int shareSizeWithTrace = 1000;
    private int dataSize;


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
        rng = new Random(this.seed);
        super.setAppID(APP_ID);
    }

    public TraceApplication(TraceApplication a) {
        super(a);
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
            if (isTracing) {
                String logReceivedId = "logReceived-" + SimClock.getIntTime() + "-" + msg.getId();
                Message log = new Message(host, SimScenario.getInstance().getHosts().get(0), logReceivedId, 50);
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
        // get current time
        double curTime = SimClock.getTime();
        if (curTime - this.lastShare >= this.interval) {
            // send a message
            DTNHost dest = randomHost();
            String msgId = "Data-" + SimClock.getIntTime() + "-" + host.getAddress() + "-" + dest.getAddress();
            Message m = new Message(host, dest, msgId, this.dataSize);
            m.addProperty("type", "Data");
            m.setAppID(APP_ID);
            host.createNewMessage(m);
            this.lastShare = curTime;
            super.sendEventToListeners("SentData", msgId, host);

            if (this.isTracing) {
                // send a log message to host 0
                String logSentId = "logSent-" + SimClock.getIntTime() + "-" + msgId;
                Message logSent = new Message(host, SimScenario.getInstance().getHosts().get(0), logSentId, 50);
                logSent.addProperty("type", "Log");
                logSent.setAppID(APP_ID);
                host.createNewMessage(logSent);
                super.sendEventToListeners("SentLog", logSentId, host);
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
        destaddr = 1 + new Random().nextInt(100);
        World w = SimScenario.getInstance().getWorld();
        return w.getNodeByAddress(destaddr);
    }

    @Override
    public Application replicate() {
        return new TraceApplication(this);
    }

}