package killim;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.async.RedisAsyncCommands;
import kilim.Mailbox;
import kilim.Pausable;
import kilim.Task;
import io.lettuce.core.RedisClient;

public class GuavaCacheDemo {

    public static void main(String[] args) throws Exception {
        Mailbox mailbox = new Mailbox();
        RedisClient client = RedisClient.create("redis://localhost");
        RedisAsyncCommands<String, String> commands = client.connect().async();
        System.out.println("----");
        commands.set("test1", "test1");

        RedisFuture<String> redisFuture = commands.get("test1");
        String value = redisFuture.get();
        System.out.println(value);

        redisFuture.thenRun(new Runnable() {
            @Override
            public void run() {
                //mailbox.putb(1);
                mailbox.putb("cao");

            }
        });

        mailbox.getb();

        Task.fork(() -> {
            String val = (String)mailbox.getb();
            System.out.println("mailbox get: " + val);
        });
//
//        Task.fork(()->
//        {
//            mailbox.get();
//            System.out.println(redisFuture.get());
//
//        });
    }
}
