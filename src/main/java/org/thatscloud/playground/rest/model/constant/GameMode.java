package org.thatscloud.playground.rest.model.constant;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum GameMode
{
    @JsonProperty( "solo" )
    SOLO,
    @JsonProperty( "duo" )
    DUO,
    @JsonProperty( "squad" )
    SQUAD,
    @JsonProperty( "solo-fpp" )
    SOLO_FIRST_PERSON,
    @JsonProperty( "duo-fpp" )
    DUO_FIRST_PERSON,
    @JsonProperty( "squad-fpp" )
    SQUAD_FIRST_PERSON
}
