/*
 * Traceability application.
 */

package applications;

import java.util.Random;

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
        // print the message
        System.out.println("Message received at " + SimClock.getTime() + " from " + msg.getFrom() + " to " + msg.getTo()
                + " with size " + msg.getSize());
        return msg;
    }

    // update is called for every node at every time cycle
    @Override
    public void update(DTNHost host) {
        // update is called at host
        // System.out.println("update is called at host " + host.getAddress() + " at time " + SimClock.getTime());
        
        // at time 10, host 1 sends a message to host 50
        // if (SimClock.getTime() == 10 && host.getAddress() == 1) {
        //     // System.out.println("host 1 sends a message to host 50 at time " + SimClock.getTime());
        //     Message m = new Message(host, SimScenario.getInstance().getWorld().getNodeByAddress(50), "trace", 100);
        //     m.setAppID(APP_ID);
        //     host.createNewMessage(m);

        //     // get class information of host
        //     System.out.println(host.getClass().getName());
        // }
    }

    @Override
    public Application replicate() {
        return new TraceApplication(this);
    }

}