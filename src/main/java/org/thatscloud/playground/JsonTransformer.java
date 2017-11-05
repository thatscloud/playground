package org.thatscloud.playground;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import spark.ResponseTransformer;

public class JsonTransformer implements ResponseTransformer
{

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public String render( Object model ) throws JsonProcessingException
    {

        if ( model == null )
        {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        if ( model instanceof Stream )
        {
            ( (Stream)model ).forEach( submodel -> {
                try
                {
                    builder.append( mapper.writeValueAsString( submodel ) );
                }
                catch ( Exception e )
                {
                    throw new RuntimeException( "bah", e );
                }
            } );
            return "{" + builder.toString() + "}";
        }
        return mapper.writeValueAsString( model );
    }

    public Map<String, String> stringToMap( String json ) throws JsonParseException,
                                                          JsonMappingException,
                                                          IOException
    {
        // convert JSON string to Map
        return mapper.readValue( json, new TypeReference<Map<String, String>>()
        {
        } );
    }

}