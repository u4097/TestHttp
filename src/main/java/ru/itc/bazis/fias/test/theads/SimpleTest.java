package ru.itc.bazis.fias.test.theads;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Callable;

public class SimpleTest implements Callable<Long> {

    private String endpoint;
    private HttpClient client;

    public SimpleTest(String endpoint, HttpClient client) {
        this.endpoint = endpoint;
        this.client = client;
    }

    public Long call() {
        try {
            HttpPost request = new HttpPost(endpoint);
            if (endpoint.endsWith("getFullAddressByAoGuid")) {
                //endpoint=endpoint+"?aoguid="+ UUID.randomUUID().toString();
                ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
                postParameters.add(new BasicNameValuePair("aoguid", UUID.randomUUID().toString()));
                request.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
            }


            long ts = System.currentTimeMillis();
            HttpResponse response = client.execute(request);
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream in = response.getEntity().getContent();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                IOUtils.copy(in,byteArrayOutputStream);
                byteArrayOutputStream.close();
                in.close();
                return System.currentTimeMillis() - ts;
            }

        } catch (IOException e) {
            return -1l;
        }
        return -1l;
    }


}
