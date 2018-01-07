/* builds a hirarchical edge tree, needs also linksReportDataBuilder.js to work */

function buildLinksReport(data) {
    if(data=='') {
        d3.select("#userLinksInfoContainer")
                .append("p")
                .text("No data for the selected period. Please try a longer interval");
        return;
    }
    
    data = JSON.parse(data);
    

    var w = 1280,
            h = 800,
            rx = w / 2,
            ry = h / 2,
            m0,
            rotate = 0,
            duration = 1000,
            round = 0;

    var splines = [];

    // tree
    var cluster = d3.layout.cluster()
            .size([360, ry - 120])
            .sort(function(a, b) {
                return d3.ascending(a.key, b.key);
            });

    // make groups
    var bundle = d3.layout.bundle();

    // links
    var line = d3.svg.line.radial()
            // form of the lines
            .interpolate("bundle")
            // how much are the lines formed (0 straight, 1 maximum interpolation)
            .tension(.85)
            .radius(function(d) {
                return d.y;
            })
            .angle(function(d) {
                return d.x / 180 * Math.PI;
            });

    // append an extra div into the div, to allow fixed rotation
    var div = d3.select("#userLinksContainer").insert("div");

    var svg = div.append("svg")
            //enable automatic graph resizing
            .attr("viewBox","0 0 1000 800")
            .attr("preserveAspectRatio","xMidYMid")
            .append("g")
            // center the graph
            .attr("transform", "translate(" + [rx, ry] + ")");

    svg.append("path")
            .attr("class", "arc")
            .attr("d", d3.svg.arc()
                    .outerRadius(ry - 120)
                    .innerRadius(0)
                    .startAngle(0)
                    .endAngle(2 * Math.PI))
            .on("mousedown", mousedown);


    function render() {
        var nodes = [], links = [], splines = [];

        // get nodes data
        nodes = cluster.nodes(linksReportDataBuilder.root(data));
        // get links data
        links = linksReportDataBuilder.links(nodes);

        // get maxLinks between to nodes, to calculate the strength of each link
        var maxLinks = linksReportDataBuilder.maxLinks(links);

        // make groups (format for groups: groupname.username)
        splines = bundle(links);
        var oldLinks = [];

        d3.selectAll("path")
                .each(function(d, i) {
                    oldLinks[i] = d3.select(this);
                });

        // create new links, update or delete old links
        var path = svg.selectAll("path.link").data(links);

        // add links
        path.enter()
                .append("path")
                .style("opacity", 1e-6)
                .transition().duration(duration)
                .style("opacity", 0.6);

        // remove old links
        path.exit().transition().duration(duration).style("opacity", 1e-6).remove();

        // update link values
        path.attr("class", function(d) {
            return "selected link source-" + d.source.key
                    + " target-" + d.target.key;
        })
                .attr("stroke-width", function(d, i) {
                    return d.strength / maxLinks * 3 + 1 + "px";
                });

        path.attr("d", function(d, i) {
            return line(splines[i]);
        });


        // remove all old nodes and add new ones
        svg.selectAll("g.node").remove();
        var gnodes = svg.selectAll("g.node")
                .data(nodes.filter(function(n) {
                    return !n.children;
                }));

        // add new nodes
        gnodes.enter().append("g")
                .attr("class", "node")
                .attr("id", function(d) {
                    return "node-" + d.key;
                });

        // update nodes to their position
        gnodes
                .attr("transform", function(d) {
                    return "rotate(" + (d.x - 90) + ")translate(" + d.y + ")";
                })
                .append("text")
                .attr("dx", function(d) {
                    return d.x < 180 ? 8 : -8;
                })
                .attr("dy", ".31em")
                .attr("text-anchor", function(d) {
                    return d.x < 180 ? "start" : "end";
                })

                .attr("transform", function(d) {
                    // rotate text to make it readable
                    return d.x < 180 ? null : "rotate(180)";
                })
                .text(function(d) {
                    return d.key;
                })
                .style("font-size", function() {
                    if (nodes.length < 15)
                        return 20 / nodes.length + "em";
                    else
                        return "1.4em";
                })
                .on("mouseover", mouseover)
                .on("mouseout", mouseout);

        // interactive change of tension -> html element needed for this
        d3.select("input[type=range]").on("change", function() {
            line.tension(this.value / 100);
            path.attr("d", function(d, i) {
                return line(splines[i]);
            });
        });

    }

    // add function to rotate the graph
    d3.select(window)
            .on("mousemove", mousemove)
            .on("mouseup", mouseup);

    function mouse(e) {
        return [e.pageX - rx, e.pageY - ry];
    }

    function mousedown() {
        m0 = mouse(d3.event);
        d3.event.preventDefault();
    }

    function mousemove() {
        if (m0) {
            var m1 = mouse(d3.event),
                    dm = Math.atan2(cross(m0, m1),
                            dot(m0, m1)) * 180 / Math.PI;
            div.style("-webkit-transform", "translateY(" + (ry - rx)
                    + "px)rotateZ(" + dm
                    + "deg)translateY(" + (rx - ry) + "px)");
        }
    }

    function mouseup() {
        if (m0) {
            var m1 = mouse(d3.event),
                    dm = Math.atan2(cross(m0, m1),
                            dot(m0, m1)) * 180 / Math.PI;

            rotate += dm;
            if (rotate > 360)
                rotate -= 360;
            else if (rotate < 0)
                rotate += 360;
            m0 = null;

            div.style("-webkit-transform", null);

            svg
                    .attr("transform", "translate(" + [rx, ry]
                            + ")rotate(" + rotate + ")")
                    .selectAll("g.node text")
                    .attr("dx", function(d) {
                        return (d.x + rotate) % 360 < 180 ? 8 : -8;
                    })
                    .attr("text-anchor", function(d) {
                        return (d.x + rotate) % 360 < 180 ? "start" : "end";
                    })
                    .attr("transform", function(d) {
                        return (d.x + rotate) % 360 < 180 ? null : "rotate(180)";
                    });
        }
    }

    // function to colorize the links -> source, target
    function mouseover(d) {
        svg.selectAll("path.link.target-" + d.key)
                .classed("target", true)
                .each(updateNodes("source", true));

        svg.selectAll("path.link.source-" + d.key)
                .classed("source", true)
                .each(updateNodes("target", true));
    }

    function mouseout(d) {
        svg.selectAll("path.link.source-" + d.key)
                .classed("source", false)
                .each(updateNodes("target", false));

        svg.selectAll("path.link.target-" + d.key)
                .classed("target", false)
                .each(updateNodes("source", false));
    }

    function updateNodes(name, value) {
        return function(d) {
            if (value)
                this.parentNode.appendChild(this);
            svg.select("#node-" + d[name].key).classed(name, value);
        };
    }

    function cross(a, b) {
        return a[0] * b[1] - a[1] * b[0];
    }

    function dot(a, b) {
        return a[0] * b[0] + a[1] * b[1];
    }

    render();
}


