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

    /** Application ID */
    public static final String APP_ID = "my.TraceApplication";

    // Private vars
    private boolean isTracing = false;
    private double lastShare = 0;
    private double interval = 50;
    private int seed = 0;
    private int shareSize = 100;
    private int shareSizeWithTrace = 1000;

    // Constructor with settings
    public TraceApplication(Settings s) {
        // super(s);
        super.setAppID(APP_ID);
    }

    public TraceApplication(TraceApplication a) {
        super(a);
    }

    @Override
    public Message handle(Message msg, DTNHost host) {
        String type = (String) msg.getProperty("type");

        // if logSent arrived at host 0, record them
        if () {
            
        }
        if (type == "Data" && msg.getTo() == host) {
            // send event to the reporter
            super.sendEventToListeners(event: "GotData", params: msg.getId(), host);
            // send a logSent to host 0 when a message is received
            String msgId = "logSent" + SimClock.getIntTime() + "-" + host.getAddress();
            Message log = new Message(host, SimScenario.getInstance().getHosts().get(0), "log", 50);
            log.addProperty("type", "logReceived");
            super.sendEventToListeners(event: "logReceived", params: , host);
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
            Message m = new Message(host, randomHost(), "trace" + SimClock.getIntTime() + "-" + host.getAddress(), this.shareSize);
            m.addProperty("type", "Data");
            m.setAppID(APP_ID);
            host.createNewMessage(m);
            this.lastShare = curTime;
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