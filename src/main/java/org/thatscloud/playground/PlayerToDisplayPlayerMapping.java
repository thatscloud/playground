package org.thatscloud.playground;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.ComparatorUtils.reversedComparator;
import static org.apache.commons.lang3.ObjectUtils.compare;
import static org.apache.commons.lang3.StringUtils.compareIgnoreCase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.MutableObject;
import org.thatscloud.playground.players.DisplayPlayer;
import org.thatscloud.playground.rest.model.GameModeStatistics;
import org.thatscloud.playground.rest.model.GameModeStatisticsKey;
import org.thatscloud.playground.rest.model.Player;
import org.thatscloud.playground.rest.model.Statistic;
import org.thatscloud.playground.rest.model.constant.GameMode;
import org.thatscloud.playground.rest.model.constant.Region;
import org.thatscloud.playground.rest.model.constant.StatisticField;


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
        final BigDecimal worstSoloRating =
            findWorstModeRating( players, latestSeason, GameMode.SOLO );
        final BigDecimal worstDuoRating =
            findWorstModeRating( players, latestSeason, GameMode.DUO );
        final BigDecimal worstSquadRating =
            findWorstModeRating( players, latestSeason, GameMode.SQUAD );
        final List<DisplayPlayer> playerList = players.stream()
            .map( player ->
            {
                final DisplayPlayer displayPlayer = new DisplayPlayer();
                displayPlayer.setPlayerName( player.getPlayerName() );
                displayPlayer.setTotalGamesPlayed( 0L );
                displayPlayer.setAvatarUrl( player.getAvatarUrl() );
                final String defaultSeason = player.getDefaultSeason();
                if( !Objects.equals( player.getDefaultSeason(), latestSeason ) )
                {
                    return displayPlayer;
                }

                displayPlayer.setSoloRating(
                    getGameModeRating( player, GameMode.SOLO, defaultSeason ) );
                displayPlayer.setDuoRating(
                    getGameModeRating( player, GameMode.DUO, defaultSeason ) );
                displayPlayer.setSquadRating(
                    getGameModeRating( player, GameMode.SQUAD, defaultSeason ) );
                displayPlayer.setOceaniaSoloRank(
                    getGameModeRank( player, GameMode.SOLO, defaultSeason ) );
                displayPlayer.setOceaniaDuoRank(
                    getGameModeRank( player, GameMode.DUO, defaultSeason ) );
                displayPlayer.setOceaniaSquadRank(
                    getGameModeRank( player, GameMode.SQUAD, defaultSeason ) );

                final long totalTop10s =
                    getIntegerStatisticTotal( player, StatisticField.TOP_10S, defaultSeason );
                final long totalKills =
                    getIntegerStatisticTotal( player, StatisticField.KILLS, defaultSeason );
                final Long totalDeaths =
                    calculateTotalDeaths( player, defaultSeason );
                displayPlayer.setTotalGamesPlayed(
                    getIntegerStatisticTotal( player,
                                              StatisticField.ROUNDS_PLAYED,
                                              defaultSeason ) );

                displayPlayer.setAggregateRating( calculateAggregateRating( displayPlayer,
                                                                            worstSoloRating,
                                                                            worstDuoRating,
                                                                            worstSquadRating ) );
                if( totalDeaths != null && totalDeaths != 0L )
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
            .sorted( ( p1, p2 ) -> compareIgnoreCase( p1.getPlayerName(),
                                                      p2.getPlayerName() ) )
            .sorted( reversedComparator( ( p1, p2 ) -> compare( p1.getAggregateRating(),
                                                                p2.getAggregateRating() ) ) )
            .collect( toList() );

        int rank = 1;
        for ( final DisplayPlayer player : playerList)
        {
            player.setPlayerRank( rank++ );
        }
        return playerList;
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

    private BigDecimal findWorstModeRating( final List<Player> players,
                                            final String currentSeason,
                                            final GameMode gameMode )
    {
        return players.stream()
            .map( Player::getStatistics )
            .map( gms -> gms.get( new GameModeStatisticsKey( Region.AGGREGATE,
                                                             currentSeason,
                                                             gameMode ) ) )
            .filter( Objects::nonNull )
            .map( GameModeStatistics::getStatistics )
            .map( stats -> stats.get( StatisticField.RATING ) )
            .filter( Objects::nonNull )
            .map( Statistic::getValueDecimal )
            .sorted()
            .filter( value -> value.compareTo( BigDecimal.ZERO ) != 0 )
            .findFirst()
            .orElse( BigDecimal.ONE );
    }

    private Long calculateTotalDeaths( final Player player, final String defaultSeason )
    {
        return Arrays.stream( GameMode.values() )
            .map( gm ->
                Optional.of( player )
                    .map( Player::getStatistics )
                    .map( gms -> gms.get( new GameModeStatisticsKey( Region.AGGREGATE,
                                                                     defaultSeason,
                                                                     gm ) ) )
                    .orElse( null ) )
            .filter( Objects::nonNull )
            .map( GameModeStatistics::getStatistics )
            .map( stats ->
            {
                final Long kills = stats.get( StatisticField.KILLS ).getValueInteger();
                final BigDecimal killDeathRatio =
                    stats.get( StatisticField.KILL_DEATH_RATIO ).getValueDecimal();
                if( kills != null &&
                    killDeathRatio != null &&
                    BigDecimal.ZERO.compareTo( killDeathRatio ) != 0 )
                {
                    return BigDecimal.valueOf( kills )
                        .divide( killDeathRatio,
                                 0,
                                 RoundingMode.HALF_UP )
                        .longValue();
                }

                final Long wins = stats.get( StatisticField.WINS ).getValueInteger();
                final Long roundsPlayed =
                    stats.get( StatisticField.ROUNDS_PLAYED ).getValueInteger();
                if( wins != null && roundsPlayed != null )
                {
                    // This is not technically true
                    // as it doesn't hold for
                    // duo and squad games, but
                    // it should be close enough for
                    // players without a kill.
                    return roundsPlayed - wins;
                }
                return null;
            } )
            .collect( () -> new MutableObject<Long>(),
                      ( acc, l ) ->
                      {
                          if( l == null )
                          {
                              acc.setValue( null );
                          }
                          else if( acc.getValue() != null )
                          {
                              acc.setValue( acc.getValue() + l );
                          }
                      },
                      ( accR, accI ) ->
                      {
                          if( accR.getValue() != null )
                          {
                              if( accI.getValue() == null )
                              {
                                  accR.setValue( null );
                              }
                              else
                              {
                                  accR.setValue( accR.getValue() + accI.getValue() );
                              }
                          }
                      } )
            .getValue();
    }

    private BigDecimal getGameModeRating( final Player player,
                                          final GameMode gameMode,
                                          final String defaultSeason )
    {
        return Optional.of( player )
            .map( Player::getStatistics )
            .map( gms -> gms.get( new GameModeStatisticsKey( Region.AGGREGATE,
                                                             defaultSeason,
                                                             gameMode ) ) )
            .map( GameModeStatistics::getStatistics )
            .map( stats -> stats.get( StatisticField.RATING ) )
            .map( Statistic::getValueDecimal )
            .orElse( null );
    }

    private Long getGameModeRank( final Player player,
                                  final GameMode gameMode,
                                  final String defaultSeason )
    {
        return Optional.of( player )
            .map( Player::getStatistics )
            .map( gms -> gms.get( new GameModeStatisticsKey( Region.OCEANIA,
                                                             defaultSeason,
                                                             gameMode ) ) )
            .map( GameModeStatistics::getStatistics )
            .map( stats -> stats.get( StatisticField.RATING ) )
            .map( Statistic::getRank )
            .orElse( null );
    }

    private Long getIntegerStatisticTotal( final Player player,
                                           final StatisticField statisticField,
                                           final String defaultSeason )
    {
        return Arrays.stream( GameMode.values() )
            .map( gm ->
                Optional.of( player )
                .map( Player::getStatistics )
                .map( gms -> gms.get( new GameModeStatisticsKey( Region.AGGREGATE,
                                                                 defaultSeason,
                                                                 gm ) ) )
                .orElse( null ) )
            .filter( Objects::nonNull )
            .map( GameModeStatistics::getStatistics )
            .map( stats -> stats.get( statisticField ) )
            .map( Statistic::getValueInteger )
            .filter( Objects::nonNull )
            .collect( Collectors.summingLong( Long::longValue ) );
    }

    private BigDecimal calculateAggregateRating( final DisplayPlayer displayPlayer,
                                                 final BigDecimal worstSoloRating,
                                                 final BigDecimal worstDuoRating,
                                                 final BigDecimal worstSquadRating )
    {
        return new BigDecimal( 3 )
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
            RoundingMode.HALF_UP );
    }
}
