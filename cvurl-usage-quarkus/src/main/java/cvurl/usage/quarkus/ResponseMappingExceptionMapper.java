package cvurl.usage.quarkus;

import coresearch.cvurl.io.exception.ResponseMappingException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ResponseMappingExceptionMapper implements ExceptionMapper<ResponseMappingException> {

    @Override
    public Response toResponse(ResponseMappingException e) {
        var response = e.getResponse();

        return Response
                .status(response.status())
                .entity(response.getBody())
                .build();
    }
}
