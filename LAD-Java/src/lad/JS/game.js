/*global window: false, jQuery: false, console: false, define: false, doAjax: false, getPopupContext: false */
(function(factory) {
	if(typeof define === 'function' && define.amd) {
		define(['jquery'], factory);
	}
	else {
		factory(jQuery);
	}
}

(function($) {
    function ctx(){
        return getPopupContext( 'LAD' );
    }
    //var ctx = getPopupContext( 'LAD' );
    $.ladAjax = function( params )
    {
        return doAjax( 'java_run', params );
    };
    $.lad = function()
    {
        return ctx();
    };
    $.extend( $.lad, {
        minion: {
            add: function( index, lvl, exp, trnr, id )
            {
                ctx().append( "Minion #" + index + " Level: " + lvl +
                              " Exp: " + exp );
                $("<button>Train</button>").button().click(function(){
                    $.lad.minion.doTrain( id, trnr );
                }).appendTo( ctx() );
                ctx().append( '<br>' );
            },
            doTrain: function( id, trnr )
            {
                $.ladAjax( { 'trainminion': id, 'trainer': trnr } );
            },
            battle: function( opt, trnr )
            {
                function createOptions()
                {
                    var select = $("<select></select>");
                    $.each( opt, function(i,v){
                        select.append( "<option value=" + v + ">" + ( i + 1 ) +
                                       "</option>" );
                    });
                    return select;
                }
                ctx().append( "<br><br>Battle: " )
                     .append( createOptions().attr( 'id', 'minion1' ) )
                     .append( " with " )
                     .append( createOptions().attr( 'id', 'minion2' ) );
                $('<button>Battle</button>').button().click(function(){
                    $.ladAjax( { 'battleminion': trnr,
                                 'minion1': $('#minion1').val(),
                                 'minion2': $('#minion2').val()
                    });
                }).appendTo( ctx() );
            }
        },
        trainer: {
            overview: function( trnr, lvl, exp, st, mins ){
                var minids = [];
                ctx().append( "Trainer #" + trnr + "<br>" +
                              "Level: " + lvl + "<br>" +
                              "Exp: " + exp + "<br>" +
                              "Battle State: " + st + "<br>" );
                $.each( mins, function(i,v){
                    $.lad.minion.add( ( i + 1 ), v[ 1 ], v[ 2 ], trnr, v[ 0 ] );
                    if( v[ 1 ] > 0 )
                    {
                        minids.push( v[ 0 ] );
                    }
                });
                if( minids.length >= 2 )
                {
                    $.lad.minion.battle( minids, trnr );
                }
            }
        }
    });
}));