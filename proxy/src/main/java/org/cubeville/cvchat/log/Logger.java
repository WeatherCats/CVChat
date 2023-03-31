package org.cubeville.cvchat.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Logger implements Runnable
{
    private ConcurrentLinkedQueue<String> queue;
    private PrintWriter printWriter;
    private FileWriter fileWriter;
    private BufferedWriter bufferedWriter;
    private File logfile;
    
    private static Logger instance;
    public static Logger getInstance() {
        return instance;
    }
    
    public Logger(File logfile) {
        instance = this;
        queue = new ConcurrentLinkedQueue<>();
        this.logfile = logfile;
        openFiles();
    }

    private void openFiles() {
        try {
            if(printWriter != null) printWriter.close();
            if(bufferedWriter != null) bufferedWriter.close();
            if(fileWriter != null) fileWriter.close();
            fileWriter = new FileWriter(logfile, true);
            bufferedWriter = new BufferedWriter(fileWriter);
            printWriter = new PrintWriter(bufferedWriter);
        }
        catch(IOException ignored) {}

    }
    
    public void log(String message) {
        queue.add(message);
    }

    public void logWithHeader(String message) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        log(sdf.format(cal.getTime()) + " " + message);
    }
    
    public void run() {
        if(queue.isEmpty()) return;
        
        if(!logfile.exists()) {
            System.out.println("Logfile disappeared, reopening logfile.");
            openFiles();
        }

        while(!queue.isEmpty()) {
            String message = queue.poll();
            printWriter.println(message);
        }
        try {
            bufferedWriter.flush();
            fileWriter.flush();
        }
        catch(IOException e) {
            System.out.println("IO error, reopening logfile.");
            openFiles();
        }
    }
}
