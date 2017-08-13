package org.thatscloud.playground;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.glassfish.jersey.CommonProperties.FEATURE_AUTO_DISCOVERY_DISABLE;
import static org.glassfish.jersey.CommonProperties.JSON_PROCESSING_FEATURE_DISABLE;
import static org.glassfish.jersey.CommonProperties.METAINF_SERVICES_LOOKUP_DISABLE;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.port;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.LongStream;

import javax.ws.rs.client.ClientBuilder;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

public class Main
{
    private static final Logger theLogger = LoggerFactory.getLogger( Main.class );
    private static final Object theDataLock = new Object();

    private static final List<DisplayPlayer> theDisplayPlayers = new ArrayList<>();
    private static final Mutable<Instant> theLastUpdateInstant = new MutableObject<>();

    private static final long SLEEP_TIME_BETWEEN_API_READS_IN_MILLISECONDS = 1000;
    private static final long SLEEP_TIME_BETWEEN_UPDATE_CYCLES_IN_MILLISECONDS = 15 * 60 * 1000;

    public static void main( final String[] args )
    {
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

        get( "/", ( req, res ) ->
        {
            synchronized( theDataLock )
            {
                final StringBuilder sb = new StringBuilder();
                sb.append( "<!DOCTYPE html>" );
                sb.append( "<html lang=\"en\">" );
                sb.append( "<head>" );
                sb.append( "<title>Playground - A PUBG Tracker</title>" );
                sb.append( "<link href=\"//fonts.googleapis.com/css?family=Nunito:300,400,600\" " +
                           "rel=\"stylesheet\" type=\"text/css\">" );
                sb.append( "<link rel=\"stylesheet\" href=\"css/normalize.css\">" );
                sb.append( "<link rel=\"stylesheet\" href=\"css/skeleton.css\">" );
                sb.append( "<link rel=\"stylesheet\" href=\"css/custom.css\">" );
                sb.append( "<link rel=\"stylesheet\" href=\"css/tablesorter-theme.css\">" );
                sb.append( "<script src=\"https://cdn.jsdelivr.net/jquery/3.2.1/jquery.min.js\"></script>" );
                sb.append( "<script src=\"https://cdn.jsdelivr.net/tablesorter/2.28.9/js/jquery.tablesorter.min.js\"></script>" );
                sb.append( "<script src=\"js/sorter.js\"></script>" );
                sb.append( "</head>" );
                sb.append( "<body>" );
                sb.append( "<div class=\"section\"><div class=\"container\">" );
                sb.append( "<h1 style=\"text-align: center; padding-top: 20px;\">Rankings</h1>" );
                sb.append( "<table id=\"resultsTable\" class=\"tablesorter\">" );
                sb.append( "<thead><tr>" +
                           "<th class=\"sorter-playground-rank\">Rank</th>" +
                           "<th>Player</th>" +
                           "<th>Overall Rating</th>" +
                           "<th>Solo Rating</th>" +
                           "<th>Duo Rating</th>" +
                           "<th>Squad Rating</th>" +
                           "<th class=\"sorter-playground-ordinal\">OC Solo Rank</th>" +
                           "<th class=\"sorter-playground-ordinal\">OC Duo Rank</th>" +
                           "<th class=\"sorter-playground-ordinal\">OC Squad Rank</th>" +
                           "<th>Overall K/D Ratio</th>" +
                           "<th>Total Games Played</th>" +
                           "<th>Top 10 Percentage</th>" +
                           "</tr></thead>" );
                sb.append( "<tbody>" );

                int rank = 0;
                for( final DisplayPlayer displayPlayer : theDisplayPlayers )
                {
                    sb.append( "<tr>" );
                    sb.append( "<td>" );
                    rank++;
                    if( displayPlayer.getAggregateRating() == null )
                    {
                        sb.append( "N/A" );
                    }
                    else
                    {
                        sb.append( rank );
                    }
                    sb.append( "</td>" );
                    sb.append( "<td>" );
                    sb.append( "<a href=\"https://pubgtracker.com/profile/pc/" );
                    sb.append( URLEncoder.encode( displayPlayer.getPlayerName(),
                                                  StandardCharsets.UTF_8.name() ) );
                    sb.append( "\">" );
                    if( isNotBlank( displayPlayer.getAvatarUrl() ) )
                    {
                        sb.append( "<div><img alt=\"" );
                        sb.append( displayPlayer.getPlayerName() );
                        sb.append( "'s Avatar\" " );
                        sb.append( "title=\"" );
                        sb.append( displayPlayer.getPlayerName() );
                        sb.append( "'s Avatar\" " );
                        sb.append( "src=\"" );
                        sb.append( displayPlayer.getAvatarUrl() );
                        sb.append( "\" /></div>" );
                    }
                    sb.append( "<div>" );
                    sb.append( displayPlayer.getPlayerName() );
                    sb.append( "</div>" );
                    sb.append( "</a>" );
                    sb.append( "</td>" );
                    sb.append( "<td><strong>" );
                    sb.append( formatDecimal( displayPlayer.getAggregateRating() ) );
                    sb.append( "</strong></td>" );
                    sb.append( "<td>" );
                    sb.append( formatDecimal( displayPlayer.getSoloRating() ) );
                    sb.append( "</td>" );
                    sb.append( "<td>" );
                    sb.append( formatDecimal( displayPlayer.getDuoRating() ) );
                    sb.append( "</td>" );
                    sb.append( "<td>" );
                    sb.append( formatDecimal( displayPlayer.getSquadRating() ) );
                    sb.append( "</td>" );
                    sb.append( "<td>" );
                    sb.append( formatOrdinal( displayPlayer.getOceaniaSoloRank() ) );
                    sb.append( "</td>" );
                    sb.append( "<td>" );
                    sb.append( formatOrdinal( displayPlayer.getOceaniaDuoRank() ) );
                    sb.append( "</td>" );
                    sb.append( "<td>" );
                    sb.append( formatOrdinal( displayPlayer.getOceaniaSquadRank() ) );
                    sb.append( "</td>" );
                    sb.append( "<td>" );
                    sb.append( formatDecimal( displayPlayer.getOverallKillDeathRatio() ) );
                    sb.append( "</td>" );
                    sb.append( "<td>" );
                    sb.append( formatInteger( displayPlayer.getTotalGamesPlayed() ) );
                    sb.append( "</td>" );
                    sb.append( "<td>" );
                    sb.append( formatPercentage( displayPlayer.getTop10Percentage() ) );
                    sb.append( "</td>" );
                    sb.append( "</tr>" );
                }
                sb.append( "</tbody>" );
                sb.append( "</table>" );
                if( theLastUpdateInstant.getValue() != null )
                {
                    sb.append( "<p>" );
                    sb.append( "<span>Last Updated: " );
                    final Map<Long, String> DAYS_LOOKUP =
                        LongStream
                            .rangeClosed( 1, 31 )
                            .boxed()
                            .collect( toMap( Function.identity(), Main::formatOrdinal ) );

                    ;
                    sb.append(
                        new DateTimeFormatterBuilder()
                            .appendPattern( "EEEE" )
                            .appendLiteral( ", " )
                            .appendText( ChronoField.DAY_OF_MONTH, DAYS_LOOKUP )
                            .appendLiteral(" ")
                            .appendPattern( "MMMM" )
                            .appendLiteral( " " )
                            .appendPattern( "yyyy" )
                            .appendLiteral( ", " )
                            .appendPattern( "h" )
                            .appendLiteral( ":" )
                            .appendPattern( "mm" )
                            .appendLiteral( " " )
                            .appendPattern( "a" )
                            .toFormatter( Locale.forLanguageTag( "en-AU" ) )
                            .format( theLastUpdateInstant
                                        .getValue()
                                        .atZone( ZoneId.of( "Australia/NSW" ) ) ) );
                    sb.append( "</span> " );
                    sb.append( "<span>( " );
                    sb.append( DateTimeFormatter.ISO_INSTANT
                                   .format( theLastUpdateInstant.getValue() ) );
                    sb.append( " )</span>" );
                    sb.append( "</p>" );
                }
                sb.append( "</div></div>" );
                sb.append( "</body>" );
                sb.append( "</html>" );
                return sb;
            }
        } );

        get( "/*", new StaticContentRoute() );


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
                        try
                        {
                            final Player player =
                                ClientBuilder.newClient()
                                    .property( FEATURE_AUTO_DISCOVERY_DISABLE, true )
                                    .property( JSON_PROCESSING_FEATURE_DISABLE, true )
                                    .property( METAINF_SERVICES_LOOKUP_DISABLE, true )
                                    .register(
                                        new JacksonJsonProvider(
                                            new ObjectMapper()
                                                .registerModule( new ParameterNamesModule() )
                                                .registerModule( new Jdk8Module() )
                                                .registerModule( new JavaTimeModule() ) ) )
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
                        int liveTrackingCounter = 0;
                        for( final LiveTracking liveTracking : player.getLiveTracking() )
                        {
                            if( liveTracking.hasUnknownProperties() )
                            {
                                theLogger.warn( "player[\"" + playerName + "\"].LiveTracking[" +
                                                liveTrackingCounter + "] has unknown " +
                                                "properties: " +
                                                liveTracking.unknownProperties() );
                            }
                            liveTrackingCounter++;
                        }
                        int gmsCounter = 0;
                        for( final GameModeStatistics gms : player.getStatistics() )
                        {
                            if( gms.hasUnknownProperties() )
                            {
                                theLogger.warn( "player[\"" + playerName + "\"].Stats[" +
                                                gmsCounter + "] has unknown " +
                                                "properties: " +
                                                gms.unknownProperties() );
                            }
                            int statCounter = 0;
                            for( final Statistic stat : gms.getStatistics() )
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
                        synchronized( theDataLock )
                        {
                            theDisplayPlayers.clear();
                            theDisplayPlayers.addAll( newDisplayPlayers );
                            theLastUpdateInstant.setValue( Instant.now() );
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

    private static String formatDecimal( final BigDecimal bd )
    {
        return bd == null ? "N/A" : String.format( "%.2f", bd );
    }

    private static String formatPercentage( final BigDecimal bd )
    {
        return bd == null ? "N/A" : String.format( "%.2f%%",
                                                   bd.multiply( BigDecimal.valueOf( 100 ) ) );
    }

    private static String formatInteger( final Long l )
    {
        if( l == null )
        {
            return "N/A";
        }
        return NumberFormat.getIntegerInstance( Locale.forLanguageTag( "en-AU" ) ).format(  l );
    }

    private static String formatOrdinal( final Long l )
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
