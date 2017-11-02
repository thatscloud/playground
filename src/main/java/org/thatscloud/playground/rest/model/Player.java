package org.thatscloud.playground.rest.model;

import java.time.Instant;
import java.util.Map;
import java.util.function.Function;

import org.thatscloud.playground.util.json.ArrayToMapDeserialiser;
import org.thatscloud.playground.util.json.LenientInstantDeserialiser;
import org.thatscloud.playground.util.json.annotation.JsonKey;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class Player extends BaseJsonEntity
{
    private Long myPlatformId;
    private String myAccountId;
    private String myAvatarUrl;
    private String mySelectedRegion;
    private String myDefaultSeason;
    private Instant myLastUpdated;
    private String myPlayerName;
    private Long myPubgTrackerId;
    private  Map<GameModeStatisticsKey, GameModeStatistics> myStatistics;
    private Object myMatchHistory;

    public Long getPlatformId()
    {
        return myPlatformId;
    }

    public void setPlatformId( final Long platformId )
    {
        myPlatformId = platformId;
    }

    @JsonProperty( "AccountId" )
    public String getAccountId()
    {
        return myAccountId;
    }

    public void setAccountId( final String accountId )
    {
        myAccountId = accountId;
    }

    @JsonProperty( "Avatar" )
    public String getAvatarUrl()
    {
        return myAvatarUrl;
    }

    public void setAvatarUrl( final String avatarUrl )
    {
        myAvatarUrl = avatarUrl;
    }

    public String getSelectedRegion()
    {
        return mySelectedRegion;
    }

    public void setSelectedRegion( final String selectedRegion )
    {
        mySelectedRegion = selectedRegion;
    }

    public String getDefaultSeason()
    {
        return myDefaultSeason;
    }

    public void setDefaultSeason( final String defaultSeason )
    {
        myDefaultSeason = defaultSeason;
    }

    @JsonProperty( "LastUpdated" )
    @JsonDeserialize( using = LenientInstantDeserialiser.class )
    public Instant getLastUpdated()
    {
        return myLastUpdated;
    }

    public void setLastUpdated( final Instant lastUpdated )
    {
        myLastUpdated = lastUpdated;
    }

    @JsonProperty( "PlayerName" )
    public String getPlayerName()
    {
        return myPlayerName;
    }

    public void setPlayerName( final String playerName )
    {
        myPlayerName = playerName;
    }

    @JsonProperty( "PubgTrackerId" )
    public Long getPubgTrackerId()
    {
        return myPubgTrackerId;
    }

    public void setPubgTrackerId( final Long pubgTrackerId )
    {
        myPubgTrackerId = pubgTrackerId;
    }

    public static class GameModeStatisticsKeyMapper implements
        Function<GameModeStatistics, GameModeStatisticsKey>
    {
        @Override
        public GameModeStatisticsKey apply( final GameModeStatistics t )
        {
            return new GameModeStatisticsKey( t.getRegion(),
                                              t.getSeason(),
                                              t.getGameMode() );
        }
    }

    @JsonProperty( "Stats" )
    @JsonKey( GameModeStatisticsKeyMapper.class )
    @JsonDeserialize( using = ArrayToMapDeserialiser.class )
    public Map<GameModeStatisticsKey, GameModeStatistics> getStatistics()
    {
        return myStatistics;
    }

    public void setStatistics(
        final Map<GameModeStatisticsKey, GameModeStatistics> statistics )
    {
        myStatistics = statistics;
    }

    @JsonProperty( "MatchHistory" )
    public Object getMatchHistory()
    {
        return myMatchHistory;
    }

    public void setMatchHistory( final Object matchHistory )
    {
        myMatchHistory = matchHistory;
    }
}
