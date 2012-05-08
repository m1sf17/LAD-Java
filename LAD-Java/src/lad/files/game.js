/*global jQuery: false, define: false, doAjax: false,
  makeSortableTable: false */

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
        return $("#ladcontent");
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
        window: function(){
            function create(){
                var ladbkgd,
                    ladheader = $("<div id='ladheader'></div>"),
                    closeLADButton = $("<button id='closelad'></button>");
                ladbkgd = $("<div id='ladbkgd'></div>").appendTo( $("body") );

                // Close Button
                closeLADButton.button({
                    icons: {primary: "ui-icon-closethick"},
                    text: false
                }).attr( "title", "Leave LAD" ).click(function(){
                    ladbkgd.fadeOut( "slow" );
                });

                // Add header/body
                ladheader.append( closeLADButton );
                ladbkgd.append( ladheader );
                ladbkgd.append( $("<div id='ladcontent'></div>") );
            }
            
            // Find/fill the background
            var ladbkgd = $("#ladbkgd");
            if( ladbkgd.length === 0 )
            {
                create();
                ladbkgd = $("#ladbkgd");
            }

            // Fade in and run login
            ladbkgd.hide();
            ladbkgd.fadeIn( "slow" );
            $.ladAjax({ 'login': '' });
        },
        minion: {
            oldBattleValue: -1,
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
                    var select = $("<select></select>").change(function(){
                        if( $("#minion1").val() === $("#minion2").val() )
                        {
                            $(this).val( $.lad.minion.oldBattleValue );
                        }
                        else
                        {
                            $.lad.minion.oldBattleValue = $(this).val();
                        }
                        // TODO: Pretty warning about same
                    }).focus(function(){
                        $.lad.minion.oldBattleValue = $(this).val();
                    });
                    // Add each option
                    $.each( opt, function(i,v){
                        select.append( "<option value=" + v + ">" + ( i + 1 ) +
                                       "</option>" );
                    });
                    // Convenience: set readonly if only 2 values
                    if( opt.length === 2 )
                    {
                        select.attr({
                            "readonly": "readonly",
                            "disabled": "disabled"
                        });
                    }
                    return select;
                }
                ctx().append( "<br><br>Battle: " )
                     .append( createOptions().attr( 'id', 'minion1' ) )
                     .append( " with " )
                     .append( createOptions().attr( 'id', 'minion2' )
                         .val( opt[ 1 ] ) );
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
                    $("<button>Arena Battle</button>").button()
                    .click(function(){
                        $.lad.trainer.arenabattledialog( trnr );
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
            },
            arenabattledialog: function(trnr){
                function createRow( hand ){
                    return $("<div></div>").addClass( "row" ).append(
                        $("<div></div>").append( hand ).addClass( "rowHeader")
                    );
                }
                var weaponButtons = {},
                    weaponData = $.lad.weapons.all(),
                    twoRow = createRow( "Two-Handed" ),
                    mainRow = createRow( "Main Hand" ),
                    offRow = createRow( "Off Hand" ),
                    eitherRow = createRow( "Either Hand" ),
                    topContainer = $("<p></p>"),
                    rowContainer = $("<div></div>"),
              //      rightContainer = $("<div></div>"),
                    dialog = $("<div></div>"),
                    okButton = $("<button>Ok</button>"),
                    cancelButton = $("<button>Cancel</button>");
                $.each( weaponData, function(i,v){
                    var div = $("<div></div>").append( v.name )
                        .addClass( "weapon" ).mouseover(function(){
                            $(this).toggleClass( "weapon-hover" );
                        }).mouseout(function(){
                            $(this).toggleClass( "weapon-hover" );
                        }).click(function(){
                            $.each( weaponButtons, function(w,b){
                                b.removeClass( "weapon-selected" );
                            });
                            $(this).addClass( "weapon-selected" );
                            okButton.button( "enable" );
                            okButton.data( "selectedWeapon", i + 1 );
                        });
                    switch( v.type )
                    {
                        case 1:
                            offRow.append( div );
                            break;
                        case 2:
                            twoRow.append( div );
                            break;
                        case 3:
                            eitherRow.append( div );
                            break;
                        case 4:
                            mainRow.append( div );
                            break;
                    }
                    weaponButtons[ i ] = div;
                });

                // Setup top container
                topContainer.addClass( "weaponSelector" )
                    .append( rowContainer );

                // Setup Buttons
                cancelButton.button().click(function(){
                    dialog.dialog( "close" ).remove();
                }).appendTo( topContainer );
                okButton.button().click(function(){
                    $.ladAjax({
                        'trainertoarena': trnr,
                        'weapon': okButton.data( "selectedWeapon" )
                    });
                    dialog.dialog( "close" ).remove();
                }).button( "disable" ).appendTo( topContainer );

                // Setup row container
                rowContainer.addClass( "rows" )
                    .append( twoRow ).append( mainRow )
                    .append( offRow ).append( eitherRow );

                dialog.attr({
                    "title": "Weapon Selection",
                    "id": "dialog-weapon"
                }).append( topContainer ).appendTo( $("body") )
                .dialog({
                    resizable: false,
                    height: 400,
                    width: 525,
                    modal: true
                });
            }
        },
        weapons: {
            allStrings: function(){
                //# WEAPON STRINGS
                return [ "Bombarder", "..." ];
                //# END WEAPON STRINGS
            },
            all: function(){
                //# WEAPON OBJECTS
                return [ this.weapon( "Bombarder", 2 ) ];
                //# END WEAPON OBJECTS
            },
            weapon: function( name, type ){
                // Type: 1 = Off, 2 = Two, 3 = Either, 4 = Main
                return {
                    'name': name,
                    'type': type
                };
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