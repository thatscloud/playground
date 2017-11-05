package org.thatscloud.playground.route.management;

import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thatscloud.playground.Main;
import org.thatscloud.playground.route.content.StaticContentRoute;


public class RouteManager
{
	private static final Logger theLogger = LoggerFactory.getLogger( Main.class );
	
    public static void insertRoutes(  )
    {
        Reflections[] routePaths = {
        new Reflections( "org.thatscloud.playground.route.content" ),
        new Reflections( "org.thatscloud.playground.route.rest" )};

        for( Reflections reflections : routePaths)
        {
	        try
	        {
	            Set<Class<? extends RegistrableRoute>> allClasses = reflections
	                .getSubTypesOf( RegistrableRoute.class );
	
	            allClasses.remove( StaticContentRoute.class );
	            for ( Class<? extends RegistrableRoute> clazz : allClasses )
	            {
	                clazz.getConstructor()
	                    .newInstance( ).register();
	                theLogger.info( "Route Registered: " + clazz.getName() );
	            }
	
	        }
	        catch ( Exception e )
	        {
	            throw new RuntimeException( "Couldn't register all routes.", e );
	        }
        }

    }

    public static void registerStaticContent()
    {
    	theLogger.info( "Registering Static Content Route (this must be done last)." );
        new StaticContentRoute().register();
        theLogger.info( "Route Registered: " + StaticContentRoute.class.getName() );

        theLogger.info( "All Routes Registered." );

    }
}
