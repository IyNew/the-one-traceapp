package report;

import applications.TraceApplication;
import core.Application;
import core.ApplicationListener;
import core.DTNHost;
import java.util.ArrayList;


/*
 * Reporter for the <code>TraceApplication</code>. Counts the number of data sharing and logs.
 */

public class TraceAppReporter extends Report implements ApplicationListener {
    private int dataSent = 0, dataReceived = 0;
    private int logSent = 0, logReceived = 0;
    private ArrayList<String> logSentList = new ArrayList<String>();
    private ArrayList<String> logReceivedList = new ArrayList<String>();
    private ArrayList<String> dataSentList = new ArrayList<String>();
    private ArrayList<String> dataReceivedList = new ArrayList<String>();  

    public void gotEvent(String event, Object params, Application app, DTNHost host) {
        // Check that the event is sent by correct application type
        if (!(app instanceof TraceApplication)) return;

        // Increment the counters based on the event type
        if (event.equalsIgnoreCase("GotData")) {
            dataReceived++;
            dataReceivedList.add((String) params);
        }
        if (event.equalsIgnoreCase("SentLog")) {
            logSent++;
            logSentList.add((String) params);
        }
        if (event.equalsIgnoreCase("GotLog")) {
            logReceived++;
            logReceivedList.add((String) params);
        }
        if (event.equalsIgnoreCase("SentData")) {
            dataSent++;
            dataSentList.add((String) params);
        }
    }

    @Override
    public void done() {
        write("Trace stats for scenario " + getScenarioName() + "\nsim_time: " + format(getSimTime()));
        // double dataProb = 0; // data probability
        // double logProb = 0; // log probability
        // double successProb = 0; // success probability

        // if (this.dataSent > 0) {
        //     dataProb = (1.0 * this.dataReceived) / this.dataSent;
        // }
        // if (this.logSent > 0) {
        //     logProb = (1.0 * this.logReceived) / this.logSent;
        // }
        // if (this.dataSent > 0) {
        //     successProb = (1.0 * this.logReceived) / this.dataSent;
        // }

        // write("Data sent: " + this.dataSent + " received: " + this.dataReceived + " probability: " + dataProb);
        // write("Log sent: " + this.logSent + " received: " + this.logReceived + " probability: " + logProb);
        // write("Success probability: " + successProb);

        write("Data sent: " + this.dataSent + " received: " + this.dataReceived);
        write("Log sent: " + this.logSent + " received: " + this.logReceived);
        // write a few lines of code to print out the dataSentList, dataReceivedList, logSentList, and logReceivedList with blank lines between each list
        write("Data sent list: ");
        for (String data : dataSentList) {
            write(data);
        }
        write("");
        write("Data received list: ");
        for (String data : dataReceivedList) {
            write(data);
        }
        write("");
        write("Log sent list: ");
        for (String data : logSentList) {
            write(data);
        }
        write("");
        write("Log received list: ");
        for (String data : logReceivedList) {
            write(data);
        }
        super.done();
    }
}