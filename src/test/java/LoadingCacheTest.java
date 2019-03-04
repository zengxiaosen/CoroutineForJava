import com.google.common.cache.*;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LoadingCacheTest {
    private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public static void main(String[] args) throws Exception {
        //test1();
        test2();
    }

    private static void test2() throws Exception{
        LoadingCache<String, String> cacheBuilder = CacheBuilder
                .newBuilder()
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(String s) throws Exception {
                        // not hit cache , do some thing
                        String strProValue = "hello " + s + " !";
                        return strProValue;
                    }

                    @Override
                    public ListenableFuture reload(final String key, String oldValue) {
                        final SettableFuture future = SettableFuture.create();
                        System.out.println("oldValue: " + oldValue);
                        return future;
                    }
                });
        cacheBuilder.put("jerry", "ssdded");
        System.out.println("jerry value: " + cacheBuilder.get("jerry"));
        //System.out.println("peida value:"+ cacheBuilder.get("peida"));
        cacheBuilder.refresh("jerry");

        System.out.println("jerry value:" + cacheBuilder.get("jerry"));
        System.out.println("jerry value:" + cacheBuilder.get("jerry"));


    }


    private static void test1() throws Exception {
        LoadingCache cacheBuilder = CacheBuilder.newBuilder().maximumSize(10).removalListener(
                new RemovalListener() {

                    @Override
                    public void onRemoval(RemovalNotification rn) {
                        System.out.println(rn.getKey() + "被移除");
                    }
                }).build(new CacheLoader() {
            @Override
            public Object load(Object o) throws Exception {
                String strProValue = "load " + (String) o + "!";
                return strProValue;
            }


        });


        System.out.println(cacheBuilder.get("jerry"));
        System.out.println(cacheBuilder.get("jerry"));
//
//        System.out.println(cacheBuilder.get("peida"));
//        System.out.println(cacheBuilder.get("jerry1"));
//        executor.scheduleAtFixedRate(new EchoServer(), 0, 1000, TimeUnit.MILLISECONDS);
        cacheBuilder.refresh("jerry");
        System.out.println(cacheBuilder.get("jerry"));
    }

}
