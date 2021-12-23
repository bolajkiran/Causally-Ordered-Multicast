import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


public class VectorClock {
    public static String process_id; //Ex P1
    public static int pid; // Ex. 1

    private static InetAddress server_host_addr = InetAddress.getLoopbackAddress();
    private static ServerSocket server_socket;
    public static int[] process_portList;

    public static String[] process_eventList;
    public static int num_processes;
    // Event buffer for received messages
    public static volatile CopyOnWriteArrayList<ProcessEvent> event_buffer = new CopyOnWriteArrayList<>();

    // Vector Clock of the process
    public static Vector<Integer> process_vc;

    // total number of events received
    private static int events_delivered = 0;

    // event ID of an send event
    public  static String current_eventID = null;


    public static Thread connectionThread = null;
    public static Thread orderThread = null;
    public static Thread ackThread = null;

    public VectorClock(int num_processes, int num_events, String processId) {
        pid = Integer.parseInt(processId);
        process_id = "P" + processId;
        this.num_processes = num_processes;
        process_vc = new Vector<>(num_processes);

        process_portList = new int[num_processes];
        for (int i = 0; i < num_processes; i++) {
            process_portList[i] = 2001 + i;
            process_vc.add(0);
        }
        process_eventList = new String[num_events];
        for (int i = 0; i < num_events; i++) {
            process_eventList[i] = "m" + i;
        }

    }

    // enforce causal ordering
   Comparator<ProcessEvent> comp = new Comparator<ProcessEvent>() {
        @Override
        public int compare(ProcessEvent event1, ProcessEvent event2) {
            for (int i = 0; i < event1.getVectorClock().size(); i++) {
                if (event1.getVectorClock().get(i) != (event2.getVectorClock().get(i) + 1)) {
                    if (event1.getVectorClock().get(i) > event2.getVectorClock().get(i)) {
                        return 1;
                    }
                }
            }
            return -1;
        }
    };


    public void initialize() throws InterruptedException {
        System.out.println("PROCESS: " + process_id);
        System.out.println("\t\t    ___");
        System.out.println("\t\t   / []\\");
        System.out.println("\t\t _|_____|_");
        System.out.println("\t\t| | === | |");
        System.out.println("\t\t|_|  0  |_|");
        System.out.println("\t\t ||_____||");
        System.out.println("\t\t|~ \\___/ ~|");
        System.out.println("\t\t/=\\ /=\\ /=\\");
        System.out.println("\t\t[_] [_] [_]");
        System.out.println("\nEvents delivered to " + process_id + " are\uD83D\uDE0A :");
        System.out.println("------------------------------");

        // Thread 01: manageConnections
        connectionThread = (new Thread(() -> {
            manageConnections();
            return;
        }));

        connectionThread.start();

        // Thread 02: enforceTotalOrder
        orderThread = (new Thread(() -> {
            enforceCausalOrder();
            return;
        }));

        orderThread.start();

        try {
            // Added this delay to wait till all other processes are in the listening state
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        for (int i = pid; i < process_eventList.length;) {
            current_eventID = process_eventList[i];
            ProcessEvent myEvent = new ProcessEvent();
            myEvent.setEvent_id(current_eventID);
            myEvent.setVectorClock(process_vc);
            myEvent.setPid(pid);
            myEvent.setProcess_id(process_id);

            // Multicast event to everyone
            new Thread(() -> {
                sendEvent(myEvent);
            }).start();
            //Thread.sleep(800);
            i = i + num_processes;
        }

    }

    public void manageConnections(){
        int server_port = process_portList[pid];
        try {
            server_socket = new ServerSocket(server_port, 0, server_host_addr);
            //while(events_delivered != process_eventList.length){
            while(events_delivered != process_eventList.length){
                Socket clientSocket = server_socket.accept();
                ObjectInputStream obj_ip = new ObjectInputStream(clientSocket.getInputStream());
                // receive an event
                ProcessEvent event = (ProcessEvent) obj_ip.readObject();
                clientSocket.close();
                (new Thread(() -> {
                    if (event.getEvent_id() != process_id) {
                        event_buffer.add(event);
                        Collections.sort(event_buffer, comp);
                    }
                    return;
                })).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void enforceCausalOrder(){
        // enforce total ordering till all the events are delivered to the process
        //while(events_delivered != process_eventList.length){
        while(events_delivered != process_eventList.length){
            synchronized (event_buffer) {
                if (!event_buffer.isEmpty()) {
                    // get the first event in received buffer
                    ProcessEvent event = event_buffer.get(0);
                    deliverEvent(event);
                    event_buffer.remove(event);
                }
            }
        }
    }

    public void sendEvent(ProcessEvent event){
        try {
            int vectorValue = process_vc.get(pid);
            process_vc.set(pid, vectorValue + 1);
            event.setVectorClock(process_vc);
            Socket socket;
            ObjectOutputStream obj_op;
            // Multicast to all the processes
            for(int port : process_portList){
                if (port == process_portList[pid]) {
                    continue;
                }
                socket = new Socket(server_host_addr,port);
                obj_op = new ObjectOutputStream(socket.getOutputStream());
                obj_op.writeObject(event);

                obj_op.close();
                socket.close();
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void deliverEvent(ProcessEvent event){
        // deliver  received event
            for (int i = 0; i < num_processes; i++) {
                int localVC = process_vc.get(i);
                int messageVC = event.getVectorClock().get(i);
                process_vc.set(i, Math.max(localVC, messageVC));
            }
            System.out.println(process_id + ": " + event.getProcess_id() + "." + event.getEvent_id() + ": " + event.getVectorClock());
            events_delivered++;

    }
}
