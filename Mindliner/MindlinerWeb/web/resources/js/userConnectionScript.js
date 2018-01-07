/* 
 * Author: Marius and Niklaus Messerli
 * July 4th, 2013
 */

/**
 * Build a chord graph with the users as the object and the connections between
 * objects belonging to those users as the strokes.
 * 
 * @param {type} userConnectionMatrix An int[][] 3D vector with the connection count as the values.
 * @param {type} usernames A String[] vector with the user names that are used as labels
 * @returns {undefined}
 */
function buildInterConnectionChart(userConnectionMatrix, usernames) {
    var colors = new Array();

    var hueScale = d3.scale.linear().domain([1, usernames.length]).range([200, 240]);
    var saturationScale = d3.scale.linear().domain([1, usernames.length]).range([0.4, 0.7]);
    
    for (var i = 0; i < usernames.length; i++) {
        colors.push(d3.hsl(hueScale(i), saturationScale(i), 0.5).toString());
    }

    var chord = d3.layout.chord()
            .padding(.05)
            .sortSubgroups(d3.descending)
            .matrix(userConnectionMatrix);

    var width = 700;
    var height = 700;
    var innerRadius = Math.min(width, height) * .25, outerRadius = innerRadius * 1.1;

    d3.selectAll("svg").remove();
    var svg = d3.select("body").insert("svg")
            .attr("width", width)
            .attr("height", height)
            .append("g")
            .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");
    var group = svg.append("g").selectAll("path")
            .data(chord.groups)
            .enter().append("g").classed("arcstyle", true);
    group.append("path")
            .style("fill", function(d) {
                return colors[d.index];
            }).attr("d", d3.svg.arc().innerRadius(innerRadius).outerRadius(outerRadius))
            .on("mouseover", fade(.1))
            .on("mouseout", fade(1));
    group.append("text")
            .text(function(d) {
                return usernames[d.index];
            }).attr("class", "chordText")
            .attr("transform", function(d) {
                return "rotate(" + ((d.startAngle + d.endAngle) / 2 * 180 / Math.PI - 90) + ") translate(" + (outerRadius + 20) + ")";
            });
    svg.append("g")
            .attr("class", "chord")
            .selectAll("path")
            .data(chord.chords)
            .enter().append("path")
            .attr("d", d3.svg.chord().radius(innerRadius))
            .style("fill", function(d) {
                return colors[d.target.index];
            })
            .style("opacity", 1);

// Returns an event handler for fading a given chord group.
    function fade(opacity) {
        return function(g, i) {
            svg.selectAll(".chord path")
                    .filter(function(d) {
                        return d.source.index !== i && d.target.index !== i;
                    })
                    .transition()
                    .style("opacity", opacity);
            svg.selectAll(".chord path")
                    .filter(function(d) {
                        return d.source.index === i || d.target.index === i;
                    })
                    .style("fill", function(d) {
                        // use color of opposite group; we don't know whether that is the source or the target.
                        return colors[d.source.index === i ? d.target.index : d.source.index];
                    });
        };
    }

}