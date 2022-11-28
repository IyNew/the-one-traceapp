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
    public static final String TRACE_ATTACK = "isattack";
    public static final String TRACE_STRONG_ATTACK_PROB = "strongattackprob";
    public static final String TRACE_WEAK_ATTACK_PROB = "weakattackprob";
    public static final String TRACE_REPORT_PROB = "reportprob";
    public static final String TRACE_REPEAT_TIME = "repeattimes";
    /** Application ID */
    public static final String APP_ID = "my.TraceApplication";

    // Private vars
    private int isTracing = 2; // 0: no trace, 1: only logReceived, 2: logSent and logReceived
    private double lastShare = 0;
    private int interval = 100;
    private int seed = 0;
    private int numNodes = 100;
    // private Random rng;
    private int shareSize = 1024;
    private int shareSizeWithTrace = 1024;
    private int logSize = 50;
    private int dataSize;
    private double sendPace = 0.5;
    private int numTA = 10;
    private boolean isReshare = true;
    private boolean isAttack = false;
    private double strongAttackProb = 0.5;
    private double weakAttackProb = 0;
    private double reportProb = 0.3;
    private int repeatTimes = 0;
    private int edgeY =  250;
    private int repeatCount = -1;
    private String repeatMessage = null;
    private int lastRepeatTime = -1;


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
        if (s.contains(TRACE_ATTACK)) {
            this.isAttack = s.getBoolean(TRACE_ATTACK);
        }
        if (s.contains(TRACE_STRONG_ATTACK_PROB)) {
            this.strongAttackProb = s.getDouble(TRACE_STRONG_ATTACK_PROB);
        }
        if (s.contains(TRACE_WEAK_ATTACK_PROB)) {
            this.weakAttackProb = s.getDouble(TRACE_WEAK_ATTACK_PROB);
        }
        if (s.contains(TRACE_REPORT_PROB)) {
            this.reportProb = s.getDouble(TRACE_REPORT_PROB);
        }
        if (s.contains(TRACE_REPEAT_TIME)) {
            this.repeatTimes = s.getInt(TRACE_REPEAT_TIME);
            // this.repeatCount = this.repeatTimes;
        }
        // rng = new Random(this.seed);
    
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
        this.isAttack = a.isAttack();
        this.strongAttackProb = a.strongAttackProb();
        this.weakAttackProb = a.weakAttackProb();
        this.reportProb = a.reportProb();
        this.repeatTimes = a.repeatTimes();
        this.repeatCount = a.repeatCount();
        this.lastRepeatTime = a.lastRepeatTime();
        // this.rng = new Random(this.seed);
    }

    @Override
    public Message handle(Message msg, DTNHost host) {

        // debug mode: print the message
        // if (host.getAddress() > 140) {
        //     System.out.println("Message received: " + msg.getId() + " from " + msg.getFrom() + " to " + msg.getTo()
        //             + " at " + SimClock.getTime() + " with repeatCount " + msg.getProperty("repeatCount"));
        // }
        
     
        
        String type = (String) msg.getProperty("type");
        String zonePrefix = "";
        if (this.isAttack) {
            zonePrefix = (host.getLocation().getY() < this.edgeY) ? "H" : "L";
        }

        // handle attack if the host is no TA
        if (this.isAttack && host.getAddress() >= this.numTA) {
            // nodes in dangerous zone have strongAttackProb chance to drop the message
            double dropProb = (host.getLocation().getY() < this.edgeY) ? this.strongAttackProb : this.weakAttackProb;
            if (doIAttack(dropProb)) {
                return null;
            }
        }

        // at TA nodes, collect all logs
        if (type == "Log" && host.getAddress() < this.numTA) {
            super.sendEventToListeners("GotLog", (String) msg.getId(), host);
            // return null to drop the message
            return null;
        }
        if (type == "probLog" && host.getAddress() < this.numTA) {
            super.sendEventToListeners("GotProbLog", (String) msg.getId(), host);
            // System.out.println((String) msg.getId() +  "should be dropped by node " + host.getAddress());
            return null;
        }

        // when a data is received
        if (type == "Data" && msg.getTo() == host) {
            // print the data
            System.out.println("mesage received by " + host.getAddress() + " at " + SimClock.getTime() + " is " + (String) msg.getId() + " with count " + (int) msg.getProperty("repeatCount"));
            // send event to the reporter
            super.sendEventToListeners("GotData", zonePrefix + msg.getId(), host);
            super.sendEventToListeners("GotRepeatData", msg.getId() + "-" + msg.getProperty("repeatCount"), host);
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
                String reshareDataId = zonePrefix + msg.getId() + "-rs" + SimClock.getIntTime() + "-" + reshareTo.getAddress();
                Message reshareData = new Message(host, reshareTo, reshareDataId, this.dataSize);
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

        // intermediate nodes report the routing information
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

        // debug mode: see only one node's behavior
        if (host.getAddress() != 100) {
            return;
        }
        

        // World temp = SimScenario.getInstance().getWorld();
        // System.out.println("World: " + temp.getHosts().size());

        String zonePrefix = "";
        if (this.isAttack) {
            zonePrefix = (host.getLocation().getY() < this.edgeY) ? "H" : "L";
        }

        // add offset 3 in repeat interval to avoid edge cases
        int repeatInterval = this.interval / (this.repeatTimes + 1 ) - 3;

        // skip TA nodes
        if (host.getAddress() >= this.numTA) {
            int curTime = SimClock.getIntTime();
            // System.out.println("times: " + this.repeatCount);
            // resend the data
            // print repeatCount
            // System.out.println("At host " + host.getAddress() + " repeatCount: " + this.repeatCount);

            // a new data sharing is initiated
            if (curTime - this.lastShare >= this.interval) {
                if (paceSampler() == false) {
                    this.lastShare = curTime;
                    return ;
                }
                // send a new data
                else {
                    DTNHost dest = randomHost();
                    this.repeatCount = 0;
                    String msgId = zonePrefix + "Data-" + SimClock.getIntTime() + "-" + host.getAddress() + "-"
                            + dest.getAddress();
                    // System.out.println("dest" + dest.getAddress() + "host" + host.getAddress());
                    if (this.repeatTimes > 0) {
                        msgId = Integer.toString(this.repeatCount) + "-" + msgId;
                    }
                    Message m = new Message(host, dest, msgId, this.dataSize);
                    m.addProperty("type", "Data");
                    m.addProperty("repeatCount", this.repeatCount);
                    m.setAppID(APP_ID);
                    host.createNewMessage(m);
                    this.lastShare = curTime;
                    super.sendEventToListeners("SentData", msgId, host);
                    this.repeatMessage = msgId;
                    // print the repeatMessage
                    System.out.println("Hello repeat message: " + this.repeatMessage);
                    this.repeatCount += 1;
                    this.lastRepeatTime = curTime;
                    

                    if (this.isTracing >= 2) {
                        // send a log message to host 0
                        String logSentId = zonePrefix + "logSent-" + SimClock.getIntTime() + "-" + msgId;
                        Message logSent = new Message(host, randomTAHost(), logSentId,
                                logSize);
                        logSent.addProperty("type", "Log");
                        logSent.setAppID(APP_ID);
                        host.createNewMessage(logSent);
                        super.sendEventToListeners("SentLog", logSentId, host);
                    }
                }
                
            }

            // resend the data
            /

            // if (this.repeatTimes > 0) {
            //     if (curTime - (this.repeatCount * repeatInterval) - this.lastShare > repeatInterval) {
            //         // split the repeatMessage
            //         System.out.println("repeat message: " + this.repeatMessage);
            //         String[] repeatMessageSplit = this.repeatMessage.split("-");
            //         // System.out.println("to: " + repeatMessageSplit[3]);
            //         Message repMsg = new Message(host, SimScenario.getInstance().getWorld().getNodeByAddress(
            //                 Integer.parseInt(repeatMessageSplit[3])),
            //                 Integer.toString(this.repeatCount) + "-" + this.repeatMessage, this.dataSize);
            //         repMsg.addProperty("type", "Data");
            //         repMsg.addProperty("repeatCount", this.repeatCount);
            //         repMsg.setAppID(APP_ID);
            //         host.createNewMessage(repMsg);
            //         super.sendEventToListeners("SentData", repMsg.getId(), host);
            //         this.repeatCount += 1;
            //         // System.out.println("Sent data: " + repMsg.getId() + " at time: " + curTime +
            //         // " with repeat count: " + repMsg.getProperty("repeatCount"));
            //         if (this.isTracing >= 2) {
            //             String logSentId = zonePrefix + "logSent-" + SimClock.getIntTime() + "-"
            //                     + repMsg.getId();
            //             Message logSent = new Message(host, randomTAHost(), logSentId,
            //                     logSize);
            //             logSent.addProperty("type", "Log");
            //             logSent.setAppID(APP_ID);
            //             host.createNewMessage(logSent);
            //             super.sendEventToListeners("SentLog", logSentId, host);
            //         }
            //     }
            //     return;

            // }
        
        }
        
    }

    // get a random host
    private DTNHost randomHost() {
        int destaddr = 0;
        destaddr = this.numTA + new Random().nextInt(this.numNodes);
        // System.out.println(this.numNodes + this.numTA + " destaddr in randomHost: " + destaddr);
        World w = SimScenario.getInstance().getWorld();
        // System.out.println("ok");s
        return w.getNodeByAddress(destaddr);
    }

    // get n random TA hosts.
    // hosts 0 - numTA are TA hosts
    private DTNHost randomTAHost() {
        int destTAaddr = 0;
        
        destTAaddr = new Random().nextInt(this.numTA);
        // System.out.println(this.numTA + " destTAaddr in randomTAHost: " + destTAaddr);
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
        double r = new Random().nextDouble();
        if (r < this.reportProb) {
            return true;
        }
        else {
            return false;
        }
    }


    private boolean doIAttack(double attackProb) {
        double r = new Random().nextDouble();
        if (r < attackProb) {
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

    public boolean isAttack() {
        return this.isAttack;
    }

    public double strongAttackProb() {
        return this.strongAttackProb;
    }
    
    public double weakAttackProb() {
        return this.weakAttackProb;
    }

    public double reportProb() {
        return this.reportProb;
    }

    public int repeatTimes() {
        return this.repeatTimes;
    }

    public int repeatCount() {
        return this.repeatCount;
    }

    public int lastRepeatTime() {
        return this.lastRepeatTime;
    }
    // public void setTracing(boolean isTracing) {
    //     this.isTracing = isTracing;
    // }

}