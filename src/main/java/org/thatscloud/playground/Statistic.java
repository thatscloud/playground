package org.thatscloud.playground;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Statistic extends BaseJsonEntity
{
    private String myPartition;
    private String myLabel;
    private String mySubLabel;
    private String myField;
    private String myCategory;
    private Long myValueInteger;
    private BigDecimal myValueDecimal;
    private String myValue;
    private Long myRank;
    private BigDecimal myPercentile;
    private String myDisplayValue;

    public String getPartition()
    {
        return myPartition;
    }

    public void setPartition( final String partition )
    {
        myPartition = partition;
    }

    public String getLabel()
    {
        return myLabel;
    }

    public void setLabel( final String label )
    {
        myLabel = label;
    }

    public String getSubLabel()
    {
        return mySubLabel;
    }

    public void setSubLabel( final String subLabel )
    {
        mySubLabel = subLabel;
    }

    public String getField()
    {
        return myField;
    }

    public void setField( final String field )
    {
        myField = field;
    }

    public String getCategory()
    {
        return myCategory;
    }

    public void setCategory( final String category )
    {
        myCategory = category;
    }

    @JsonProperty( "ValueInt" )
    public Long getValueInteger()
    {
        return myValueInteger;
    }

    public void setValueInteger( final Long valueInteger )
    {
        myValueInteger = valueInteger;
    }

    @JsonProperty( "ValueDec" )
    public BigDecimal getValueDecimal()
    {
        return myValueDecimal;
    }

    public void setValueDecimal( final BigDecimal valueDecimal )
    {
        myValueDecimal = valueDecimal;
    }

    public String getValue()
    {
        return myValue;
    }

    public void setValue( final String value )
    {
        myValue = value;
    }

    public Long getRank()
    {
        return myRank;
    }

    public void setRank( final Long rank )
    {
        myRank = rank;
    }

    public BigDecimal getPercentile()
    {
        return myPercentile;
    }

    public void setPercentile( final BigDecimal percentile )
    {
        myPercentile = percentile;
    }

    public String getDisplayValue()
    {
        return myDisplayValue;
    }

    public void setDisplayValue( final String displayValue )
    {
        myDisplayValue = displayValue;
    }
}
