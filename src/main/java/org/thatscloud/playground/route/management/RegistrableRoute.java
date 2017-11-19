package org.thatscloud.playground.route.management;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
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
        response.redirect( getFullUrl( request, "/404" ) );
        return response.raw();
    }

    protected String getFullUrl( final Request request, final String path )
    {
        final StringBuilder builder = new StringBuilder( request.host() );
        builder.insert( 0, "http://" );
        builder.append( path );
        return builder.toString();
    }

    protected Map<String, Object> getNewPageModel( final Request request ) throws SQLException
    {
        final Map<String, Object> page = new HashMap<>();
        return page;
    }

    public abstract void register();


    //Helper methods for routes
    protected static String formatDecimal( final BigDecimal bd )
    {
        return bd == null ? "N/A" : String.format( "%.2f", bd );
    }

    protected static String formatPercentage( final BigDecimal bd )
    {
        return bd == null ? "N/A" : String.format( "%.2f%%",
                                                   bd.multiply( BigDecimal.valueOf( 100 ) ) );
    }

    protected static String formatInteger( final Long l )
    {
        if( l == null )
        {
            return "N/A";
        }
        return NumberFormat.getIntegerInstance( Locale.forLanguageTag( "en-AU" ) ).format(  l );
    }

    protected static String formatOrdinal( final Long l )
    {
        final NumberFormat nf =
            NumberFormat.getIntegerInstance( Locale.forLanguageTag( "en-AU" ) );
        if( l == null )
        {
            return "N/A";
        }
        if(  l / 10 % 10 == 1 )
        {
            return nf.format( l ) + "th";
        }

        switch( (int)( l % 10 ) )
        {
            case 1:
            {
                return nf.format( l ) + "st";
            }
            case 2:
            {
                return nf.format( l ) + "nd";
            }
            case 3:
            {
                return nf.format( l ) + "rd";
            }
            default:
            {
                return nf.format( l ) + "th";
            }
        }
    }
}
