/*global window: false, jQuery: false, console: false, define: false,
doAjax: false, getPopupContext: false, makeSortableTable: false,
genericDialog: false */

// TODO: Migrate external functions inside
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
                // TODO: Smarter selects
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

                ctx().html( "" ).append( "Trainer #" + trnr + "<br>" +
                    "Level: " + lvl + "<br>Exp: " + exp + "<br>" +
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
                          // TODO: Better Dialog
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
                //# WEAPON STRINGS
                return [ "Bombarder", "..." ];
                //# END WEAPON STRINGS
            }
        },
        main: {
            returnButton: function(){
                $("<button>Return to Overview</button>").button().click(
                  function(){
                      $.ladAjax({ 'viewalltrainers': '' });
                }).appendTo( ctx() );
            },
            overview: function( trnrs ){
                // Output each
                ctx().html( "" );
                $.each( trnrs, function(i,v){
                    var num = i + 1;
                    ctx().append( "Trainer " + num + ": Level " + v[ 1 ] +
                                  " Exp:" + v[ 2 ] );
                    $("<button>View</button>").button().click(function(){
                        $.ladAjax({'viewtrainer': v[ 0 ] });
                    }).appendTo( ctx() );
                    ctx().append( "<br>" );
                });

                // "Add trainer" button if less than 8
                if( trnrs.length < 8 )
                {
                    $("<button>Add Trainer</button>").button().click(function(){
                        $.ladAjax({'addtrainer':''});
                    }).appendTo( ctx() );
                }

                // Add the modifiers button
                ctx().append( "<br><br>" );
                $("<button>Modifiers</button>").button().click(function(){
                    $.ladAjax({'viewmodifiers':''});
                }).appendTo( ctx() );

                // Add the User EXP button
                $("<button>User EXP</button>").button().click(function(){
                    $.ladAjax({'viewuserexp': ''});
                }).appendTo( ctx() );
            }
        },
        userexp: {
            overview: function( exps ){
                var headers = {
                    Type: "true",
                    Target: "true",
                    Level: "true",
                    Exp: "true"
                };

                ctx().html( "" )
                    .append( makeSortableTable( headers, exps, 'userexp' ) );
                $.lad.main.returnButton();
            }
        },
        modifiers: {
            overview: function( mods ){
                var headers = {
                    Type: "true",
                    Battles: "true",
                    Action: ""
                };

                ctx().html( "" )
                    .append( makeSortableTable( headers, mods, 'mods' ) );
                $.lad.main.returnButton();
            }
        }
    });
}));