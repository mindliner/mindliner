/* builds a bar chart */

function buildBarChart(data) {
    data = JSON.parse(data);
    var margin = {top: 20, right: 20, bottom: 30, left: 40},
    width = 960 - margin.left - margin.right,
            height = 500 - margin.top - margin.bottom;
    var first = true, dataset = 0;

    var duration = 1000;

    var x0 = d3.scale.ordinal()
            .rangeRoundBands([0, width], .1);

    var x1 = d3.scale.ordinal();

    var y = d3.scale.linear()
            .range([height, 0]);

    var color = d3.scale.category20c();

    var xAxis = d3.svg.axis()
            .scale(x0)
            .orient("bottom");

    var yAxis = d3.svg.axis()
            .scale(y)
            .orient("left")
            .tickFormat(d3.format(".2s"));
    /* horizontal */
    var svg = d3.select("#barChartContainer").append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    /* vertical */
    /*            var svg = d3.select("body").append("svg")
     .attr("width", height + margin.left + margin.right)
     .attr("height", width + margin.top + margin.bottom)
     .append("g")
     .attr("transform", "translate(" + (width / 2 + margin.left) + "," + margin.top + "),rotate(90)");
     */

    var rankings;
    var mainData;


    mainData = data;
    //rankings = data[dataset].data;
    rankings = data;
    setAllRankings();

    function cleanAll() {
        d3.selectAll("rect").transition().duration(duration).attr("y", height).attr("height", 0)
                .each("end", function(d, i) {
                    if (i == d3.selectAll(".state").size() - 1)
                        setAllRankings();
                });

    }
    function minmax(data) {
        var minY = 99999, maxY = -99999;
        data.forEach(function(d) {
            d.values.forEach(function(dv) {
                if (minY > dv.count)
                    minY = dv.count;
                if (maxY < dv.count)
                    maxY = dv.count;
            });
        });
        var extent = [0, maxY];
        return extent;
    }

    function setAllRankings() {
        var data = rankings;
        svg.selectAll(".state").remove();

        x0.domain(rankings.map(function(d) {
            return d.date;
        }));
        var extent = minmax(data);
        y.domain([extent[0], extent[1]]);

        if (first) {
            svg.append("g")
                    .attr("class", "x axis")
                    .attr("transform", "translate(0," + height + ")")
                    .call(xAxis);

            svg.append("g")
                    .attr("class", "y axis")
                    .call(yAxis)
                    .append("text")
                    .attr("transform", "rotate(-90)")
                    .attr("y", 6)
                    .attr("dy", ".71em")
                    .style("text-anchor", "end")
                    .text("amount");
            first = false;
        } else {
            svg.select(".x.axis")
                    .transition().duration(duration)
                    .call(xAxis);

            svg.select(".y.axis")
                    .transition().duration(duration * 2)
                    .call(yAxis);
        }

        var state = svg.selectAll(".state")
                .data(data)
                .enter().append("g")
                .attr("class", "state")
                .attr("transform", function(d) {
                    return "translate(" + x0(d.date) + ",0)";
                })
                .on("click", function(d, i) {
                    setOneRanking(i);
                });

        var months = data.length;

        state.selectAll("rect")
                .data(function(d) {
                    var val = d.values;
                    var onlyThree = [];
                    for (var i = 0; i < 3; i++) {
                        onlyThree.push(val[i]);
                    }
                    return onlyThree;
                })
                .enter().append("rect")
                .attr("width", function(d) {
                    return width / (months) / 4;
                })
                .attr("x", function(d, i) {
                    return i * width / (months) / 4;
                })
                .attr("y", function(d) {
                    return height;
                })
                .attr("height", function(d) {
                    return 0;
                })
                .style("fill", function(d, i) {
                    return color(i);
                })
                .transition().duration(duration)
                .attr("y", function(d) {
                    return y(d.count);
                })
                .attr("height", function(d) {
                    return height - y(d.count);
                });

        state.selectAll("text")
                .data(function(d) {
                    var val = d.values;
                    var onlyThree = [];
                    for (var i = 0; i < 3; i++) {
                        onlyThree.push(val[i]);
                    }
                    return onlyThree;
                })
                .enter().append("text")
                .text(function(d, i) {
                    return d.word;
                })
                .attr("x", function(d, i) {
                    return -(height - 20);
                })
                .attr("y", function(d, i) {
                    return i * width / months / 4 + width / months / 7;
                })
                .attr("transform", "rotate(270)")
                .attr("fill", "white")
                .attr("font-size", function() {
                    return 5 * 1 / (Math.log(months) + 1) + "em";
                });
    }

    function setOneRanking(index) {
        var data = [];
        data.push(rankings[index]);

        x0.domain(data.map(function(d) {
            return d.date;
        }));

        var extent = minmax(data);
        y.domain([extent[0], extent[1]]);


        svg.select(".x.axis")
                .transition().duration(duration)
                .call(xAxis);

        svg.select(".y.axis")
                .transition().duration(duration * 2)
                .call(yAxis);

        var state = svg.selectAll(".state")
                .data(data)
                .on("click", function() {
                    cleanAll();
                });

        state.exit().selectAll("text").remove();
        state.exit().transition().duration(duration).selectAll("rect").attr("y", height).attr("height", 0).remove();

        state.transition().duration().attr("transform", function(d) {
            return "translate(" + x0(d.date) + ",0)";
        });

        state.selectAll("rect").data(data[0].values)
                .transition().duration(duration)
                .attr("width", function(d) {
                    return width / 11;
                })
                .transition().duration(duration)
                .attr("x", function(d, i) {
                    return (i - 1) * width / 11;
                })
                .attr("height", function(d) {
                    return height - y(d.count);
                })
                .attr("y", function(d) {
                    return y(d.count);
                });

        state.selectAll("rect").data(data[0].values)
                .enter().append("rect")
                .attr("width", 0)
                .attr("x", width)
                .attr("height", 0)
                .transition().duration(duration)
                .attr("width", function(d) {
                    return width / 11;
                })
                .attr("x", function(d, i) {
                    return (i - 1) * width / 11;
                })
                .attr("y", function(d) {
                    return y(d.count);
                })
                .attr("height", function(d) {
                    return height - y(d.count);
                })
                .style("fill", function(d, i) {
                    return color(i);
                });


        state.selectAll("text")
                .data(data[0].values)
                .transition().delay(duration).duration(duration)
                .attr("y", function(d, i) {
                    return (i - 1) * width / 11 + width / 11 / 2;
                })
                .attr("transform", "rotate(270)")
                .attr("fill", "black")
                .attr("font-size", function() {
                    return "3em";
                });

        state.selectAll("text")
                .data(data[0].values)
                .enter().append("text")
                .text(function(d, i) {
                    return d.word;
                })
                .attr("x", function(d, i) {
                    return -(height - 20);
                })
                .attr("y", function(d, i) {
                    return width;
                })
                .attr("transform", "rotate(270)")
                .attr("fill", "black")
                .attr("font-size", function() {
                    return "3em";
                })
                .transition().duration(duration)
                .attr("y", function(d, i) {
                    return (i - 1) * width / 11 + width / 11 / 2 + 10;
                });
    }
}
