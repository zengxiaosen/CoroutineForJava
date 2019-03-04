package killim;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.async.RedisAsyncCommands;
import kilim.Mailbox;
import kilim.Task;

import java.util.concurrent.ExecutionException;

public class KilimRedis {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        if (kilim.tools.Kilim.trampoline(false, args)) {

            return;
        }

        Mailbox mailbox = new Mailbox();
        RedisClient client = RedisClient.create("redis://localhost");
        RedisAsyncCommands<String, String> commands = client.connect().async();
        //commands.set("test", "test");
        //System.out.println(commands.get("test"));
        RedisFuture<String> redisFuture = commands.get("test");
        redisFuture.thenRun(new Runnable() {
            @Override
            public void run() {
                mailbox.putb(1);
            }
        });


        Task.fork(()->
        {
            int val = (int)mailbox.getb();
            System.out.println("value: " + val);
            System.out.println(redisFuture.get());

        });



    }
}
