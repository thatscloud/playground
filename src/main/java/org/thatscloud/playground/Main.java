package org.thatscloud.playground;

import static com.fasterxml.jackson.databind.DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.glassfish.jersey.CommonProperties.FEATURE_AUTO_DISCOVERY_DISABLE;
import static org.glassfish.jersey.CommonProperties.JSON_PROCESSING_FEATURE_DISABLE;
import static org.glassfish.jersey.CommonProperties.METAINF_SERVICES_LOOKUP_DISABLE;
import static spark.Spark.before;
import static spark.Spark.port;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thatscloud.playground.players.DisplayPlayer;
import org.thatscloud.playground.players.PlayersContainer;
import org.thatscloud.playground.rest.model.GameModeStatistics;
import org.thatscloud.playground.rest.model.Player;
import org.thatscloud.playground.rest.model.Statistic;
import org.thatscloud.playground.route.management.RouteManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

public class Main
{
	public static final Logger theLogger = LoggerFactory.getLogger( Main.class );
    private static final long SLEEP_TIME_BETWEEN_API_READS_IN_MILLISECONDS = 1000;
    private static final long SLEEP_TIME_BETWEEN_UPDATE_CYCLES_IN_MILLISECONDS = 15 * 60 * 1000;

    public static void main( final String[] args )
    {
    	init();
    	
    	buildDataGatherThread();
    	
    	RouteManager.insertRoutes();
    	
    	RouteManager.registerStaticContent();
    }

	private static void init() {
		try
        {
            final URI uri = Main.class.getClassLoader().getResource(
                                 "org/thatscloud/playground/Main.class" ).toURI();
            if( uri.getScheme().equals( "jar" ) )
            {
                final String uriString = uri.toString();
                final String jarUriString = uriString.split( "!" )[0];
                final URI jarUri = new URI( jarUriString );
                FileSystems.newFileSystem( jarUri, singletonMap( "create", "true" ) );
                theLogger.info(
                    "Loading static resources from jar( " + jarUri + " )" );
            }
            else
            {
                theLogger.info( "Loading static resources from filesystem" );
            }
        }
        catch( final URISyntaxException | IOException e )
        {
            throw new RuntimeException( e );
        }
		
		port( 6789 );
        before( ( req, res ) ->
        {
            res.raw().setCharacterEncoding( StandardCharsets.UTF_8.toString() );
            if( req.url().endsWith( ".css" ) )
            {
                res.raw().setContentType( "text/css; charset=utf-8" );
            }
            else if( req.url().endsWith( ".js" ) )
            {
                res.raw().setContentType( "text/javascript; charset=utf-8" );
            }
            else
            {
                res.raw().setContentType( "text/html; charset=utf-8" );
            }
        } );
	}

