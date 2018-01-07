(function() {
    linksReportDataBuilder = {
        // Construct the groups hirarchy
        root: function(classes) {
            var map = {};

            function find(name, data) {
                var node = map[name], i;
                if (!node) {
                    node = map[name] = data || {name: name, children: []};
                    if (name.length) {
                        node.parent = find(name.substring(0, i = name.lastIndexOf(".")));
                        node.parent.children.push(node);
                        node.key = name.substring(i + 1);
                    }
                }
                return node;
            }

            classes.forEach(function(d) {
                find(d.name, d);
            });

            return map[""];
        },
        // Return a list of links for the given array of nodes.
        links: function(nodes, oldestDate) {
            var map = {},
                    links = [];

            // Compute a map from name to node.
            nodes.forEach(function(d) {
                map[d.name] = d;
            });

            // For each import, construct a link from the source to target node.
            nodes.forEach(function(d) {
                // check if it's not a group node -> e.g. ml for ml.name
                if (d.links) {
                    d.links.forEach(function(i) {
                        var sourceCount = 0;
                        var targetCount = 0;
                        var currentSource = d.name;
                        var currentTarget = i.target;


                        sourceCount = i.count;

                        // count how many times t target links to the source
                        nodes.forEach(function(o) {
                            if (o.name == currentTarget) {
                                o.links.forEach(function(ol) {
                                    if (ol.target == currentSource) {
                                        targetCount = ol.count;
                                    }
                                });
                            }
                        });
                        if (currentSource != currentTarget) {
                            if (sourceCount >= targetCount) {
                                links.push({source: map[d.name], target: map[i.target], strength: (sourceCount + targetCount)});
                            }
                        }
                    });
                }
            });

            return links;
        },
        // calculates and returns the link with the highest value
        maxLinks: function(links) {
            var max = 0;

            links.forEach(function(l) {
                if (l.strength > max) {
                    max = l.strength;
                }
            });

            return max;
        }
    };
})();