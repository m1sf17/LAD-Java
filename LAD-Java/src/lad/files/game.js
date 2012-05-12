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
        $.lad.tutorial( "" );
        return doAjax( 'java_run', params );
    };
    $.lad = {
        window: function(){
            function create(){
                var ladbkgd,
                    ladheader = $("<div id='ladheader'></div>"),
                    closeLADButton = $("<button id='closelad'></button>"),
                    notifier = $("<div id='notifier'></div>");
                ladbkgd = $("<div id='ladbkgd'></div>").appendTo( $("body") );

                // Close Button
                closeLADButton.button({
                    icons: {primary: "ui-icon-closethick"},
                    text: false
                }).attr( "title", "Leave LAD" ).click(function(){
                    ladbkgd.fadeOut( "slow" );
                    $("#loginform button").button( "enable" )
                        .button( "refresh" );
                    $("#loginform button, #loginform input")
                        .attr({
                            "disabled": false,
                            "readonly": false
                        }).not( "#username" ).val( "" ).keyup();
                    $("#password").focus();
                    $("#tutorial").remove();
                });

                // Add header/body
                ladheader.append( closeLADButton );
                ladbkgd.append( ladheader );
                ladbkgd.append( $("<div id='ladcontent'></div>") );

                // Add notifier
                ladbkgd.append( notifier );
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
            
        },
        minion: {
            oldBattleValue: -1,
            add: function( index, lvl, exp, trnr, id )
            {
                var div = $("<div></div>"), list = $(".minionList");
                list.append( div );
                div.append( "Minion #" + index + " Level: " + lvl + " Exp: " +
                            exp ).addClass( "minionItem ui-corner-all");
                $("<button>Train</button>").button().click(function(){
                    $.lad.minion.doTrain( id, trnr );
                }).appendTo( div );
                list.append( '<br>' );
            },
            doTrain: function( id, trnr )
            {
                $.ladAjax( {'trainminion': id, 'trainer': trnr} );
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
                        $.lad.blockedAction( "Cannot battle a minion with " +
                                             "itself." );
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

                $("<div></div>").append( "Battle: " )
                    .append( createOptions().attr( 'id', 'minion1' ) )
                    .append( " with " )
                    .append( createOptions().attr( 'id', 'minion2' )
                        .val( opt[ 1 ] ) )
                    .append( "  " )
                    .append( $('<button>Battle</button>').button().click(
                    function(){
                        $.ladAjax( {'battleminion': trnr,
                                     'minion1': $('#minion1').val(),
                                     'minion2': $('#minion2').val()
                        });
                    })).attr( "id", "minionBattleOptions" ).appendTo( ctx() );
                ctx().append( "<br>" );
            }
        },
        trainer: {
            overview: function( trnr, lvl, exp, st, mins, tb, hasBS ){
                var minids = [], trainerInfo = $("<div></div>"),
                    minionList = $("<div></div>");

                ctx().html( "" ).append( trainerInfo ).append( "<br>" )
                    .append( minionList );

                trainerInfo.append( "Trainer #" + trnr + "<br>" +
                    "Level: " + lvl + "<br>Exp: " + exp + "<br>" +
                    "Battle State: " + st + "  " ).attr( "id", "trnrOverview");
                $.lad.tutorial( "This is the main view for your trainer.  " +
                    "All of your trainer's stats are listed as well as the " +
                    "minions that belong to your trainer.", $("#trnrOverview"),
                    "Here are all of the stats for your trainer." );

                // Trainer battle state 1 == Can Battle
                if( tb === 1 )
                {
                    $("<button>Arena Battle</button>").button()
                    .click(function(){
                        $.lad.trainer.arenabattledialog( trnr );
                    }).css( "margin-right", "10px" ).appendTo( trainerInfo )
                        .attr( "id", "arenaBattleBtn" );
                }
                // Trainer battle state 2 == Can Leave Battle
                else if( tb === 2 )
                {
                    $("<button>Leave Arena</button>").button().click(function(){
                        $.ladAjax({
                            'trainerleavequeue':trnr
                        });
                    }).css( "margin-right", "10px" ).appendTo( trainerInfo )
                        .attr( "id", "arenaBattleBtn" );
                }

                minionList.addClass( "minionList" );
                $.lad.tutorial( "Minions are the method to increase a " +
                    "trainer's experience and to acquire new modifiers.  " +
                    "Minions advance in levels by simply clicking on their " +
                    "train button.  Minions may only battle when they reach " +
                    "level 1.", $(".minionList"), "Here are all of the " +
                    "minions belonging to this trainer." );
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
                    $.lad.tutorial( "Once two minions reach level one, they " +
                        "may battle each other.  However, the higher level " +
                        "the minions are and the higher level the trainer is " +
                        "plays a large role in the rarity of the modifier " +
                        "you will receive.  The losing minion is killed " +
                        "and the winning minion will receive 20% of the " +
                        "losing minion's experience.",
                        $("#minionBattleOptions"), "Here you may select " +
                        "which minions to have battle each other." );
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
                    }).css( "margin-right", "10px" ).appendTo( ctx() )
                        .attr( "id", "addMinionBtn" );
                    $.lad.tutorial( "You may also add a minion but may only " +
                        "have up to 8 minions per trainer total.",
                        $("#addMinionBtn"), "Click here to add a minion." );
                }

                if( tb === 1 )
                {
                    $.lad.tutorial( "Arena battles make up the core of life " +
                        "and death.  They consist of your trainer being " +
                        "sent into a duel (non-fatal) with another trainer.  " +
                        "You can select the weapon your trainer takes but " +
                        "nothing else.  Winning arena battles will grant " +
                        "you weapon experience", $("#arenaBattleBtn"),
                        "Click here to send your trainer into the arena." );
                }
                else if( tb === 2 )
                {
                    $.lad.tutorial( "So long as your trainer is in the " +
                        "interim period they may be recalled and will stop " +
                        "fighting in the arena.", $("#arenaBattleBtn"),
                        "Click here to recall your trainer from the arena." );
                }

                // If the trainer has battle stats, and the button to view them
                if( hasBS )
                {
                    $("<button>Trainer Battle Stats</button>").click(function(){
                        $.ladAjax({'viewtrainerstats': trnr});
                    }).button().css( "margin-right", "10px" ).appendTo( ctx() )
                        .attr( "id", "trainerBSBtn" );
                    $.lad.tutorial( "You may also view the arena battle " +
                        "statistics for this trainer here.", $("#trainerBSBtn"),
                        "Click here to view this trainer's battle " +
                        "statistics." );
                }

                // Return to main button
                $.lad.main.returnButton().appendTo( ctx() );
            },
            arenabattledialog: function(trnr){
                function createRow( hand ){
                    return $("<div></div>").addClass( "row" ).append(
                        $("<div></div>").append( hand )
                        .addClass( "rowHeader ui-corner-tl ui-corner-tr" )
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
                    dialog = $("<div></div>"),
                    okButton = $("<button>Ok</button>"),
                    cancelButton = $("<button>Cancel</button>");
                $.each( weaponData, function(i,v){
                    var div = $("<div></div>").append( $( "<div></div>" )
                            .append( v.name ).addClass( "weapon-name" ) )
                        .addClass( "weapon ui-corner-all" )
                        .mouseover(function(){
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
                        }),
                        liner = $("<div></div>").addClass( "weapon-liner" );

                    $.each( v, function(wi,wv){
                        if( wi !== 'name' && wi !== 'type' )
                        {
                            var att = wi.replace( /_/, ' ' ),
                                attdiv = $("<div></div>");
                            attdiv.append( att + ": " + wv + "&nbsp;&nbsp;" )
                                .addClass( "attribute").appendTo( div );
                        }
                    });

                    liner.append( div );

                    switch( v.type )
                    {
                        case 1:
                            offRow.append( liner );
                            break;
                        case 2:
                            twoRow.append( liner );
                            break;
                        case 3:
                            eitherRow.append( liner );
                            break;
                        case 4:
                            mainRow.append( liner );
                            break;
                    }
                    weaponButtons[ i ] = div;
                });

                // Setup top container
                topContainer.addClass( "weaponSelector" )
                    .append( rowContainer );

                // Setup Buttons
                cancelButton.button().click(function(){
                    dialog.remove();
                }).appendTo( topContainer );
                okButton.button().click(function(){
                    $.ladAjax({
                        'trainertoarena': trnr,
                        'weapon': okButton.data( "selectedWeapon" )
                    });
                    dialog.remove();
                }).button( "disable" ).appendTo( topContainer );

                // Setup row container
                rowContainer.addClass( "rows" ).append( mainRow )
                    .append( offRow ).append( eitherRow ).append( twoRow );

                dialog.attr({
                    "title": "Weapon Selection",
                    "id": "dialog-weapon"
                }).append( topContainer ).appendTo( $("body") )
                .dialog({
                    width: '500px',
                    resizable: false,
                    modal: true,
                    close: function(){ $(this).remove(); }
                });
            },
            createbutton: function( id, txt ) {
                return $("<button></button>").button({
                    "label": txt
                }).click(function(){
                    $.ladAjax({'viewtrainer': id});
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
                return [ this.weapon( "Bombarder", '...' ) ];
                //# END WEAPON OBJECTS
            },
            weapon: function( name, type, atkSpd, dmg, reload, accy, moby,
                              flex, shield, aim, range ){
                // Type: 1 = Off, 2 = Two, 3 = Either, 4 = Main
                return {
                    'name': name,
                    'type': type,
                    'Attack_Speed': atkSpd,
                    'Damage': dmg,
                    'Reload_Rate': reload,
                    'Accuracy': accy,
                    'Mobility': moby,
                    'Flexibility': flex,
                    'Shielding': shield,
                    'Aim': aim,
                    'Range': range
                };
            }
        },
        main: {
            returnButton: function(){
                return $("<button>Return to Overview</button>").button()
                    .click(function(){
                        $.ladAjax({'viewalltrainers': ''});
                }).button().css( "margin-right", "10px" );
            },
            overview: function( trnrs, hasBS ){
                // Output each
                var trainerList = $("<div></div>").addClass( "trainerList" );
                ctx().html( "" );
                $.each( trnrs, function(i,v){
                    var num = i + 1,
                        trnrDiv = $("<div></div>");
                    trnrDiv.append( "Trainer " + num + ": Level " + v[ 1 ] +
                                    " Exp:" + v[ 2 ]+ "  " )
                        .appendTo( trainerList )
                        .addClass( "trainerItem ui-corner-all" );
                    $.lad.trainer.createbutton( v[ 0 ], "View" )
                        .appendTo( trnrDiv );
                    trainerList.append( "<br>" );
                });
                
                trainerList.appendTo( ctx() );

                $.lad.tutorial( "This is the main view for managing your " +
                    "trainers.  Each trainer will grow levels as they " +
                    "have their minions battle.", $(".trainerList"), "Here " +
                    "are the trainers that you command." );
                $.lad.tutorial( "You may view any of the trainers to send " +
                    "them into the arena or to manage their minions.",
                    $(".trainerItem button"), "Click here to view the " +
                    "trainer." );

                // "Add trainer" button if less than 8
                if( trnrs.length < 8 )
                {
                    $("<button>Add Trainer</button>").button().click(function(){
                        $.ladAjax({'addtrainer':''});
                    }).css( "margin-right", "10px" ).appendTo( ctx() )
                        .attr( "id", "addTrainerBtn" );
                    $.lad.tutorial( "At any given time you may add another " +
                        "trainer that you will be able to command.",
                        $("#addTrainerBtn"), "Click here to add a trainer." );
                }

                // Add the modifiers button
                $("<button>Modifiers</button>").button().click(function(){
                    $.ladAjax({'viewmodifiers':''});
                }).css( "margin-right", "10px" ).appendTo( ctx() )
                    .attr( "id", "modifiersBtn" );
                $.lad.tutorial( "Modifiers are a critical component to arena " +
                    "battles.  Having a good set of modifiers is often the " +
                    "difference between a win and a loss.", $("#modifiersBtn"),
                    "Click here to view your modifiers." );

                // Add the User EXP button
                $("<button>User EXP</button>").button().click(function(){
                    $.ladAjax({'viewuserexp': ''});
                }).css( "margin-right", "10px" ).appendTo( ctx() )
                    .attr( "id", "userExpBtn" );
                $.lad.tutorial( "As your trainers win arena battles, you " +
                    "gain experience by watching them.  This experience is " +
                    "then shared between all of your current and future " +
                    "trainers.", $("#userExpBtn"), "Click here to view your " +
                    "weapon experience." );

                // If the user has battle stats, and the button to view them
                if( hasBS )
                {
                    $("<button>User Battle Stats</button>").click(function(){
                        $.ladAjax({'viewuserstats':''});
                    }).button().css( "margin-right", "10px" ).appendTo( ctx() )
                        .attr( "id", "userBSBtn" );
                    $.lad.tutorial( "You may also view the cumulative " +
                        "statistics for all of your trainers here.",
                        $("#userBSBtn"), "Click here to view your cumulative " +
                        "battle statistics." );
                }
            }
        },
        userexp: {
            overview: function( exps ){
                var headers = {
                    Target: "true",
                    Level: "true",
                    Exp: "true",
                    Action: ""
                }, i, genTables = {}, specTables = {}, current, group,
                addExpTable, addExpTableGroup, levels;

                $.each( exps, function(i,v){
                    group = v[ 0 ];
                    current = [ v[ 1 ], v[ 2 ], v[ 3 ] ];
                    levels = v[ 4 ];

                    if( levels !== undefined && levels.length > 0 )
                    {
                        current.push( $("<button>Increase </button>")
                            .append( "(" + levels.length + ")" )
                            .click($.lad.userexp.increasePrompt).button()
                            .data( "target", group ).data( "type", v[ 1 ] )
                            .data( "level", v[ 2 ] ).data( "exp", v[ 3 ] )
                            .data( "bonuses", v[ 4 ] )
                        );
                    }
                    else
                    {
                        current.push( "" );
                    }
                    if( group === 'Two Hand' || group === 'Off Hand' ||
                        group === 'Either Hand' || group === 'Main Hand' )
                    {
                        if( genTables[ group ] === undefined )
                        {
                            genTables[ group ] = [];
                        }
                        genTables[ group ].unshift( current );
                    }
                    else
                    {
                        if( specTables[ group ] === undefined )
                        {
                            specTables[ group ] = [];
                        }
                        specTables[ group ].push( current );
                    }
                });

                ctx().html( "" );
                addExpTable = function( i, v )
                {
                    var id = i.replace( /\s/, '_' ) + 'exp';
                    $("<div></div>").addClass( "exptable" )
                        .append( "<span>" + i + "</span>" )
                        .append( makeSortableTable( headers, v, id ) )
                        .appendTo( current );
                };
                addExpTableGroup = function( tables, group )
                {
                    current = $("<div></div>").appendTo( ctx() )
                        .addClass( "exptablegroup" )
                        .attr( "id", group + "grp" );
                    $("<div></div>").append( group )
                        .addClass( "exptableheader ui-corner-tl ui-corner-tr" )
                        .appendTo( current );
                    $.each( tables, addExpTable );
                };
                addExpTableGroup( genTables, "General" );
                addExpTableGroup( specTables, "Specific" );
                ctx().append( "<br>" );

                $.lad.tutorial( "General experience is broken down into " +
                    "2-handed, main hand, off hand and either hand weapons." +
                    "Each of the general categories grow at a normal rate in " +
                    "trainer battles and increase whenever you win with a " +
                    "weapon of the same type.", $("#Generalgrp"), "Here are " +
                    "the various general experience groups." );
                $.lad.tutorial( "Specific experience is based solely on the " +
                    "specific weapon used in the arena battles.  Each of the " +
                    "specific categories only increase whenever you win with " +
                    "the corresponding weapon, but they will increase at " +
                    "twice the rate of the general experience.",
                    $("#Specificgrp"), "Here are the various specific " +
                    "experience groups." );
                $.lad.main.returnButton().appendTo( ctx() );
            },
            increasePrompt: function( )
            {
                var type = $(this).data( "type" ),
                    target = $(this).data( "target" ),
                    exp = "<span style='color:#00DD00'>" +
                          $(this).data( "exp" ) + "</span>",
                    levels = $(this).data( "bonuses" ),
                    dialog = $("<div></div>")
                        .append( target + " " + type + "<br>You currently " +
                                 "have " + exp + " experience.<br>" )
                        .appendTo( $("body") ).attr( "title", "EXP Increase" ),
                    increaseCost = $("<span style='color:#DD0000'></span>")
                        .attr( "id", "increasecost" ),
                    increaseLevel = $("<select></select>")
                        .attr( "id", "increaselevel" ),
                    increaseBtn = $("<button>Increase</button>")
                        .attr( "id", "increaseBtn").button().click(function(){
                            $.ladAjax( {
                                'increaseexplevel': $("#increaselevel").val(),
                                'type': type,
                                'target': target
                            });
                            dialog.remove();
                        }),
                    levelCost = 0;

                dialog.append( "It will cost you " )
                    .append( increaseCost )
                    .append( " experience to grow " )
                    .append( increaseLevel )
                    .append( " levels.  " )
                    .append( increaseBtn )
                    .dialog();
                
                $.each( levels, function(i,v){
                    levelCost += v;
                    var level = i + 1;
                    increaseLevel.append( $("<option></option>")
                        .attr( "value", level ).append( level )
                        .data( "cost", levelCost )
                    );
                });
                increaseLevel.change(function(){
                    increaseCost.html(
                        $(this).children( ":selected" ).data( "cost" )
                    );
                }).change();
            }
        },
        stats: {
            view: function( stats, mode, trnr ) {
                var statTypes = $.lad.stats.types(),
                    statRow = $("<tr></tr>"), modRow = $("<tr></tr>"),
                    valueRow = $("<tr></tr>"), tbl = $("<table></table>");
                    
                ctx().html( "" );
                $.each( statTypes, function(i,v){
                    statRow.append( "<td>" + v[ 0 ] + "</td>" );
                    modRow.append( "<td>" + v[ 1 ] + "</td>" );
                });
                $.each( stats, function(i,v){
                    v = Math.round( v * 10000 ) / 10000;
                    valueRow.append( "<td>" + v + "</td>" );
                });
                tbl.append( statRow ).append( modRow ).append( valueRow )
                    .appendTo( ctx() ).attr({
                        "id": "battlestatstbl",
                        "border": 1
                    });
                $.lad.tutorial( "Here are all of the statistics for your " +
                    "battles in the arena.  The first row contains a " +
                    "description of what the statistic is.  The second row " +
                    "contains which modifier type affects the given " +
                    "statistic.  The final row is your actual value.",
                    $("#battlestatstbl"), "Here is the table of your " +
                    "statistics." );

                // Output return button
                if( mode === "Trainer" )
                {
                    $.lad.trainer.createbutton( trnr, "Return to Trainer" )
                        .appendTo( ctx() );
                }
                else
                {
                    // Return to main button
                    $.lad.main.returnButton().appendTo( ctx() );
                }
            },
            types: function() {
                //# STAT TYPES
                return [ [ "Damage Dealt", "Damage" ], [ "...", "..." ] ];
                //# END STAT TYPES
            }
        },
        modifiers: {
            overview: function( mods ){
                var headers = {
                    Type: "true",
                    Battles: "true",
                    Action: ""
                }, btn, modList = [], row;

                $.each( mods, function(i,v){
                    // If modifier is equipped don't let it be destroyed
                    btn = v[ 3 ] ? "In Battle" :
                        $("<button>Destroy</button>").click(function(){
                            $.ladAjax({'deletemodifier': v[ 0 ]});
                        }).button();
                    // [ID, type, battles, equipped] => [type, battles, action]
                    row = [ v[ 1 ], v[ 2 ], btn ];
                    modList.push( row );
                });

                ctx().html( "" )
                    .append( makeSortableTable( headers, modList, 'mods' ) );
                $.lad.tutorial( "Modifiers are the core of advancing a " +
                    "trainer in battle.  Each trainer may bring up to 3 " +
                    "modifiers into battle.  Every modifier brought must " +
                    "apply to a different area (Aim, Flexibility, etc.).  " +
                    "Furthermore, some will grant bonuses more often, " +
                    "whereas others will last longer in battle.", $("#modstbl"),
                    "Here is the table of all the modifiers you own." );
                $.lad.tutorial( "As your trainers only bring up to 3 " +
                    "modifiers (different types each) it is sometimes smart " +
                    "to delete your less wanted modifiers.  This way you " +
                    "are more likely to use your better modifiers.",
                    $("#modstbl tbody tr td button"), "Click here to destroy " +
                    "one of your modifiers." );
                $.lad.main.returnButton().appendTo( ctx() );

            }
        },
        blockedAction: function(txt){
            var popup = $("<span class='ui-state-error ui-corner-all' " +
                          "style='padding: 0.2em;font-size:smaller'></span>");
            popup.append( "<a>" + txt + "</a>" ).prependTo( $("#notifier") );
            popup.show( "slide", {direction: 'right'}, 1000, function(){
                setTimeout(function(){
                    popup.fadeOut( "slow", function(){
                        popup.remove();
                    });
                }, 5000 );
            });
        },
        tutorial: function( txt, block, tiptxt ){
            // First parameter is actually added to the tutorial bar.
            // Second is a jQuery block which will be highlighted when
            // the given text is mouseover'd, Third is the qtip content to show
            // over the jQuery block
            if( $("#tutorial").length === 0 )
            {
                $( "<div id='tutorial'></div>" )
                    .addClass( "ui-corner-tl ui-corner-bl" )
                    .prependTo( "#ladheader" );
            }

            // If the text block is empty, clear out the whole content
            if( txt === undefined || txt.length === 0 )
            {
                $("#tutorial").html( "" );
                return;
            }

            var tutorial = $("#tutorial"),
                current = $("<div></div>");
            current.mouseover(function(){
                $(".highlight-tutorial").removeClass( "highlight-tutorial" );
                block.addClass( "highlight-tutorial" );
                var repeatFunction = function(){
                    var obj = $(".highlight-tutorial"), ran = false;
                    if( obj.length === 0 )
                    {
                        return;
                    }
                    obj.animate({opacity: 0.6}, 500 ).animate(
                        {opacity: 1.0}, 500, function(){
                            if( !ran )
                            {
                                $(this).queue(function(){
                                    repeatFunction();
                                    $(this).dequeue();
                                });
                                ran = true;
                            }
                    });
                };
                repeatFunction();
                $(this).addClass( "highlight" );
            }).mouseout(function(){
                $(".highlight-tutorial").queue( "fx", [] ).stop()
                    .removeClass( "highlight-tutorial").css( "opacity", 1.0 );
                $(this).removeClass( "highlight" );
            }).append( txt ).qtip({
                content: tiptxt,
                position: {
                    my: 'top left',
                    at: 'bottom right',
                    target: block,
                    container: $("#ladbkgd"),
                    viewport: $("#ladbkgd")
                }
            }).appendTo( tutorial );
        }
    };
}));
