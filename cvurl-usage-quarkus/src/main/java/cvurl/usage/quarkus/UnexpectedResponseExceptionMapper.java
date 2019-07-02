package cvurl.usage.quarkus;

import coresearch.cvurl.io.exception.UnexpectedResponseException;
import coresearch.cvurl.io.util.HttpStatus;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class UnexpectedResponseExceptionMapper implements ExceptionMapper<UnexpectedResponseException> {

    @Override
    public Response toResponse(UnexpectedResponseException e) {
        return Response.status(HttpStatus.BAD_REQUEST).entity(e.getMessage()).build();
    }
}
