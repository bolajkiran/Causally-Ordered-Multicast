public class CausallyOrderedMulticast {

    public static void main(String[] args) throws InterruptedException {

        /* Reading input :
         * arg[0] - no. of processes,
         * arg[1] - total no. of send events/messages,
         * arg[2] - current pid
         */
        VectorClock causalOrder = new VectorClock(Integer.parseInt(args[0]), Integer.parseInt(args[1]), args[2]);
        causalOrder.initialize();

        try {

            // Handle processes' connection concurrently
            if(VectorClock.connectionThread != null){
                VectorClock.connectionThread.join();
            }

            // Enforce causal order concurrently
            if(VectorClock.orderThread != null){
                VectorClock.orderThread.join();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Process "+ VectorClock.process_id + " ended!");

    }

}


