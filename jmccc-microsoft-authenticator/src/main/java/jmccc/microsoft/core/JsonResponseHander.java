package jmccc.microsoft.core;

import com.google.gson.Gson;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;

public class JsonResponseHander<T> implements HttpClientResponseHandler<T> {
    private final Class<T> clazz;
    private final boolean checkResponseCode;
    private static final Gson GSON = new Gson();

    public JsonResponseHander(Class<T> clazz) {
        this(clazz, true);
    }

    public JsonResponseHander(Class<T> clazz, boolean checkResponseCode) {
        this.clazz = clazz;
        this.checkResponseCode = checkResponseCode;
    }

    @Override
    public T handleResponse(ClassicHttpResponse response) throws HttpException, IOException {
        final HttpEntity entity = response.getEntity();
        String res = null;
        if (entity != null) {
            res = EntityUtils.toString(entity);
        }
        if (checkResponseCode && response.getCode() >= HttpStatus.SC_REDIRECTION) {
            EntityUtils.consume(entity);
            throw new HttpResponseException(response.getCode(), response.getReasonPhrase());
        }
        return GSON.fromJson(res, clazz);
    }
}
