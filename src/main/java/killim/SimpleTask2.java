package killim;

import kilim.Mailbox;
import kilim.Pausable;
import kilim.Task;

public class SimpleTask2 extends Task {
    static Mailbox<String> mb = new Mailbox<>();
    static Mailbox<String> exitmb = new Mailbox<>();

    public static void main(String[] args) throws Exception {
        Task t = new SimpleTask2().start();
        t.informOnExit(exitmb);
        mb.putnb("Hello ");
        mb.putnb("World\n");
        mb.putnb("done");

        exitmb.getb();
        System.exit(0);

    }

    @Override
    public void execute() throws Pausable, Exception {
        while (true) {
            String s = mb.get();
            if (s.equals("done")) break;
            System.out.print(s);
        }
        Task.exit(0); // Strictly optional.
    }
}
