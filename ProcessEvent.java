import java.io.Serializable;
import java.util.Comparator;
import java.util.Vector;

public class ProcessEvent implements Serializable, Comparator<ProcessEvent> {
    private static final long serialVersionUID = 1001626634L;
    private String process_id;
    private int pid;
    private Vector<Integer> vectorClock;
    private String event_id;

    public String getProcess_id() {
        return process_id;
    }

    public void setProcess_id(String process_id) {
        this.process_id = process_id;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public Vector<Integer> getVectorClock() {
        return vectorClock;
    }

    public void setVectorClock(Vector<Integer> vectorClock) {
        this.vectorClock = vectorClock;
    }

    public String getEvent_id() {
        return event_id;
    }

    public void setEvent_id(String event_id) {
        this.event_id = event_id;
    }

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


}


