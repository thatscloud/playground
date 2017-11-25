package org.thatscloud.playground.route.management;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.thatscloud.playground.JsonTransformer;

import spark.Request;
import spark.Response;
import spark.Route;

public abstract class RegistrableRoute implements Route
{
    private final JsonTransformer jsonTransformer = new JsonTransformer();

    public RegistrableRoute(  )
    {
    }

    public JsonTransformer getJsonTransformer()
    {
        return jsonTransformer;
    }

    @Override
    public Object handle( final Request request, final Response response ) throws Exception
    {
        return response.raw();
    }

    protected String getFullUrl( final Request request, final String path )
    {
        final StringBuilder builder = new StringBuilder( request.host() );
        builder.append( path );
        return builder.toString();
    }

    protected Map<String, Object> getNewPageModel( final Request request ) throws SQLException
    {
        final Map<String, Object> page = new HashMap<>();
        return page;
    }

    public abstract void register();
}
