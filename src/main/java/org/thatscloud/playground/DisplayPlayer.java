package org.thatscloud.playground;

import java.math.BigDecimal;

public class DisplayPlayer
{
    private String myPlayerName;
    private BigDecimal mySoloRating;
    private BigDecimal myDuoRating;
    private BigDecimal mySquadRating;
    private BigDecimal myAggregateRating;
    private Long myOceaniaSoloRank;
    private Long myOceaniaDuoRank;
    private Long myOceaniaSquadRank;
    private BigDecimal myOverallKillDeathRatio;
    private Long myTotalGamesPlayed;
    private BigDecimal myTop10Percentage;
    private String myAvatarUrl;

    public String getPlayerName()
    {
        return myPlayerName;
    }

    public void setPlayerName( final String playerName )
    {
        myPlayerName = playerName;
    }

    public BigDecimal getSoloRating()
    {
        return mySoloRating;
    }

    public void setSoloRating( final BigDecimal soloRating )
    {
        mySoloRating = soloRating;
    }

    public BigDecimal getDuoRating()
    {
        return myDuoRating;
    }

    public void setDuoRating( final BigDecimal duoRating )
    {
        myDuoRating = duoRating;
    }

    public BigDecimal getSquadRating()
    {
        return mySquadRating;
    }

    public void setSquadRating( final BigDecimal squadRating )
    {
        mySquadRating = squadRating;
    }

    public BigDecimal getAggregateRating()
    {
        return myAggregateRating;
    }

    public void setAggregateRating( final BigDecimal aggregateRating )
    {
        myAggregateRating = aggregateRating;
    }

    public Long getOceaniaSoloRank()
    {
        return myOceaniaSoloRank;
    }

    public void setOceaniaSoloRank( final Long oceaniaSoloRank )
    {
        myOceaniaSoloRank = oceaniaSoloRank;
    }

    public Long getOceaniaDuoRank()
    {
        return myOceaniaDuoRank;
    }

    public void setOceaniaDuoRank( final Long oceaniaDuoRank )
    {
        myOceaniaDuoRank = oceaniaDuoRank;
    }

    public Long getOceaniaSquadRank()
    {
        return myOceaniaSquadRank;
    }

    public void setOceaniaSquadRank( final Long oceaniaSquadRank )
    {
        myOceaniaSquadRank = oceaniaSquadRank;
    }

    public BigDecimal getOverallKillDeathRatio()
    {
        return myOverallKillDeathRatio;
    }

    public void setOverallKillDeathRatio( final BigDecimal overallKillDeathRatio )
    {
        myOverallKillDeathRatio = overallKillDeathRatio;
    }

    public Long getTotalGamesPlayed()
    {
        return myTotalGamesPlayed;
    }

    public void setTotalGamesPlayed( final Long totalGamesPlayed )
    {
        myTotalGamesPlayed = totalGamesPlayed;
    }

    public BigDecimal getTop10Percentage()
    {
        return myTop10Percentage;
    }

    public void setTop10Percentage( final BigDecimal top10Percentage )
    {
        myTop10Percentage = top10Percentage;
    }

    public String getAvatarUrl()
    {
        return myAvatarUrl;
    }

    public void setAvatarUrl( final String avatarUrl )
    {
        myAvatarUrl = avatarUrl;
    }
}
