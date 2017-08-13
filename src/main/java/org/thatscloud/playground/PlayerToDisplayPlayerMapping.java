package org.thatscloud.playground;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.compare;
import static org.apache.commons.lang3.StringUtils.compareIgnoreCase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;


public class PlayerToDisplayPlayerMapping implements Function<List<Player>, List<DisplayPlayer>>
{
    @Override
    public List<DisplayPlayer> apply( final List<Player> players )
    {
        final Optional<String> latestSeasonOpt = findLatestSeason( players );
        if( !latestSeasonOpt.isPresent() )
        {
            return Collections.emptyList();
        }
        final String latestSeason = latestSeasonOpt.get();
        final BigDecimal worstSoloRating = findWorstModeRating( players, latestSeason, Mode.SOLO );
        final BigDecimal worstDuoRating = findWorstModeRating( players, latestSeason, Mode.DUO );
        final BigDecimal worstSquadRating =
            findWorstModeRating( players, latestSeason, Mode.SQUAD );
        return players.stream()
            .map( p ->
            {
                long totalKills = 0;
                long totalDeaths = 0;
                long totalTop10s = 0;
                final DisplayPlayer displayPlayer = new DisplayPlayer();
                displayPlayer.setPlayerName( p.getPlayerName() );
                displayPlayer.setTotalGamesPlayed( 0L );
                displayPlayer.setAvatarUrl( p.getAvatarUrl() );
                final String defaultSeason = p.getDefaultSeason();
                if( !Objects.equals( p.getDefaultSeason(), latestSeason ) )
                {
                    return displayPlayer;
                }
                boolean killDeathRatioCalculable = true;

                for( final GameModeStatistics gms : p.getStatistics() )
                {
                    if( gms.getRegion().equals( "agg" ) &&
                        gms.getSeason().equals( defaultSeason ) )
                    {
                        Long aggModeKills = null;
                        BigDecimal aggModeKdRatio = null;
                        Long aggModeGamesPlayed = null;
                        Long aggModeWins = null;
                        for( final Statistic stat : gms.getStatistics() )
                        {
                            if( stat.getLabel().equals( "Rating" ) )
                            {
                                if( gms.getMatch().equals( "solo" ) )
                                {
                                    displayPlayer
                                        .setSoloRating( stat.getValueDecimal() );
                                }
                                else if( gms.getMatch().equals( "duo" ) )
                                {
                                    displayPlayer
                                        .setDuoRating( stat.getValueDecimal() );
                                }
                                else if( gms.getMatch().equals( "squad" ) )
                                {
                                    displayPlayer
                                        .setSquadRating( stat.getValueDecimal() );
                                }
                            }
                            if( stat.getLabel().equals( "Kills" ) )
                            {
                                aggModeKills = stat.getValueInteger();
                            }
                            if( stat.getLabel().equals( "K/D Ratio" ) )
                            {
                                aggModeKdRatio = stat.getValueDecimal();
                            }
                            if( stat.getLabel().equals( "Rounds Played" ) )
                            {
                                aggModeGamesPlayed = stat.getValueInteger();
                            }
                            if( stat.getLabel().equals( "Wins" ) )
                            {
                                aggModeWins = stat.getValueInteger();
                            }
                            if( stat.getLabel().equals( "Top 10s" ) )
                            {
                                totalTop10s += stat.getValueInteger();
                            }
                        }
                        if( killDeathRatioCalculable &&
                            aggModeKills != null &&
                            aggModeKdRatio != null )
                        {
                            if( BigDecimal.ZERO.compareTo( aggModeKdRatio ) != 0 )
                            {
                                final long aggModeDeaths =
                                    BigDecimal.valueOf( aggModeKills )
                                        .divide( aggModeKdRatio,
                                                 0,
                                                 RoundingMode.HALF_UP )
                                        .longValue();
                                totalDeaths += aggModeDeaths;
                                totalKills += aggModeKills;
                            }
                            else
                            {
                                if( aggModeWins != null &&
                                    aggModeGamesPlayed != null )
                                {
                                    // This is not technically true
                                    // as it doesn't hold for
                                    // duo and squad games, but
                                    // it should be close enough for
                                    // players without a kill.
                                    final long aggModeDeaths =
                                        aggModeGamesPlayed - aggModeWins;
                                    totalDeaths += aggModeDeaths;
                                    totalKills += aggModeKills;
                                }
                                else
                                {
                                    killDeathRatioCalculable = false;
                                }
                            }
                        }

                        displayPlayer.setTotalGamesPlayed(
                            displayPlayer.getTotalGamesPlayed() +
                            aggModeGamesPlayed );
                    }
                    if( gms.getRegion().equals( "oc" ) &&
                        gms.getSeason().equals( defaultSeason ) )
                    {
                        for( final Statistic stat : gms.getStatistics() )
                        {
                            if( stat.getLabel().equals( "Rating" ) )
                            {
                                if( gms.getMatch().equals( "solo" ) )
                                {
                                    displayPlayer
                                        .setOceaniaSoloRank( stat.getRank() );
                                }
                                else if( gms.getMatch().equals( "duo" ) )
                                {
                                    displayPlayer
                                        .setOceaniaDuoRank( stat.getRank() );
                                }
                                else if( gms.getMatch().equals( "squad" ) )
                                {
                                    displayPlayer
                                        .setOceaniaSquadRank( stat.getRank() );
                                }
                            }
                        }
                    }
                }

                displayPlayer.setAggregateRating(
                    new BigDecimal( 3 )
                        .divide(
                            BigDecimal.ZERO
                                .add( BigDecimal.ONE.divide(
                                          displayPlayer.getSoloRating() == null ||
                                          displayPlayer.getSoloRating()
                                                  .compareTo( BigDecimal.ONE ) < 0 ?
                                              worstSoloRating :
                                              displayPlayer.getSoloRating(),
                                          16,
                                          RoundingMode.HALF_DOWN ) )
                                .add( BigDecimal.ONE.divide(
                                          displayPlayer.getDuoRating() == null ||
                                          displayPlayer.getDuoRating()
                                                  .compareTo( BigDecimal.ONE ) < 0 ?
                                              worstDuoRating :
                                              displayPlayer.getDuoRating(),
                                              16,
                                              RoundingMode.HALF_DOWN  ) )
                                .add( BigDecimal.ONE.divide(
                                          displayPlayer.getSquadRating() == null ||
                                          displayPlayer.getSquadRating()
                                                   .compareTo( BigDecimal.ONE ) < 0 ?
                                               worstSquadRating :
                                               displayPlayer.getSquadRating(),
                                           16,
                                           RoundingMode.HALF_DOWN ) ),
                            2,
                            RoundingMode.HALF_UP ) );
                if( killDeathRatioCalculable && totalDeaths != 0 )
                {
                    displayPlayer.setOverallKillDeathRatio(
                        BigDecimal.valueOf( totalKills )
                            .divide( BigDecimal.valueOf( totalDeaths ),
                                     2,
                                     RoundingMode.HALF_UP ) );
                }
                if( displayPlayer.getTotalGamesPlayed() != null &&
                    displayPlayer.getTotalGamesPlayed() > 0 )
                {
                    displayPlayer.setTop10Percentage(
                        BigDecimal.valueOf( totalTop10s )
                            .divide( BigDecimal.valueOf(
                                         displayPlayer.getTotalGamesPlayed() ),
                                     4,
                                     RoundingMode.HALF_UP ) );
                }
                return displayPlayer;
            } )
            .filter( Objects::nonNull )
            .sorted( ( p1, p2 ) -> compareIgnoreCase(
                                            p1.getPlayerName(),
                                            p2.getPlayerName() ) )
            .sorted( ( (Comparator<DisplayPlayer>)( p1, p2 ) ->
                            compare( p1.getAggregateRating(),
                                     p2.getAggregateRating() ) ).reversed() )
            .collect( toList() );
    }

