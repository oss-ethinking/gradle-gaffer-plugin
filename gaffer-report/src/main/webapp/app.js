var myapp = angular.module('app', [ 'ngMaterial', 'nvd3' ]);
//.config(
//		function($mdThemingProvider) {
//			$mdThemingProvider.theme('default').primaryPalette('deep-orange');
//		}
//);

myapp.controller('mainController', [ '$scope', '$mdSidenav',
		function($scope, $mdSidenav) {
			$scope.dateFormat = 'yyyy-MM-dd HH:mm:ss Z'
			$scope.report = report;
			$scope.queryFilter = {
				 query:null	
			};
			
			$scope.searchFilter = function(assemble) {
				
				return assemble.assemble.indexOf($scope.queryFilter.query)>-1;
			};
			
			$scope.content = "/templates/dashboard.html";
			$scope.toggleSidenav = function(menuId) {
				$mdSidenav(menuId).toggle();
			};

			$scope.taskData = report['taskReports'];
			$scope.taskChartOptions = {
				chart : {
					type : 'pieChart',
					x : function(d) {
						return d.assemble;
					},
					y : function(d) {
						return d.bytes;
					},
					showLabels : true,
					transitionDuration : 500,
					labelThreshold : 0.01,
					legend : {
						margin : {
							top : 5,
							right : 35,
							bottom : 5,
							left : 0
						}
					}
				}
			};
			$scope.barData = [{
				key : 'Tasks',
			    values:  report['taskReports']

			}];

			$scope.taskBarOptions = {
				chart : {
					type : "discreteBarChart",
					height:400,
					x : function(d) {
						return d.assemble;
					},
					y : function(d) {
						
						return d.files;
					},
					margin : {
						top : 0,
						right : 20,
						bottom : 5,
						left : 0
					},
					showValues : true,
					transitionDuration : 500,
					xAxis : {
						axisLabel : "X Axis"
					},
					yAxis : {
						axisLabe : "Y Axis",
						axisLabelDistance : 30
					},
					valueFormat : function(d) {	
						return d;
					},
				}
			};

			$scope.taskCopyOptions = {
		            chart: {
		                type: 'pieChart',
		                donut: true,
		                x: function(d){return d.assemble;},
		                y: function(d){return d.copyReports.length;},
		                showLabels: true,

		                pie: {
		                    startAngle: function(d) { return d.startAngle/2 -Math.PI/2 },
		                    endAngle: function(d) { return d.endAngle/2 -Math.PI/2 }
		                },
		                transitionDuration: 500,
		                legend: {
		                    margin: {
		                        top: 5,
		                        right: 70,
		                        bottom: 5,
		                        left: 0
		                    }
		                }
		            }
		        };

		        $scope.taskCopyData = report['taskReports'];

		} ]);
myapp.directive('assemble', function () {
	  return {
		    restrict: 'E',
		    scope: { 'assemble': '=data' },
		    templateUrl: "/templates/detail-assemble.html"
		  };
		});