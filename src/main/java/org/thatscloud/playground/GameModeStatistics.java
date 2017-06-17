package org.thatscloud.playground;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GameModeStatistics extends BaseJsonEntity
{
    private String myRegion;
    private String mySeason;
    private String myMatch;
    private List<Statistic> myStatistics;

    @JsonProperty( "Region" )
    public String getRegion()
    {
        return myRegion;
    }

    public void setRegion( final String region )
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
    public String getMatch()
    {
        return myMatch;
    }
    public void setMatch( final String match )
    {
        myMatch = match;
    }

    @JsonProperty( "Stats" )
    public List<Statistic> getStatistics()
    {
        return myStatistics;
    }

    public void setStatistics( final List<Statistic> statistics )
    {
        myStatistics = statistics;
    }
}