	private static void buildDataGatherThread() {
		
		final Thread t = new Thread( (Runnable)() ->
        {
            try
            {
                while( true )
                {
                    final String apiKey;
                    try
                    {
                        apiKey = readFileToString( new File( "auth.txt" ) ).trim();
                    }
                    catch( final FileNotFoundException e )
                    {
                        theLogger.error( "Could not find file " + new File( "auth.txt" ) );
                        Thread.sleep( SLEEP_TIME_BETWEEN_UPDATE_CYCLES_IN_MILLISECONDS );
                        continue;
                    }
                    catch( final IOException e )
                    {
                        theLogger.error( "Error reading file " + new File( "auth.txt" ), e );
                        Thread.sleep( SLEEP_TIME_BETWEEN_UPDATE_CYCLES_IN_MILLISECONDS );
                        continue;
                    }

                    final List<String> playerNames;
                    try
                    {
                        playerNames =
                            asList( readFileToString( new File( "players.txt" ) )
                                        .split( "\\s*\r?\n\\s*" ) )
                                .stream()
                                .filter( StringUtils::isNotBlank )
                                .map( String::trim )
                                .collect( toList() );
                    }
                    catch( final FileNotFoundException e )
                    {
                        theLogger.error( "Could not find file " + new File( "players.txt" ), e );
                        Thread.sleep( SLEEP_TIME_BETWEEN_UPDATE_CYCLES_IN_MILLISECONDS );
                        continue;
                    }
                    catch( final IOException e )
                    {
                        theLogger.error( "Error reading file " + new File( "players.txt" ) );
                        Thread.sleep( SLEEP_TIME_BETWEEN_UPDATE_CYCLES_IN_MILLISECONDS );
                        continue;
                    }

                    final List<Player> players = new ArrayList<>();
                    for( final String playerName : playerNames )
                    {
                        Client client = null;
                        try
                        {
                            client = ClientBuilder.newClient();
                            final Player player =
                                client
                                    .property( FEATURE_AUTO_DISCOVERY_DISABLE, true )
                                    .property( JSON_PROCESSING_FEATURE_DISABLE, true )
                                    .property( METAINF_SERVICES_LOOKUP_DISABLE, true )
                                    .register(
                                        new JacksonJsonProvider(
                                            new ObjectMapper()
                                                .registerModule( new ParameterNamesModule() )
                                                .registerModule( new Jdk8Module() )
                                                .registerModule( new JavaTimeModule() )
                                                .configure( READ_UNKNOWN_ENUM_VALUES_AS_NULL,
                                                            true ) ) )
                                    .target( "https://pubgtracker.com/api" )
                                    .path( "profile" )
                                    .path( "pc" )
                                    .path( playerName )
                                    .request()
                                    .header( "TRN-Api-Key", apiKey )
                                    .get( Player.class );
                            if( player.get( "error" ) != null )
                            {
                                theLogger.error(
                                    "Error in REST call for player \"" + playerName + "\": " +
                                    player.get( "message" ) );
                                continue;
                            }
                            players.add( player );
                            theLogger.info(
                                "Succesfully updated player \"" + playerName + "\"" );
                        }
                        catch( final Exception e )
                        {
                            theLogger.error(
                                "Error in REST call for player \"" + playerName + "\"", e );
                        }
                        finally
                        {
                            if( client != null )
                            {
                                client.close();
                            }
                        }
                        Thread.sleep( SLEEP_TIME_BETWEEN_API_READS_IN_MILLISECONDS );
                    }

                    // Check for unknown properties
                    for( final Player player : players )
                    {
                        final String playerName = player.getPlayerName();
                        if( player.hasUnknownProperties() )
                        {
                            theLogger.warn( "player[\"" + playerName + "\"] has unknown " +
                                            "properties: " +
                                            player.unknownProperties() );
                        }
                        int gmsCounter = 0;
                        for( final GameModeStatistics gms : player.getStatistics().values() )
                        {
                            if( gms.hasUnknownProperties() )
                            {
                                theLogger.warn( "player[\"" + playerName + "\"].Stats[" +
                                                gmsCounter + "] has unknown " +
                                                "properties: " +
                                                gms.unknownProperties() );
                            }
                            int statCounter = 0;
                            for( final Statistic stat : gms.getStatistics().values() )
                            {
                                if( stat.hasUnknownProperties() )
                                {
                                    theLogger.warn( "player[\"" + playerName + "\"].Stats[" +
                                                    gmsCounter + "].Stats[" + statCounter  +
                                                    "] has unknown properties: " +
                                                    stat.unknownProperties() );
                                }
                                statCounter++;
                            }
                            gmsCounter++;
                        }
                    }

                    final List<DisplayPlayer> newDisplayPlayers =
                        new PlayerToDisplayPlayerMapping().apply( players );
                    if( isNotEmpty( newDisplayPlayers ) )
                    {
                        synchronized( PlayersContainer.theDataLock )
                        {
                        	PlayersContainer.theDisplayPlayers.clear();
                        	PlayersContainer.theDisplayPlayers.addAll( newDisplayPlayers );
                            PlayersContainer.theLastUpdateInstant.setValue( Instant.now() );
                        }
                    }
                    Thread.sleep( SLEEP_TIME_BETWEEN_UPDATE_CYCLES_IN_MILLISECONDS );
                }
            }
            catch( final InterruptedException e ){}
        } );
        t.setDaemon( true );
        t.start();
	}
	
}
