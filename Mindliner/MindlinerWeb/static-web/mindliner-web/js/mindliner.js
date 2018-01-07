 
    var mindlinerApp = angular.module('mindlinerApp', ['ngRoute'])
    .controller('headerController', ['$route', '$routeParams', '$location', '$http', '$scope',
        function ($route, $routeParams, $location, $http, $scope) {
            this.$route = $route;
            this.$location = $location;
            this.$routeParams = $routeParams;   
            
            //load slogans
            $http.get('data/slogans.json')
            .then(function(res){
                $scope.slogans = res.data;   
            });            
        }
    ])
    .controller('anchorController', ['$anchorScroll', '$location', '$scope',
      function ($anchorScroll, $location, $scope) {
        $scope.gotoAnchor = function(x) {
          if ($location.hash() !== x) {
            // set the $location.hash to `newHash` and
            // $anchorScroll will automatically scroll to it
            $location.hash(x);
            $anchorScroll.yOffset = 200;
            $anchorScroll();
          } else {
            // call $anchorScroll() explicitly,
            // since $location.hash hasn't changed
            $anchorScroll();
          }
        };
      }
    ])
    .controller('tutorialsController', ['$scope', '$http',
        function($scope, $http) {
            //load tutorial data from json
            $http.get('data/tutorials.json')
            .then(function(res){
                $scope.tutorials = res.data;   
            
                // workaround for chrome to stop pending video load requests
                // last answer in http://stackoverflow.com/questions/16137381/html5-video-element-request-stay-pending-forever-on-chrome
                if(window.stop !== undefined) {
                    window.stop();
                } else if(document.execCommand !== undefined) {
                    document.execCommand("Stop", false);
                }
            });
        }
    ])
    .controller('downloadsController', ['$scope', '$http',
        function($scope, $http) {
            //load download data from json
            $http.get('data/downloads.json')
            .then(function(res){
                $scope.downloads = res.data;   
            });
        }
    ])   
    // defines show/hide/slide behaviour of the tutorials
    .directive('tutorialSlider', function() {
        return {
          link : function(scope, element, attrs) {
            $(element).click(function() {
                var current = $(element); //selected video slide
                if (current.hasClass("actual")) {
                    //selected video is already open, close it now
                    $(".actual .video-section").slideDown(400);
                    current.removeClass("actual").next().removeClass("open").slideUp(400).children().first().get(0).pause();
                    $(".arrow-up").hide();
                    $(".arrow-down").show();
                }
                else {
                    if (!($('.open').length==0)) {
                        //another video is already open, close it first
                        $(".actual .video-section").slideDown(400);
                        $('.open').children().first().get(0).pause();
                        $('.open').slideUp(400).removeClass("open").prev().removeClass("actual");
                        $(".arrow-up").hide();
                        $(".arrow-down").show();
                    }
                    //open selected video
                    current.addClass("actual");
                    $(".actual .video-section").slideUp(400);
                    $(".actual .arrow-up").show();
                    $(".actual .arrow-down").hide();
                    current.next().addClass("open").delay(100).slideDown(400);
                }
            });
          }
        }
    })
    /*
     * Source: http://alxhill.com/blog/articles/angular-scrollspy/
     * Converted from CoffeeScript and fixed at few issues
     */
    .directive('scrollSpy', function($window) {
    return {
        restrict: 'A',
        controller: function($scope) {
          $scope.spies = [];
        },
        link: function(scope, elem, attrs) {
          var spyElems;
          spyElems = [];
          scope.$watch('spies', function(spies) {
            var i, len, results, spy;
            results = [];
            for (i = 0, len = spies.length; i < len; i++) {
              spy = spies[i];
              if (spyElems[spy.id] == null) {
                results.push(spyElems[spy.id] = $('#' + spy.id));
              } else {
                results.push(void 0);
              }
            }
            return results;
          });
          return $($window).scroll(function() {
            var highlightSpy, i, len, pos, ref, spy;
            highlightSpy = null;
            ref = scope.spies;
            for (i = 0, len = ref.length; i < len; i++) {
              spy = ref[i];
              spy.out();
              // offset (-100) ensures that the spy changes earlier
              if ((pos = spyElems[spy.id].offset().top-300) - $window.scrollY <= 0) {
                spy.pos = pos;
                if (highlightSpy == null) {
                  highlightSpy = spy;
                }
                if (highlightSpy.pos < spy.pos) {
                  highlightSpy = spy;
                }
              }
            }
            return highlightSpy != null ? highlightSpy["in"]() : void 0;
          });
        }
      };
    })
    .directive('spy', function() {
      return {
        restrict: "A",
        require: "^scrollSpy",
        link: function(scope, elem, attrs) {
          return scope.spies.push({
            id: attrs.spy,
            "in": function() {
              return elem.addClass('active');
            },
            out: function() {
              return elem.removeClass('active');
            }
          });
        }
      };
    })
    .config(function($httpProvider, $routeProvider, $locationProvider) {
    
        $routeProvider
            .when('/', {
                redirectTo  : '/product',
                templateUrl : 'partials/product.html',
                controller  : 'headerController'
            })
            .when('/tutorials', {
                templateUrl : 'partials/tutorials.html',
                controller  : 'tutorialsController'
            })
            .when('/product', {
                templateUrl : 'partials/product.html',
                controller  : 'headerController'
            })
            .when('/downloads', {
                templateUrl : 'partials/downloads.html',
                controller  : 'downloadsController'
            })
            .when('/impressum', {
                templateUrl : 'partials/impressum.html',
                controller  : 'headerController'
            })
            .when('/desktop', {
                templateUrl : 'partials/desktop.html',
                controller  : 'headerController'
            })
            .when('/404', {
                templateUrl : 'partials/404.html',
                controller  : 'headerController'
            })
            .otherwise({
                redirectTo: '/404'
            });
            
            // use the HTML5 History API
            $locationProvider.html5Mode(true);
    });
    /* potential solution if we have trouble with requesting resources
    .filter('trusted', ['$sce', function ($sce) {
        return function(url) {
            return $sce.trustAsResourceUrl(url);
        };
    }]);*/
    
    
  