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
            overview: function( trnr, lvl, exp, st, mins, tb ){
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

                // If more than 2 eligible minions, let them battle
                if( minids.length >= 2 )
                {
                    $.lad.minion.battle( minids, trnr );
                }

                // If there is less than 8 minions allow the trainer to get
                // another
                if( mins.length < 8 )
                {
                    $("<button>Add Minion</button>").button().click(function(){
                        $.ladAjax( {
                            'addminion': '',
                            'trainer': trnr
                        });
                    }).appendTo( ctx() );
                }

                // Trainer battle state 1 == Can Battle
                if( tb === 1 )
                {
                    $("<button>Arena Battle</button>").button().click(
                      function(){
                        var weaponButtons = {};
                        $.each( $.lad.weapons.allStrings(), function(i,v){
                            weaponButtons[ v ] = function(){
                                $.ladAjax({
                                    'trainertoarena':trnr,
                                    'weapon':i
                                });
                                $(this).dialog('close').remove();
                            };
                        });
                        genericDialog( "Weapon Selection", "Select a weapon" +
                        "for your trainer to battle with.", weaponButtons );
                    }).appendTo( ctx() );
                }
                // Trainer battle state 2 == Can Leave Battle
                else if( tb === 2 )
                {
                    $("<button>Leave Arena</button>").button().click(function(){
                        $.ladAjax({
                            'trainerleavequeue':trnr
                        });
                    }).appendTo( ctx() );
                }

                // Return to main button
                $.lad.main.returnButton();
            }
        },
        weapons: {
            allStrings: function(){
                return [ "Generator", "Amplifier", "Launcher", "Bombarder",
                         "Lancer", "Pistol", "Propeller", "Projector",
                         "Catapulter" ];
            }
        },
        main: {
            returnButton: function(){
                $("<button>Return to Overview</button>").button().click(
                  function(){
                      $.ladAjax({ 'viewalltrainers': '' });
                }).appendTo( ctx() );
            }
        }
    });
}));