package org.thatscloud.playground;


import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class StaticContentRoute implements Route
{
    @Override
    public Object handle( final Request request, final Response response )
        throws Exception
    {
        final String uriString;
        if( request.splat() == null ||
            request.splat().length == 0 )
        {
            uriString = "";
        }
        else
        {
            uriString = request.splat()[0];
        }

        final boolean notFound;
        final URL url =
            getClass().getClassLoader().getResource(
                "org/thatscloud/playground/staticcontent/" + uriString );
        if( url == null )
        {
            notFound = true;
        }
        else
        {
            final Path path = Paths.get( url.toURI() );
            if( Files.isDirectory( path ) )
            {
                notFound = true;
            }
            else
            {
                notFound = false;
            }
        }

        if( notFound )
        {
            Spark.halt( 404, "Not Found" );
        }
        else
        {
            try( final InputStream in =
                     getClass().getClassLoader().getResourceAsStream(
                         "org/thatscloud/playground/staticcontent/" + uriString );
                final OutputStream out = response.raw().getOutputStream() )
            {
                IOUtils.copy( in, out );
            }
        }
        return response.raw();
    }
}
