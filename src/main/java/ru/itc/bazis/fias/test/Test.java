package ru.itc.bazis.fias.test;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import ru.itc.bazis.fias.test.theads.SimpleTest;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Test {
    public static void main(String[] args) throws Exception {

        List<String> lUrl = new LinkedList();
        lUrl.add("http://10.3.0.57:8090/getSimpleText");
        lUrl.add("http://10.3.0.57:8090/getSimplePsqlText");
        lUrl.add("http://10.3.0.57:8090/getFullAddressByAoGuid");




        final int counthttp = 5000;
        ArrayList<SimpleTest> testList = new ArrayList(counthttp);
        for (String strUrl : lUrl) {
            testList.clear();
            System.out.println("-----------------");
            System.out.println(strUrl);
            System.out.println("-----------------");
            PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
            cm.setConnectionConfig(new HttpHost("http://10.3.0.57",8090), ConnectionConfig.DEFAULT);
            cm.setMaxTotal(counthttp);
            cm.setDefaultMaxPerRoute (counthttp);

            for (int i = 1; i <= counthttp; i++) {
                testList.add(new SimpleTest(strUrl,HttpClients.custom().setConnectionManager(cm).build()));
            }
            ExecutorService executor = Executors.newFixedThreadPool(counthttp);
            List<Future<Long>> futures = executor.invokeAll(testList);
            executor.shutdown();
            final boolean done = executor.awaitTermination(1, TimeUnit.MINUTES);
            System.out.println("Выполнение засершено:" + done);

            TreeMap<Long, Long> map = new TreeMap<Long, Long>() {
                @Override
                public Long put(Long key, Long value) {
                    if (!super.containsKey(key)) {
                        return super.put(key, 1l);
                    } else {
                        return super.put(key, super.get(key) + 1l);
                    }
                }
            };

            long min = Long.MAX_VALUE;
            long max = 0;

            for (Future<Long> future : futures) {
                long cur = future.get();
                if(cur>-1) {
                    map.put(cur / 10 * 10 + 10 > 10 ? cur / 100 * 100 + 100 : 10, cur);
                }else{
                    map.put(cur,0l);
                }
                if (cur > max) {
                    max = cur;
                }
                if (cur < min) {
                    min = cur;
                }
            }
            System.out.println("min=" + min);
            System.out.println("max=" + max);
            String ch = "<";
            String ch0 = "";
            for (Map.Entry<Long, Long> entry : map.entrySet()) {
                double prc = (entry.getValue() * 100d) / counthttp;
                if(entry.getKey()==110){
                    ch=" ";
                    ch0=">";
                }
                System.out.printf("%-15s | %-8s | %s%n", ch0 + "" + entry.getKey() + " " + ch, prc + "%", entry.getValue());
                ch0 = entry.getKey() + " --> ";
                ch = "";
            }
        }
    }

}
