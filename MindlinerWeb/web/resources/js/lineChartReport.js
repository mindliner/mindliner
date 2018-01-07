/**
 * Build a lineChart 
 * 
 * @param data String in JSON format containing the data for the last year
 * @param axisMeasurement String containing the measurement type, to label the axis
 * @param axisUnitSymbol String containing the measurement unit, to label the measurement units; can be left out
 * @returns chart object, to be able to call update functions from outside
 */

function buildLineChart(data, axisMeasurement, axisUnitSymbol) {
    // mzh: should be moved to a more appropriate place (concerns dashboard)
    $('#smallsearchfield-nav').hide();
    
    // check if it's an empty string before parsing, since the parser breaks on empty strings
    if (data != '')
        data = JSON.parse(data);

    // if there are no records or only one record of the data, the line graph can't be shown
    if (data == '' || data[0].values.length <= 1) {
        d3.select("#userActivityContainer")
                .append("p")
                .text("There isn't enough data for the graph yet. Please wait a few days.");
        return;
    }

    if (!axisUnitSymbol)
        axisUnitSymbol = "";

    var _chart = {};

    var margin = {top: 20, right: 20, bottom: 30, left: 50},
    width = 660 - margin.left - margin.right,
            height = 500 - margin.top - margin.bottom,
            height2 = height / 7,
            duration = 1000,
            ease = "linear";

    // function to convert a date-string with de defined format to a standard javascript date object
    var parseDate = d3.time.format("%Y-%m-%d").parse,
            bisectDate = d3.bisector(function(d) {
                return d.date;
            }).left;

    // scales for the big chart
    var x = d3.time.scale()
            .range([0, width]);

    var y = d3.scale.linear()
            .range([height, 0]);
    // scales for the small chart
    var x2 = d3.time.scale()
            .range([0, width]);

    var y2 = d3.scale.linear()
            .range([height2, 0]);

    // add brush function to the small chart, to choose data
    var brush = d3.svg.brush()
            .x(x2)
            .on("brush", brushed);

    var color = d3.scale.category10();

    // axis defenition for the big chart
    var xAxis = d3.svg.axis()
            .scale(x)
            .orient("bottom");

    var yAxis = d3.svg.axis()
            .scale(y)
            .tickFormat(function(v) {
                return v + axisUnitSymbol;
            })
            .orient("left");
    // axis definition for the small chart
    var xAxis2 = d3.svg.axis()
            .scale(x2)
            .orient("bottom");

    var yAxis2 = d3.svg.axis()
            .scale(y2)
            .ticks(5)
            .orient("left");

    // line functions, to calculate the points
    var line = d3.svg.line()
            // make roundedd splines
            .interpolate('cardinal')
            .x(function(d) {
                return x(d.date);
            })
            .y(function(d) {
                return y(d.amount);
            });

    var line2 = d3.svg.line()
            .interpolate('cardinal')
            .x(function(d) {
                return x2(d.date);
            })
            .y(function(d) {
                return y2(d.amount);
            });


    // append a svg for the big chart
    var svg = d3.select("#userActivityContainer").append("svg")
            .attr("width", width + margin.left + margin.right * 3)
            .attr("height", height + margin.top * 2 + margin.bottom)
            .append("g")
            .attr("transform", "translate(" + (margin.left) + "," + margin.top * 2 + ")");

    // check first if an svg for the smaller line charts already exists
    if (document.querySelectorAll('.selectingChart').length == 0) {
        // append a svg for the small chart
        d3.select("#userActivityContainer").append("svg")
                .style("font", "10px sans-serif")
                .attr("width", (width + margin.left + margin.right))
                .attr("height", (height2 + margin.top + 2 * margin.bottom))
                .append("g")
                .attr("class", "selectingChart")
                .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
    } else {
        // removes the content of the previously created smaller svg
        d3.select(".selectingChart").selectAll("*").remove();
    }

    // store linesarray outside to make it possible to do fluent updates on it
    var lines = [];
    render(data);
    function render(json) {
        /* creates a map which stores the colors of the lines reachable with the linename */
        color.domain(json.map(function(d) {
            return d.name;
        }));

        json.forEach(function(kv) {
            kv.values.forEach(function(d) {
                d.date = parseDate(d.date);
            });
        });

        /* save the json string to the outside variable to make updates possible */
        lines = json;

        /* calculates the extent of the axes */
        var minX = d3.min(json, function(kv) {
            return d3.min(kv.values, function(d) {
                return d.date;
            });
        });
        var maxX = d3.max(json, function(kv) {
            return d3.max(kv.values, function(d) {
                return d.date;
            });
        });
        var minY = d3.min(json, function(kv) {
            return d3.min(kv.values, function(d) {
                return d.amount;
            });
        });
        var maxY = d3.max(json, function(kv) {
            return d3.max(kv.values, function(d) {
                return d.amount;
            });
        });

        x.domain([minX, maxX]);
        y.domain([minY, maxY]);

        x2.domain(x.domain());
        y2.domain(y.domain());

        /* adds a legend with the line names */
        var legend = svg.selectAll('g')
                .data(lines)
                .enter()
                .append('g')
                .attr('class', 'legend');

        legend.append('rect')
                .attr('x', 40)
                .attr('y', function(d, i) {
                    return i * 20 + 20;
                })
                .attr('width', 10)
                .attr('height', 10)
                .style('fill', function(d) {
                    return color(d.name);
                });

        legend.append('text')
                .attr('x', 40 + 15)
                .attr('y', function(d, i) {
                    return (i * 20) + 29;
                })
                .text(function(d) {
                    return d.name;
                })
                .style("fill", function(d) {
                    return color(d.name);
                });

        /* prepare main svg (add axis and clipPath) */
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
                .text(axisMeasurement);

        // define were the lines can be drawn
        svg.append("defs")
                .append("clipPath")
                .attr("id", "body-clip")
                .append("rect")
                .attr("x", 0)
                .attr("y", 0)
                .attr("width", width + margin.right)
                .attr("height", height + margin.top);

        /* add lines to main svg */
        var liner = svg.selectAll(".liner")
                .data(lines)
                .enter().append("g")
                .attr("class", "liner")
                .attr("clip-path", "url(#body-clip)");

        liner.append("path")
                .attr("class", "line")
                .attr("d", function(d) {
                    return line(d.values);
                })
                .style("stroke", function(d) {
                    return color(d.name);
                });

        /* add vertical line on hover with info */
        var focus = svg.append("g")
                .attr("class", "focus")
                .style("display", "none");

        focus.append("line")
                .attr("class", "focusline")
                .attr("x1", 0)
                .attr("y1", 0)
                .attr("x2", 0)
                .attr("y2", height)
                .style("stroke", "grey");

        focus.selectAll("circle")
                .data(lines)
                .enter()
                .append("circle")
                .attr("r", 4.5)
                .attr("x", function(d) {
                    return d.date;
                })
                .attr("y", function(d) {
                    return d.amount;
                })
                .attr("class", function(d, i) {
                    return "ci" + i;
                })
                .style("stroke-width", "2px")
                .style("stroke", function(d) {
                    return color(d.name);
                });

        focus.selectAll("rect")
                .data(lines)
                .enter()
                .append("rect")
                .attr("class", function(d, i) {
                    return "rc" + i;
                })
                .attr("width", "3em")
                .attr("height", "2em")
                .attr("fill", "white");

        focus.selectAll("text")
                .data(lines)
                .enter()
                .append("text")
                .attr("class", function(d, i) {
                    return "tx" + i;
                })
                .style("font-size", "1.5em")
                .attr("x", 10)
                .attr("dy", ".35em");

        svg.append("rect")
                .attr("class", "overlay")
                .attr("width", width)
                .attr("height", height)
                .on("mouseover", function() {
                    focus.style("display", null);
                })
                .on("mouseout", function() {
                    focus.style("display", "none");
                })
                .on("mousemove", mousemove);


        /* adds the second svg with choosing range functions */
        var context = d3.select(".selectingChart").selectAll(".line").data(lines)
                .enter()
                .append("g")
                .attr("class", "context");

        context.append("path")
                .attr("class", "line")
                .attr("d", function(d) {
                    return line2(d.values);
                })
                .style("stroke", function(d) {
                    return color(d.name);
                });

        context.append("g")
                .attr("class", "x axis")
                .attr("transform", "translate(0," + height2 + ")")
                .call(xAxis2);

        context.append("g")
                .attr("class", "y axis")
                .style("stroke-width", 0.8)
                .call(yAxis2);

        context.append("g")
                .attr("class", "x brush")
                .call(brush)
                .selectAll("rect")
                .attr("y", -6)
                .attr("height", height2 + 7);


    }

    /* updates the chart after clicking on button */
    function update(date1, date2) {
        date1 = parseDate(date1);
        date2 = parseDate(date2);
        x.domain([date1, date2]);

        /* update x axis */
        svg.select(".x.axis").transition().duration(duration).call(xAxis);
        /* update y axis */
        updateYAxes(date1, date2);

        /* update line with transitional effect */
        svg.selectAll(".line").transition().duration(duration).attr("d", function(d, i) {
            return line(d.values);
        });
    }

    /* updates the chart after selected with the brush */
    function brushed() {
        /* update x-axis */
        var extent = brush.empty() ? x2.domain() : brush.extent();
        x.domain([extent[0], extent[1]]);
        svg.select(".x.axis").call(xAxis);

        /* update y-axis */
        updateYAxes(extent[0], extent[1]);

        /* update lines */
        svg.selectAll(".line").attr("d", function(d, i) {
            return line(d.values);
        });
    }

    function updateYAxes(date1, date2) {
        /* update y-axis */
        var minY = d3.min(lines, function(kv) {
            return d3.min(kv.values, function(d) {
                if (d.date >= date1 && d.date <= date2)
                    return d.amount;
                else
                    return 99999;
            });
        });
        var maxY = d3.max(lines, function(kv) {
            return d3.max(kv.values, function(d) {
                if (d.date >= date1 && d.date <= date2)
                    return d.amount;
                else
                    return -99999;
            });
        });

        y.domain([minY, maxY]);

        svg.select(".y.axis").transition().duration(duration).call(yAxis);
    }

    /* controls the vertical line on chart hover */
    function mousemove() {
        for (var k = 0; k < lines.length; k++) {
            data = lines[k];
            var x0 = x.invert(d3.mouse(this)[0]),
                    i = bisectDate(data.values, x0, 1),
                    d0 = data.values[i - 1],
                    d1 = data.values[i],
                    d = x0 - d0.date > d1.date - x0 ? d1 : d0;

            var textWidth = d.amount.toString().length + "em";
            d3.select(".ci" + k).attr("transform", "translate(" + x(d.date) + "," + y(d.amount) + ")");
            d3.select(".rc" + k).attr("transform", "translate(" + (x(d.date) + 10) + "," + (y(d.amount) - 10) + ")")
                    .attr("width", textWidth);
            d3.select(".tx" + k).attr("transform", "translate(" + x(d.date) + "," + y(d.amount) + ")").text(d.amount);
            d3.select(".focusline").attr("transform", "translate(" + x(d.date) + "," + 0 + ")");
        }
    }

    /* calculates date after a button was clicked, and calls the update function to update the axes to the selected intervall */
    _chart.changeDate = function(intervall) {
        var startDate = new Date();
        var endDate = new Date();

        switch (intervall) {
            case 'LastWeek':
                // .getDay returns current weekday(eg. Tuesday = 2) -> today - 2 = sunday, sunday - 6 = monday last week
                startDate.setDate(startDate.getDate() - 6 - startDate.getDay());
                // monday last week + 4 days to reach friday last week
                endDate.setDate(startDate.getDate() + 4);
                break;
            case 'LastMonth':
                startDate.setMonth(startDate.getMonth() - 1);
                startDate.setDate(1);
                // .setDate(0) sets the date on the last day of the previous month
                endDate.setDate(0);
                break;
            case 'LastQuarter':
                var lastQuarter = Math.floor(startDate.getMonth() / 3);

                switch (lastQuarter) {
                    // Jan - Mar
                    case 0:
                        endDate.setMonth(2);
                        endDate.setDate(31);
                        break;
                        // Apr - Jun
                    case 1:
                        endDate.setMonth(5);
                        endDate.setDate(30);
                        break;
                        // Jul - Sept
                    case 2:
                        endDate.setMonth(8);
                        endDate.setDate(30);
                        break;
                        // Oct - Dec
                    case 3:
                        startDate.setFullYear(startDate.getFullYear() - 1);
                        endDate.setFullYear(endDate.getFullYear() - 1);
                        endDate.setMonth(11);
                        endDate.setDate(31);
                        break;
                }
                startDate.setDate(1);
                startDate.setMonth(lastQuarter * 3);
                break;
            case 'LastHalfYear':
                if (startDate.getMonth() > 5) {
                    startDate = new Date(startDate.getFullYear(), 0, 1);
                    endDate = new Date(endDate.getFullYear(), 5, 30);
                } else {
                    startDate = new Date(startDate.getFullYear() - 1, 6, 1);
                    endDate = new Date(endDate.getFullYear() - 1, 11, 31);
                }
                break;
            case 'LastYear':
                startDate = new Date(startDate.getFullYear() - 1, 0, 1);
                endDate = new Date(endDate.getFullYear() - 1, 11, 31);
                break;
            case 'YearToDate':
                startDate = new Date(startDate.getFullYear(), 0, 1);
                break;
        }

        startDate.setHours(0);
        startDate.setMinutes(0);
        startDate.setSeconds(0);
        startDate.setMilliseconds(0);
        endDate.setHours(0);
        endDate.setMinutes(0);
        endDate.setSeconds(0);
        endDate.setMilliseconds(0);


        var startYear = startDate.getFullYear();
        var startMonth = startDate.getMonth() + 1;
        if (startMonth < 10) {
            startMonth = "0" + startMonth;
        }
        var startDay = startDate.getDate();
        if (startDay < 10) {
            startDay = "0" + startDay;
        }

        var endYear = endDate.getFullYear();
        var endMonth = endDate.getMonth() + 1;
        if (endMonth < 10) {
            endMonth = "0" + endMonth;
        }
        var endDay = endDate.getDate();
        if (endDay < 10) {
            endDay = "0" + endDay;
        }

        // calls the update function, which adapts the axes to the selected intervall
        update((startYear + "-" + startMonth + "-" + startDay),
                (endYear + "-" + endMonth + "-" + endDay));
    }

    return _chart;
}
