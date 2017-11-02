package org.thatscloud.playground.rest.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.thatscloud.playground.rest.model.constant.GameMode;
import org.thatscloud.playground.rest.model.constant.Region;

public final class GameModeStatisticsKey
{
    private final Region myRegion;
    private final String mySeason;
    private final GameMode myGameMode;

    public GameModeStatisticsKey( final Region region,
                                  final String season,
                                  final GameMode gameMode )
    {
        myRegion = region;
        mySeason = season;
        myGameMode = gameMode;
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder()
            .append( myRegion )
            .append( mySeason )
            .append( myGameMode )
            .build();
    }

    @Override
    public boolean equals( final Object obj )
    {
        return
            obj instanceof GameModeStatisticsKey &&
            new EqualsBuilder()
                .append( myRegion, ( (GameModeStatisticsKey)obj ).getRegion() )
                .append( mySeason, ( (GameModeStatisticsKey)obj ).getSeason() )
                .append( myGameMode, ( (GameModeStatisticsKey)obj ).getGameMode() )
                .build();
    }

    public Region getRegion()
    {
        return myRegion;
    }

    public String getSeason()
    {
        return mySeason;
    }

    public GameMode getGameMode()
    {
        return myGameMode;
    }
}
