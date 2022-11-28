package report;

import applications.TraceApplication;
import core.Application;
import core.ApplicationListener;
import core.DTNHost;
import java.util.*;


/*
 * Reporter for the <code>TraceApplication</code>. Counts the number of data sharing and logs.
 */

public class TraceAppReporter extends Report implements ApplicationListener {
    // private int dataSent = 0, dataReceived = 0;
    // private int logSent = 0, logReceived = 0;
    private Set<String> logSentList = new HashSet<>();
    private Set<String> logReceivedList = new HashSet<>();
    private Set<String> dataSentList = new HashSet<>();
    private ArrayList<String> dataSentAllList = new ArrayList<String>();
    private Set<String> dataReceivedList = new HashSet<>(); 
    private ArrayList<String> dataReceivedAllList = new ArrayList<String>();
    private Set<String> SentProbLogList = new HashSet<>();
    private Set<String> ReceivedProbLogList = new HashSet<>(); 

    public void gotEvent(String event, Object params, Application app, DTNHost host) {
        // Check that the event is sent by correct application type
        if (!(app instanceof TraceApplication)) return;

        // Increment the counters based on the event type
        if (event.equalsIgnoreCase("SentData")) {
            // dataSent++;
            dataSentList.add((String) params);
            dataSentAllList.add((String) params);
        }
        if (event.equalsIgnoreCase("GotData")) {
            // dataReceived++;
            dataReceivedList.add((String) params);
        }
        if (event.equalsIgnoreCase("GotRepeatData")) {
            dataReceivedAllList.add((String) params);
        }
        if (event.equalsIgnoreCase("SentLog")) {
            // logSent++;
            logSentList.add((String) params);
        }
        if (event.equalsIgnoreCase("GotLog")) {
            // logReceived++;
            logReceivedList.add((String) params);
        }
        if (event.equalsIgnoreCase("SentProbLog")) {
            SentProbLogList.add((String) params);
        }
        if (event.equalsIgnoreCase("GotProbLog")) {
            ReceivedProbLogList.add((String) params);
        }
    }

    @Override
    public void done() {
        write("Trace stats for scenario " + getScenarioName() + "\nsim_time: " + format(getSimTime()));
        double dataProb = 0; // data probability
        double logProb = 0; // log probability
        dataProb = (1.0 * dataReceivedList.size()) / dataSentList.size();
        if (logSentList.size() != 0) {
            logProb = (1.0 * logReceivedList.size()) / logSentList.size();
        }
        write("dataSent: " + dataSentList.size());
        write("dataSentAll: " + dataSentAllList.size());
        write("dataReceived: " + dataReceivedList.size());
        write("dataReceivedAll: " + dataReceivedAllList.size());
        write("dataDeliveryProb: " + dataProb);
        write("logSent: " + logSentList.size());
        write("logReceived: " + logReceivedList.size());
        write("logDeliveryProb: " + (1.0 * logReceivedList.size()) / logSentList.size());
        write("probLogSent: " + SentProbLogList.size());
        write("probLogReceived: " + ReceivedProbLogList.size());


        write("Data sent list: " + this.dataSentList.size() + " recorded");
        for (String data : dataSentList) {
            write(data);
        }
        write("");
        write("Data sent all list: " + this.dataSentAllList.size() + " recorded");
        for (String data : dataSentAllList) {
            write(data);
        }
        write("");
        write("Data received list: " + this.dataReceivedList.size() + " recorded");
        for (String data : dataReceivedList) {
            write(data);
        }
        write("");
        write("Data received all list: " + this.dataReceivedAllList.size() + " recorded");
        for (String data : dataReceivedAllList) {
            write(data);
        }
        write("");
        write("Log sent list: " + this.logSentList.size() + " recorded");
        for (String data : logSentList) {
            write(data);
        }
        write("");
        write("Log received list: " + this.logReceivedList.size() + " recorded");
        for (String data : logReceivedList) {
            write(data);
        }
        write("");
        write("ReceivedProbLogList: " + this.ReceivedProbLogList.size() + " recorded");
        for (String data : ReceivedProbLogList) {
            write(data);
        }
        super.done();
    }
}