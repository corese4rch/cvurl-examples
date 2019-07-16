package cvurl.usage.quarkus;

import coresearch.cvurl.io.constant.HttpStatus;
import coresearch.cvurl.io.exception.UnexpectedResponseException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class UnexpectedResponseExceptionMapper implements ExceptionMapper<UnexpectedResponseException> {

    @Override
    public Response toResponse(UnexpectedResponseException e) {
        var response = e.getResponse();

        return Response
                .status(response.status())
                .entity(response.getBody())
                .build();
    }
}
