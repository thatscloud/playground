package org.thatscloud.playground;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.ComparatorUtils.reversedComparator;
import static org.apache.commons.lang3.ObjectUtils.compare;
import static org.apache.commons.lang3.StringUtils.compareIgnoreCase;
import static org.thatscloud.pubj.rest.model.constant.Mode.DUO;
import static org.thatscloud.pubj.rest.model.constant.Mode.SOLO;
import static org.thatscloud.pubj.rest.model.constant.Mode.SQUAD;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.MutableObject;
import org.thatscloud.playground.players.DisplayPlayer;
import org.thatscloud.pubj.rest.model.Player;
import org.thatscloud.pubj.rest.model.RegionSeasonModeStats;
import org.thatscloud.pubj.rest.model.RegionSeasonModeStatsKey;
import org.thatscloud.pubj.rest.model.Stat;
import org.thatscloud.pubj.rest.model.constant.Mode;
import org.thatscloud.pubj.rest.model.constant.Region;
import org.thatscloud.pubj.rest.model.constant.StatField;


public class PlayerToDisplayPlayerMapping implements Function<List<Player>, List<DisplayPlayer>>
{
    @Override
    public List<DisplayPlayer> apply( final List<Player> players )
    {
        final String currentSeason =
            findLatestSeason( players ).orElse( "2017-pre5" );
        final BigDecimal worstSoloRating =
            findWorstModeRating( players, currentSeason, SOLO );
        final BigDecimal worstDuoRating =
            findWorstModeRating( players, currentSeason, DUO );
        final BigDecimal worstSquadRating =
            findWorstModeRating( players, currentSeason, SQUAD );
        final List<DisplayPlayer> playerList = players.stream()
            .map( player ->
            {
                final DisplayPlayer displayPlayer = new DisplayPlayer();
                displayPlayer.setPlayerName( player.getNickname() );
                displayPlayer.setTotalGamesPlayed( 0L );
                displayPlayer.setAvatarUrl( player.getAvatar() );

                displayPlayer.setSoloRating(
                    getGameModeRating( player, SOLO, currentSeason ) );
                displayPlayer.setDuoRating(
                    getGameModeRating( player, DUO, currentSeason ) );
                displayPlayer.setSquadRating(
                    getGameModeRating( player, SQUAD, currentSeason ) );
                displayPlayer.setOceaniaSoloRank(
                    getGameModeRank( player, SOLO, currentSeason ) );
                displayPlayer.setOceaniaDuoRank(
                    getGameModeRank( player, DUO, currentSeason ) );
                displayPlayer.setOceaniaSquadRank(
                    getGameModeRank( player, SQUAD, currentSeason ) );

                final long totalTop10s =
                    getIntegerStatisticTotal( player, StatField.TOP_10S, currentSeason );
                final long totalKills =
                    getIntegerStatisticTotal( player, StatField.KILLS, currentSeason );
                final Long totalDeaths =
                    calculateTotalDeaths( player, currentSeason );
                displayPlayer.setTotalGamesPlayed(
                    getIntegerStatisticTotal( player,
                                              StatField.ROUNDS_PLAYED,
                                              currentSeason ) );

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

    private BigDecimal findWorstModeRating( final List<Player> players,
                                            final String currentSeason,
                                            final Mode gameMode )
    {
        return players.stream()
            .map( Player::getStats )
            .map( rsms -> rsms.get( new RegionSeasonModeStatsKey( Region.AGGREGATE,
                                                                  currentSeason,
                                                                  gameMode ) ) )
            .filter( Objects::nonNull )
            .map( RegionSeasonModeStats::getStats )
            .map( stats -> stats.get( StatField.RATING ) )
            .filter( Objects::nonNull )
            .map( Stat::getValueDec )
            .sorted()
            .filter( value -> value.compareTo( BigDecimal.ZERO ) != 0 )
            .findFirst()
            .orElse( BigDecimal.ONE );
    }

    private Long calculateTotalDeaths( final Player player, final String currentSeason )
    {
        return Arrays.stream( Mode.values() )
            .map( mode ->
                Optional.of( player )
                    .map( Player::getStats )
                    .map( rsms -> rsms.get( new RegionSeasonModeStatsKey( Region.AGGREGATE,
                                                                          currentSeason,
                                                                          mode ) ) )
                    .orElse( null ) )
            .filter( Objects::nonNull )
            .map( RegionSeasonModeStats::getStats )
            .map( stats ->
            {
                final Long kills =
                    Optional.of( stats )
                        .map( s -> s.get( StatField.KILLS ) )
                        .map( Stat::getValueInt )
                        .orElse( null );
                final BigDecimal killDeathRatio =
                    Optional.of( stats )
                        .map( s -> s.get( StatField.KILL_DEATH_RATIO ) )
                        .map( Stat::getValueDec )
                        .orElse( null );
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

                final Long wins =
                    Optional.of( stats )
                        .map( s -> s.get( StatField.WINS ) )
                        .map( Stat::getValueInt )
                        .orElse( null );
                final Long roundsPlayed =
                    Optional.of( stats )
                        .map( s -> s.get( StatField.ROUNDS_PLAYED ) )
                        .map( Stat::getValueInt )
                        .orElse( null );
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
                                          final Mode mode,
                                          final String currentSeason )
    {
        return Optional.of( player )
            .map( Player::getStats )
            .map( rsms -> rsms.get( new RegionSeasonModeStatsKey( Region.AGGREGATE,
                                                                  currentSeason,
                                                                  mode ) ) )
            .map( RegionSeasonModeStats::getStats )
            .map( stats -> stats.get( StatField.RATING ) )
            .filter( Objects::nonNull )
            .map( Stat::getValueDec )
            .filter( Objects::nonNull )
            .orElse( null );
    }

    private Long getGameModeRank( final Player player,
                                  final Mode mode,
                                  final String defaultSeason )
    {
        return Optional.of( player )
            .map( Player::getStats )
            .map( rsms -> rsms.get( new RegionSeasonModeStatsKey( Region.OCEANIA,
                                                                  defaultSeason,
                                                                  mode ) ) )
            .map( RegionSeasonModeStats::getStats )
            .map( stats -> stats.get( StatField.RATING ) )
            .filter( Objects::nonNull )
            .map( Stat::getRank )
            .filter( Objects::nonNull )
            .orElse( null );
    }

    private Long getIntegerStatisticTotal( final Player player,
                                           final StatField statisticField,
                                           final String currentSeason )
    {
        return Arrays.stream( Mode.values() )
            .map( mode ->
                Optional.of( player )
                .map( Player::getStats )
                .map( rsms -> rsms.get( new RegionSeasonModeStatsKey( Region.AGGREGATE,
                                                                      currentSeason,
                                                                      mode ) ) )
                .orElse( null ) )
            .filter( Objects::nonNull )
            .map( RegionSeasonModeStats::getStats )
            .map( stats -> stats.get( statisticField ) )
            .filter( Objects::nonNull )
            .map( Stat::getValueInt )
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

    private Optional<String> findLatestSeason( final List<Player> players )
    {
        final SortedSet<String> seasons = new TreeSet<>();
        players.stream()
            .flatMap( p -> p.getStats().values().stream() )
            .map( rsms -> rsms.getSeason() )
            .forEach( seasons::add );
        return seasons.isEmpty() ? Optional.empty() : Optional.of( seasons.last() );
    }


}
