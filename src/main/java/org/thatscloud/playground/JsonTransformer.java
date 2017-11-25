package org.thatscloud.playground;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import spark.ResponseTransformer;

public class JsonTransformer implements ResponseTransformer
{

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public String render( Object model ) throws JsonProcessingException
    {
        return mapper.writeValueAsString( model );
    }

}