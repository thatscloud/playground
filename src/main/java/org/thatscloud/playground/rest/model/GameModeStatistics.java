package org.thatscloud.playground.rest.model;

import java.util.Map;
import java.util.function.Function;

import org.thatscloud.playground.rest.model.constant.GameMode;
import org.thatscloud.playground.rest.model.constant.Region;
import org.thatscloud.playground.rest.model.constant.StatisticField;
import org.thatscloud.playground.util.json.ArrayToMapDeserialiser;
import org.thatscloud.playground.util.json.annotation.JsonKey;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class GameModeStatistics extends BaseJsonEntity
{
    private Region myRegion;
    private String mySeason;
    private GameMode myGameMode;
    private Map<StatisticField, Statistic> myStatistics;

    @JsonProperty( "Region" )
    public Region getRegion()
    {
        return myRegion;
    }

    public void setRegion( final Region region )
    {
        myRegion = region;
    }

    @JsonProperty( "Season" )
    public String getSeason()
    {
        return mySeason;
    }

    public void setSeason( final String season )
    {
        mySeason = season;
    }

    @JsonProperty( "Match" )
    public GameMode getGameMode()
    {
        return myGameMode;
    }
    public void setGameMode( final GameMode gameMode )
    {
        myGameMode = gameMode;
    }

    public static class StatisticsKeyMapper implements Function<Statistic, StatisticField>
    {
        @Override
        public StatisticField apply( final Statistic t )
        {
            return t.getField();
        }
    }

    @JsonProperty( "Stats" )
    @JsonKey( StatisticsKeyMapper.class )
    @JsonDeserialize( using = ArrayToMapDeserialiser.class )
    public Map<StatisticField, Statistic> getStatistics()
    {
        return myStatistics;
    }

    public void setStatistics( final Map<StatisticField, Statistic> statistics )
    {
        myStatistics = statistics;
    }
}
