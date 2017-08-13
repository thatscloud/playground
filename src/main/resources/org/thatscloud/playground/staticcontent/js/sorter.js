$( document ).ready( function()
{ 
    
    $.tablesorter.addParser(
    {
        id: "playground-ordinal",
        is: function( s, table, cell, $cell ) { return false; },
        format: function( s, table, cell, cellIndex ) 
        {
            if( !/\d/g.test( s ) )
            {
                return s;
            }
            
            var commaSeparatedNumber = s.replace( /((?:\d+,)*(?:\d+))(?:st|nd|rd|th)/, "$1" );
            return -( commaSeparatedNumber.replace( /,/, "" ) );
        },
        parsed: false,
        type: 'numeric'
    } );
    
    $( "#resultsTable" ).tablesorter(
    {
        sortInitialOrder: "desc",
        sortReset: "true",
        stringTo: "bottom"
    } );
} );