    private Optional<String> findLatestSeason( final List<Player> players )
    {
        final SortedSet<String> seasons = new TreeSet<>();
        for( final Player player : players )
        {
            seasons.add( player.getDefaultSeason() );
        }
        return seasons.isEmpty() ? Optional.empty() : Optional.of( seasons.last() );
    }

    private enum Mode{ SOLO, DUO, SQUAD }

    private BigDecimal findWorstModeRating( final List<Player> players,
                                            final String currentSeason,
                                            final Mode mode )
    {
        final SortedSet<BigDecimal> ratings = new TreeSet<>();
        for( final Player player : players )
        {
            for( final GameModeStatistics gms : player.getStatistics() )
            {
                if( gms.getRegion().equals( "agg" ) &&
                    gms.getSeason().equals( currentSeason ) )
                {
                    for( final Statistic stat : gms.getStatistics() )
                    {
                        if( stat.getLabel().equals( "Rating" ) )
                        {
                            if( mode == Mode.SOLO && gms.getMatch().equals( "solo" ) ||
                                mode == Mode.DUO && gms.getMatch().equals( "duo" ) ||
                                mode == Mode.SQUAD && gms.getMatch().equals( "squad" ) )
                            {
                                ratings.add( stat.getValueDecimal() );
                            }
                        }
                    }
                }
            }
        }
        return ratings.isEmpty() || ratings.first().compareTo( BigDecimal.ONE ) < 0 ?
            BigDecimal.ONE :
            ratings.first();
    }

}
