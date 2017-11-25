package org.thatscloud.playground.route.rest;

import static spark.Spark.get;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.thatscloud.playground.players.PlayersContainer;
import org.thatscloud.playground.route.management.RegistrableRoute;

public class RankingsRestRoute extends RegistrableRoute
{

    @Override
    public void register()
    {
        get( "/rest/rankings", "application/json", ( request, response ) -> {
            Map<String, Object> page = getNewPageModel( request );
            synchronized ( PlayersContainer.theDataLock )
            {
                page.put( "players", PlayersContainer.theDisplayPlayers );

                if(PlayersContainer.theLastUpdateInstant.getValue() != null)
                {
                    page.put( "lastUpdate",
                        DateTimeFormatter.ofPattern( "dd/MM/yyyy (HH:mm:ss)" )
                            .format( PlayersContainer.theLastUpdateInstant.getValue()
                                .atZone( ZoneId.of( "Australia/NSW" ) ) ) );
                }
                else
                {
                    page.put( "lastUpdate", "Not updated yet" );
                }
                page.put( "error", "" );
                return page;
            }
        }, getJsonTransformer() );
    }

}
