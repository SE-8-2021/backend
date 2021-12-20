package pvs.app.service.http;

import io.netty.util.internal.StringUtil;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.HttpMethod;
import java.net.URI;
import java.util.concurrent.ExecutionException;

public class HttpServiceImpl extends HttpService {
    private final Logger Log = LoggerFactory.getLogger(HttpService.class);
    private final OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
            .addInterceptor(new HttpLoggingInterceptor().setLevel(Level.BASIC))
            .build();

    @NotNull
    private Response send(
            final String method,
            @NotNull final URI uri,
            final Headers httpHeaders,
            final RequestBody body
    ) throws ExecutionException, InterruptedException {

        final Request request = new Request.Builder()
                .url(uri.toString())
                .headers(httpHeaders)
                .method(method, body)
                .build();

        final CallbackFuture future = new CallbackFuture();
        okHttpClient.newCall(request).enqueue(future);
        final Response response = future.get();

        Log.debug(String.join(String.valueOf(StringUtil.SPACE),
                HttpService.class.getName(),
                method,
                uri.toString()
        ));

        return response;
    }

    @NotNull
    private Response send(
            final String method,
            final URI uri,
            final Headers httpHeaders
    ) throws ExecutionException, InterruptedException {
        return this.send(method, uri, httpHeaders, null);
    }

    @Override
    public Response get(Headers httpHeaders, URI uri) throws ExecutionException, InterruptedException {
        return this.send(HttpMethod.GET, uri, httpHeaders);
    }

    @Override
    public Response head(Headers httpHeaders, URI uri) throws ExecutionException, InterruptedException {
        return this.send(HttpMethod.HEAD, uri, httpHeaders);
    }

    @Override
    public Response options(Headers httpHeaders, URI uri) throws ExecutionException, InterruptedException {
        return this.send(HttpMethod.OPTIONS, uri, httpHeaders);
    }

    @Override
    public Response post(Headers httpHeaders, URI uri, @NotNull String requestBody) throws ExecutionException, InterruptedException {
        return this.send(HttpMethod.POST, uri, httpHeaders, RequestBody.create(requestBody.getBytes()));
    }

    @Override
    public Response put(Headers httpHeaders, URI uri, @NotNull String requestBody) throws ExecutionException, InterruptedException {
        return this.send(HttpMethod.PUT, uri, httpHeaders, RequestBody.create(requestBody.getBytes()));
    }

    @Override
    public Response delete(Headers httpHeaders, URI uri, @NotNull String requestBody) throws ExecutionException, InterruptedException {
        return this.send(HttpMethod.DELETE, uri, httpHeaders, RequestBody.create(requestBody.getBytes()));
    }

    @Override
    public Response patch(Headers httpHeaders, URI uri, @NotNull String requestBody) throws ExecutionException, InterruptedException {
        return this.send(HttpMethod.PATCH, uri, httpHeaders, RequestBody.create(requestBody.getBytes()));
    }
}
