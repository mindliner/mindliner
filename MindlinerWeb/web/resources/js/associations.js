/* builds the force directed graph to show the users map */
function buildAssociationsMap(dataString, contextPath) {
    var _chart = {},
            data,
            w = parseInt(d3.select("svg").style("width")),
            h = parseInt(d3.select("svg").style("height")),
            z = d3.scale.ordinal().range(["#22A1B5", "#dddddd", "#9AC265", "#034E5A"]),
            minRank = 99999,
            maxRank = 0,
            duration = 800,
            level = 2,
            pressTimer,
            pressTime = 500,
            // radius of the biggest node
            nodeSizeFactor = 25,
            rootId,
            rootHistory = new Array(),
            gnodes,
            clickedNodeId,
            svg;

    var force = d3.layout.force()
            .charge(function (d) {
                return -1450;
            })
            .gravity(0.225)
            .friction(0.7)
            .linkDistance(120)
            .linkStrength(0.7)
            .size([w, h]);
    
    // prepare the svg object
    svg = d3.select("#content").select("svg")
            .attr("transform", "translate(" + [-w / 50, 0] + ")");

    // unselect nodes if clicked somewhere else then on the node
    d3.select("body").on("click", function () {
        setSelection();
    });
    
    // main render function
    _chart.render = function () {
        // prepare nodes
        newNodes = _chart.data;
        var nodesArray = [];
        for (var i = 0; i < newNodes.nodes.length; i++) {
            var node = newNodes.nodes[i];
            nodesArray.push(node);
            if(minRank > node.rank)
                minRank = node.rank;
            if(maxRank < node.rank)
                maxRank = node.rank;
        }
        
        // prepare links
        links = [];
        for (var i = 0; i < newNodes.links.length; i++) {
            var temp = {};
            for (var j = 0; j < nodesArray.length; j++) {
                if (nodesArray[j].id === newNodes.links[i].source) {
                    temp.source = nodesArray[j];
                }
                if (nodesArray[j].id === newNodes.links[i].target) {
                    temp.target = nodesArray[j];
                }
            }
            links.push(temp);
        }
        
        // root id is the first node in the array
        rootId = newNodes.nodes[0].id;
        rootHistory.push(rootId);
        
        // activate the back button if we received the nodes more than once
        if (rootHistory.length >= 2) {
            d3.select("#fdgRootHistoryButton").style("background", "#77B6BF");
        } else {
            d3.select("#fdgRootHistoryButton").style("background", "lightgrey");
        }
        
        // runs the main d3 force functions
        force.nodes(nodesArray)
                .links(links)
                .start();
        
        // drawing the links
        var link = svg.selectAll("line").data(links);
        link.enter()
                .append("line")
                .classed("linklines", true);

        link.exit().remove();

        link
                .style("stroke", "#999")
                .attr("class", function (d) {
                    return "linklines" + " linksource" + d.source.id + " linktarget" + d.target.id;
                })
                .style("stroke-width", "0px")
                .transition().duration(duration)
                .style("opacity", 1)
                .style("stroke-width", "1px");

        // remove old nodes to make sure the new ones are on top
        svg.selectAll("g.gnode").remove();

        // node group: To group circles and labels; drawing the nodes
        var gnodes = svg.selectAll("g.gnode").data(nodesArray, function (d) {
            return d.id;
        });

        gnodes.enter()
                .append("g")
                .classed("gnode", true);
        gnodes.exit().remove();

        // circles for the node group
        var node = gnodes.append("circle")
                // color has to be adapted
                .style("fill", function (d) {
                    if (d.color)
                        return d.color;
                    // if color not chosen, choose by hirarchy
                    return z(d.parent && d.parent.headline);
                })
                .attr("class", function (d) {
                    return "circles n" + d.id;
                })
                .style("stroke", "#000")
                // double click event which makes the selected node to the root and loads its childs
                .on("dblclick", function (d) {
                    setSelection(this);
                    loadNewNodes();
                })
                // click event for node selection
                .on("click", function (d) {
                    setSelection(this);
                    d3.event.stopPropagation();
                })
                // holding events (click)
                .on("mouseup", (function () {
                    clearTimeout(pressTimer);
                    d3.event.stopPropagation();
                }))
                .on("mousemove", function () {
                    clearTimeout(pressTimer);
                    d3.event.stopPropagation();
                })
                .on("mousedown", (function () {
                    startPressing();
                    d3.event.stopPropagation();
                }))
                // holding events (touch)
                .on("touchstart", (function () {
                    d3.event.preventDefault();
                    setSelection(this);
                    startPressing();
                    d3.event.preventDefault();
                    d3.select(d3.event.target)
                            /*
                             touch move makes it to hard on the mobile, since the smallest movement stops the timeout
                             .on("touchmove", function() {
                             clearTimeout(pressTimer);
                             d3.event.stopPropagation();
                             })
                             */
                            .on("touchend", function () {
                                clearTimeout(pressTimer);
                                d3.event.stopPropagation();
                            });
                    d3.event.stopPropagation();
                }))
                .call(force.drag)
                .attr("r", 0)
                .attr("headline", function (d) {
                    return d.headline;
                })
                .attr("description", function (d) {
                    return d.description ? d.description : " ";
                })
                .attr("nodeId", function (d) {
                    return d.id;
                })
                .transition().duration(duration)
                .attr("r", function (d) {
                    // dynamic size of the circles
                    if (d.rank > 0)
                        return d.rank / maxRank * nodeSizeFactor + 4;
                    else
                        return 0.5 * nodeSizeFactor + 4;
                });

        // draw labels for the node group
        d3.selectAll("text").remove();
        var label = gnodes.append("text")
                .attr("class", function (d) {
                    return "label" + " labelId" + d.id;
                })
                .text(function (d) {
                    var headLineString = d.headline;
                    if (headLineString.length > 20) {
                        headLineString = headLineString.substr(0, 19) + "...";
                    }
                    return headLineString;
                })
                .attr("text-anchor", function (d, i) {
                    return getLabelAnchor(d);
                })
                .attr("dx", function (d) {
                    return getLabelPosition(d);
                })
                .style("font-size", "1em")
                .attr("dy", ".35em")
                .call(force.drag)
                .style("opacity", 0)
                .transition().duration(duration)
                .style("opacity", 1);

        // d3 adapted force handling
        force.on("tick", function (e) {
            link.attr("x1", function (d) {
                return d.source.x;
            })
                    .attr("y1", function (d) {
                        return d.source.y;
                    })
                    .attr("x2", function (d) {
                        return d.target.x;
                    })
                    .attr("y2", function (d) {
                        return d.target.y;
                    });
            gnodes.attr("transform", function (d) {
                return "translate(" + d.x + "," + d.y + ")";
            });
        });

        // set the labels which are right from the center to right, and left from the center to the left of the nodes
        function getLabelAnchor(d) {
            if (d.id !== rootId)
                // if the node is on the ride side -> text from the label "starts" at the assigned position, else it "ends" there
                return d.x >= w / 2 ? "start" : "end";
            else
                // root node is always right sided
                return "start";
        }

        // returns the label position from its node depending on where the node lies
        function getLabelPosition(d) {
            // positionFaktor 1 = label on the right side, -1 = label on the left side
            var positionFactor;
            if (getLabelAnchor(d) === "start")
                positionFactor = 1;
            else
                positionFactor = -1;

            // check if the node has a rank -> the size of the node, and so its label position depend on the rank
            if (d.rank > 0)
                return positionFactor * (d.rank / maxRank * nodeSizeFactor + 8);
            else
                return positionFactor * (0.5 * nodeSizeFactor + 8);
        }
    };

    _chart.renderNewNodes = function (newData) {
        _chart.data = JSON.parse(newData);
        _chart.render();

        // scale the view
        if (level === 3) {
            var wTrans = w * .12;
            var hTrans = h * .12;
            svg.attr("transform", "translate(" + [wTrans, hTrans] + ") scale(0.8, 0.8)");
        } else {
            svg.attr("transform", "scale(1, 1)");
        }
    };

    _chart.renderNewNodes(dataString);

    function loadNewNodes() {
        d3.selectAll("text.label").remove();
        d3.selectAll("line.linklines").style("stroke-width", "0px");
        d3.selectAll("circle.circles")
                .transition().duration(duration).attr("r", 0)
                .each("end", function (d, i) {
                    // only call the render function after the last node has finished the animation
                    if (i === d3.selectAll("circle.circles").size() - 1)
                        getNewNodes(clickedNodeId, level);
                });
    }

    function setSelection(node) {
        // check if the chosen object is already selected
        clear();
        if (node == null || node.className.baseVal.indexOf('fdgNodeSelected') >= 0) {
            d3.select("circle.fdgNodeSelected").classed("fdgNodeSelected", false);
        }
        else {
            // Remove 'selected'-class on the object already selected
            d3.select("circle.fdgNodeSelected").classed("fdgNodeSelected", false);
            // select the object, to allow keydown events for it
            d3.select(node).classed("fdgNodeSelected", true);
            var headLineString = d3.select("circle.fdgNodeSelected").attr("headline");
            clickedNodeId = d3.select("circle.fdgNodeSelected").attr("nodeId");
            /*       if (headLineString.length > 20) {
             headLineString = headLineString.substr(0, 19) + "...";
             } */
            d3.select("#nodeHeadline").text(headLineString);
            d3.select("#nodeDescription").text(d3.select("circle.fdgNodeSelected").attr("description"));
            // add buttons
            d3.select("#associationsButtonsOnClick").style("visibility", "visible");
        }
    }

    _chart.goBack = function () {
        // distinguish between going back in the view and going back to the search results
        if (rootHistory.length > 1) {
            rootHistory.pop();
            var newRootId = rootHistory.pop();
            getNewNodes(newRootId, level);
        } else {
            //TODO: implement going back to search results
        }
    };

// Key listener
    d3.select(window).on("keydown", function () {
        // only allow keys if a node is selected
        if (document.querySelectorAll('.fdgNodeSelected').length > 0) {
            switch (d3.event.keyCode) {
                // keycode 69: e - Opens a form to edit the values of a node
                case 69:
                    /* final version */
                    editObject();
                    break;
                    // keycode 78: n - Opens a form to create a new node related to the selected node
                case 78:
                    createRelatedObject();
                    break;
                    // keycode 86: v - Makes the selected node to the root node and loads its childs
                case 86:
                    setSelection();
                    loadNewNodes();
                    break;
                    // keycode 46: delete - Deletes the selected node
                case 46:
                    deletedObject();
                    break;
                    // keycode t for testing purposes
                case 84:
                    alert("id: " + d3.select("#nodeId").property("value"));
                    break;
            }
        } else {
            // Non of the nodes is selected
            switch (d3.event.keyCode) {
                // keycode 78: n - Opens a form to create a new node without relation to a node
                case 78:
                    createNewObject();
                    break;
            }
        }
    });


    /* functions to call object related processes from outside */
    _chart.createNewObject = function () {
        createNewObject();
    };

    _chart.editObject = function () {
        editObject();
    };

    _chart.createRelatedObject = function () {
        createRelatedObject();
    };

    _chart.deleteObject = function () {
        deletedObject();
    };

    // changes how many child nodes are loaded and shown
    _chart.changeLevel = function () {
        if (level === 2)
            level = 4;
        else
            level = 2;
        getNewNodes(rootId, level);
    };


    /* functions which handle the user triggered processes */
    function createNewObject() {
        window.location.href = contextPath + "/createObject.xhtml";
    }

    function editObject() {
        var nodeId = d3.select("circle.fdgNodeSelected").attr("nodeId");
        window.location.href = contextPath + "/editObject.xhtml?id=" + nodeId + "&mapview=true";
    }

    function createRelatedObject() {
        var nodeId = d3.select("circle.fdgNodeSelected").attr("nodeId");
        window.location.href = contextPath + "/createObject.xhtml?id=" + nodeId + "&mapview=true";
    }

    function deletedObject() {
        if (confirm('Are you sure you want to delete the selected item?')) {
            var nodeId = d3.select("circle.fdgNodeSelected").attr("nodeId");
            var answer = deleteNode(nodeId);
        } else {
            //do nothing
        }
    }

    // returns the id of the selected node
    function getId() {
        return d3.select("circle.fdgNodeSelected").attr("nodeId");
    }

    // returns the description of the selected node
    function getDescription() {
        return d3.select("circle.fdgNodeSelected").attr("description");
    }

    // clears the forms
    function clear() {
        d3.select("#nodeHeadline").text("");
        d3.select("#nodeDescription").text("");
        // remove buttons
        d3.select("#associationsButtonsOnClick").style("visibility", "hidden");
    }

    // function to count time of a touch event (needed for touch devices)
    function startPressing() {
        pressTimer = window.setTimeout(function () {
            loadNewNodes();
        }, pressTime);
    }

    // reads the values of the form to change a node and calls the function for the server interaction
    function submitForm() {
        //TODO: function to save form data
    }

    // reads the values of the form to create a new node and calls the function for the server interaction
    function createNode() {
        var parentId = d3.select("#parentNodeId").property("value");
        var headline = d3.select("#newHeadline").property("value");
        var description = d3.select("#newDescription").property("value");
        var type = d3.select("#nodeType").property("value");
        createNewNode(parentId, headline, description, type);
    }

    d3.selectAll(".formdiv").style("display", "block").style("float", "right");

    _chart.onDeleteComplete = function (answer, nodeId) {
        if (answer === "1") {
            d3.select(".n" + nodeId).remove();
            d3.selectAll(".linksource" + nodeId).remove();
            d3.selectAll(".linktarget" + nodeId).remove();
            d3.selectAll(".labelId" + nodeId).remove();
            setSelection();
        } else if (answer === "0") {
            alert("You can't delete a foreign node");
        }

    };

    _chart.data = function (_data) {
        if (!arguments.length)
            return data;
        data = _data;
        return data;
    };

    return _chart;
}