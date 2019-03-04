package killim;

import kilim.Mailbox;
import kilim.Pausable;
import kilim.Task;

public class SimpleTask extends Task {

    static Mailbox<String> mb = new Mailbox<String>();

    public static void main(String[] args) throws Exception {
        new SimpleTask().start();
        Thread.sleep(10);
        mb.putnb("Hello ");
        mb.putnb("World\n");
        mb.putnb("done");
    }

    @Override
    public void execute() throws Pausable, Exception {
        while (true) {
            String s = mb.get();
            if (s.equals("done")){
                break;
            }
            System.out.println(s);
        }

        System.exit(0);
    }
}
