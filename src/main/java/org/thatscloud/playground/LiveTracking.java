package org.thatscloud.playground;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class LiveTracking extends BaseJsonEntity
{
    private Long myMatch;
    private String myMatchDisplay;
    private Long mySeason;
    private Long myRegionId;
    private String myRegion;
    private Instant myDate;
    private BigDecimal myDelta;
    private BigDecimal myValue;
    private String myMessage;

    @JsonProperty( "Match" )
    public Long getMatch()
    {
        return myMatch;
    }

    public void setMatch( final Long match )
    {
        myMatch = match;
    }

    @JsonProperty( "MatchDisplay" )
    public String getMatchDisplay()
    {
        return myMatchDisplay;
    }

    public void setMatchDisplay( final String matchDisplay )
    {
        myMatchDisplay = matchDisplay;
    }

    @JsonProperty( "Season" )
    public Long getSeason()
    {
        return mySeason;
    }

    public void setSeason( final Long season )
    {
        mySeason = season;
    }

    @JsonProperty( "RegionId" )
    public Long getRegionId()
    {
        return myRegionId;
    }

    public void setRegionId( final Long regionId )
    {
        myRegionId = regionId;
    }

    @JsonProperty( "Region" )
    public String getRegion()
    {
        return myRegion;
    }

    public void setRegion( final String region )
    {
        myRegion = region;
    }



    @JsonProperty( "Date" )
    @JsonDeserialize( using = LenientInstantDeserialiser.class )
    public Instant getDate()
    {
        return myDate;
    }

    public void setDate( final Instant date )
    {
        myDate = date;
    }

    @JsonProperty( "Delta" )
    public BigDecimal getDelta()
    {
        return myDelta;
    }

    public void setDelta( final BigDecimal delta )
    {
        myDelta = delta;
    }

    @JsonProperty( "Value" )
    public BigDecimal getValue()
    {
        return myValue;
    }

    public void setValue( final BigDecimal value )
    {
        myValue = value;
    }

    public String getMessage()
    {
        return myMessage;
    }

    public void setMessage( final String message )
    {
        myMessage = message;
    }
}
