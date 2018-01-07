/* Script for the Advanced Search panel */

function selectAll(id, select) {
    var container = document.getElementById(id);
    var items = container.getElementsByTagName('input');
    for(var i = 0; i < items.length; i++) {
        items[i].checked = select;
    }   
}

$(document).ready(function () {
    
    // dashboard id
    var searchboxId = '#searchbox-form';
    var selectId = 'searchbox-form';
    if($('#searchbox-form\\:datapoolsLabel').length === 0)
    {
        // workspace id
        searchboxId = '#group\\:searchbox-form';
        selectId = 'group:searchbox-form';
    }

    /*********  VISIBILITY LISTENERS *********/
    $(searchboxId+'\\:datapoolsLabel').click(function(){
        $(searchboxId+'\\:datapools').toggle();
        $(searchboxId+'\\:objectowners').hide();
        $(searchboxId+'\\:more').hide();
    });

    $(searchboxId+'\\:objectownersLabel').click(function(){
        $(searchboxId+'\\:objectowners').toggle();
        $(searchboxId+'\\:datapools').hide()
        $(searchboxId+'\\:more').hide();
    });

    $(searchboxId+'\\:moreLabel').click(function(){
        $(searchboxId+'\\:more').toggle();
        $(searchboxId+'\\:datapools').hide();
        $(searchboxId+'\\:objectowners').hide();
    });

    $(searchboxId+'\\:advancedbtn').click(function(){
        // toggle() doesn't work here with Chrome... 
        if($(searchboxId+'\\:advanced-searchbox').css('display') === 'none'){
            $(searchboxId+'\\:advanced-searchbox').show();
        }else{
            $(searchboxId+'\\:advanced-searchbox').hide();
        }
    });
    
    /*********  (DE)SELECT ALL LISTENERS  *********/
    $(searchboxId+'\\:selectAllClients').click(function(){
        selectAll(selectId+':clientsCheckbox', true);
    });
    
    $(searchboxId+'\\:deselectAllClients').click(function(){
        selectAll(selectId+':clientsCheckbox', false);
    });
    
    $(searchboxId+'\\:selectAllOwners').click(function(){
        selectAll(selectId+':ownersCheckbox', true);
    });
    
    $(searchboxId+'\\:deselectAllOwners').click(function(){
        selectAll(selectId+':ownersCheckbox', false);
    });
});

