/* 
 * This script builds an object change report.
 * 
 * Author: Marius Messerli
 * July 5th, 2013
 */

function buildChangeReport(data) {
    
    var root = {"name": "root", "children": data};

    var diameter = 960;

    var bubble = d3.layout.pack()
            .sort(null)
            .size([diameter, diameter])
            .padding(1.5);

    var svg = d3.select("body").append("svg")
            .attr("width", diameter)
            .attr("height", diameter)
            .attr("class", "bubble");

    var node = svg.selectAll(".node")
            .data(bubble.nodes(root).filter(function(d) {
        return !d.children;
    })
            )
            .enter().append("g")
            .attr("class", "node")
            .attr("transform", function(d) {
        return "translate(" + d.x + "," + d.y + ")";
    });

    node.append("title")
            .text(function(d) {
        return d.name;
    });

    node.append("circle")
            .attr("r", function(d) {
        return d.r;
    })
            .style("fill", function(d) {

        return d3.hsl(210, d.count, .5).toString();
    });

    node.append("text")
            .attr("dy", ".3em")
            .style("text-anchor", "middle")
            .text(function(d) {
        return d.name;
    });

}

