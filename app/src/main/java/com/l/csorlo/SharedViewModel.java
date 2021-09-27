package com.l.csorlo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class SharedViewModel extends ViewModel {

    public static final int SIZE_LED = 6;
    private static final int DEFAULT_LED = 30;
    private static final int TIMEOUT = 200;
    private static final MutableLiveData<Integer> csorlo = new MutableLiveData<>();
    private static final MutableLiveData<String> logm = new MutableLiveData<>();
    private static final MutableLiveData<Boolean> connected = new MutableLiveData<>(false);
    private static final int size_b = 9; // 0 csorlo, 1-6 ledek, 8-9 relay
    private static final int[] b = new int[size_b];
    private static final boolean[] switchIsChecked = new boolean[SIZE_LED];
    private static boolean stop = false;
    private static boolean first = true;
    private static long cycle = 0;

    private static final Thread connectionTimeOutThread = new Thread(() -> {
        long c = cycle;
        try {
            while (!stop) {
                Thread.sleep(TIMEOUT);
                if (c == cycle) {
                    first = true;
                    System.err.println("kifutottunk az idobol!");
                    connected.postValue(false);
                } else c = cycle;
            }
        } catch (InterruptedException e) {
            System.err.println("connectionTimeOutThread megszakadt!");
            e.printStackTrace();
        }
    });

    private static final Thread thread = new Thread(() -> {
        try {
            ServerSocket listener = new ServerSocket(9091);
            if (!connectionTimeOutThread.isAlive())
                connectionTimeOutThread.start();
            first = true;
            String s = "";
            while (!stop) {
                try {
                    //Thread.sleep(10);
                    //System.out.println("Waiting for client ...");
                    Socket socket = listener.accept();
                    socket.setSoTimeout(700);
  //                socket.setKeepAlive(true);
                    //System.out.println("Client Connected");
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                    System.out.println("Client response: " + in.readLine());


                    if (first) {
                        out.write("zzzQ");
                        out.write("100Q");
                        out.flush();
                        for (int i = 0; i < SIZE_LED; i++) {
                            b[i + 1] = Integer.parseInt(in.readLine());
                            switchIsChecked[i] = b[i + 1] != 0;
                            System.out.print(b[i + 1] + "    ");
                        }
                        System.out.println(" ");
                        b[0] = 0;
                        csorlo.postValue(b[0]);
                        first = false;
                        connected.postValue(true);
                        socket.close();
                        continue;
                    }

                    // System.out.println("Sending Message...");

                    out.write("zzzQ");
                    for (int bs : b)
                        out.write(bs + "Q");
                    //out.write(b[0] + "Q");
                    out.flush();

                    String ss = "sending... cs:" + b[0] + ", L1:" + b[1] * 2 + ", L2:" + b[2] * 2 + ", L3:" + b[3] * 2 + ", L4:" + b[4] * 2 + "\n";
                    if (!s.equals(ss)) {
                        s = ss;
                        logm.postValue(s + logm.getValue());
                    }

                    socket.close();
                    cycle++;
                } catch (SocketTimeoutException socketTimeoutException) {
                    first = true;
                    System.err.println("Socket exception");
                    connected.postValue(false);

                } catch (Exception e) {
                    System.err.println("Megfogott Exception!");
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("Megfogott Exception! - De kilepett a thread-bol!!!");
            e.printStackTrace();
        }
    });
    private String log;
    private long time;

    public SharedViewModel() {
        onStart();
    }

    public static void onStart() {
        if (!thread.isAlive())
            thread.start();
        System.out.println("Thread started");
        csorlo.setValue(0);
    }

    public static void stopThread() {
        stop = true;
    }

    public int getLed(int i) {
        return b[i + 1];
    }

    public boolean getSwitchIsChecked(int i) {
        return switchIsChecked[i];
    }

    public void setSwitchIsChecked(int i, boolean switchIsCheckedd) {
        switchIsChecked[i] = switchIsCheckedd;
    }

    public LiveData<Integer> getCsorlo() {
        return csorlo;
    }

    public void setCsorlo(int i) {
        if (b[0] == 0) time = System.currentTimeMillis();
        b[0] = i;
        csorlo.setValue(i);
        String s = "";
        if (i == 1)
            s = "Csorlo Elore \n";
        else if (i == 2)
            s = "Csorlo Hatra \n";
        else if (i == 0) {
            long t = (System.currentTimeMillis() - time) / 1000;
            s = "Csorlo STOP - " + t + "s \n";
        }
        log = s + log;
        logm.setValue(log);
    }

    public void setLED(int i, int value) {
        b[i + 1] = value;
    }

    public LiveData<String> getLog() {
        return logm;
    }

    public LiveData<Boolean> isConnected() {
        return connected;
    }

    @Override
    protected void onCleared() {
        stop = true;
        super.onCleared();
    }
}