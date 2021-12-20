package pvs.app.service.http;

import okhttp3.Headers;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.concurrent.ExecutionException;

interface SingleHttpRequests {
    Response get(final Headers httpHeaders, final URI uri) throws ExecutionException, InterruptedException;

    Response head(final Headers httpHeaders, final URI uri) throws ExecutionException, InterruptedException;

    Response options(final Headers httpHeaders, final URI uri) throws ExecutionException, InterruptedException;

    Response post(final Headers httpHeaders, final URI uri, final String requestBody) throws ExecutionException, InterruptedException;

    Response put(final Headers httpHeaders, final URI uri, final String requestBody) throws ExecutionException, InterruptedException;

    Response delete(final Headers httpHeaders, final URI uri, final String requestBody) throws ExecutionException, InterruptedException;

    Response patch(final Headers httpHeaders, final URI uri, final String requestBody) throws ExecutionException, InterruptedException;
}

@Service
public abstract class HttpService implements SingleHttpRequests {
}
