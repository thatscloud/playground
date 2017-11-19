package org.thatscloud.playground.players;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

public class PlayersContainer
{
    public static final Object theDataLock = new Object();
    public static final List<DisplayPlayer> theDisplayPlayers = new ArrayList<>();
    public static final Mutable<Instant> theLastUpdateInstant = new MutableObject<>();
}
