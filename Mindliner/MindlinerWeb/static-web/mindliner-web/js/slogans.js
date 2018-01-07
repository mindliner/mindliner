jQuery(document).ready(function($) {
    switchquote();
	setInterval(function(){switchquote()},10000);
});


// Show  a new quote on the page's top:
function switchquote() {
	if ( $("#quote .active").next().is("li")) {
	$("#quote .active").next().addClass("next");}
	else {$("#quote ul").children().first().addClass("next"); };
	$("#quote .active").animate({
			opacity: 0,
			marginLeft: "-150px",
			marginRight:"150px"
		}, 600 );
	$("#quote .next").animate({
			opacity: 1,
			marginLeft: "0px",
			marginRight: "0px"
		}, 750, function() {
				$("#quote .active").removeClass("active").removeAttr("style");
				$("#quote .next").addClass("active").removeClass("next").removeAttr("style");
			 });
}