var myapp = angular.module('app', [ 'ngMaterial', 'nvd3' ]);

myapp
		.controller(
				'mainController',
				[
						'$scope',
						'$mdSidenav',
						function($scope, $mdSidenav) {
							$scope.dateFormat = 'yyyy-MM-dd HH:mm:ss Z';
							$scope.report = report;
							$scope.queryFilter = {
								query : null
							};
							$scope.clearSearch = function() {
								$scope.queryFilter.query = null
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
							$scope.barData = [ {
								key : 'Tasks',
								values : report['taskReports']

							} ];

							$scope.taskBarOptions = {
								chart : {
									type : "discreteBarChart",
									height : 400,
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
									}
								}
							};

							$scope.taskCopyOptions = {
								chart : {
									type : 'pieChart',
									donut : true,
									x : function(d) {
										return d.assemble;
									},
									y : function(d) {
										return d.copyReports.length;
									},
									showLabels : true,

									pie : {
										startAngle : function(d) {
											return d.startAngle / 2 - Math.PI
													/ 2
										},
										endAngle : function(d) {
											return d.endAngle / 2 - Math.PI / 2
										}
									},
									transitionDuration : 500,
									legend : {
										margin : {
											top : 5,
											right : 70,
											bottom : 5,
											left : 0
										}
									}
								}
							};

							$scope.taskCopyData = report['taskReports'];

						} ]);
myapp.directive('assemble', function() {
	return {
		restrict : 'E',
		scope : {
			'assemble' : '=data'
		},
		templateUrl : "/templates/detail-assemble.html"
	};
});

myapp.filter('fileSearch', function() {

	return function(report, queryFilter) {
		var results = [];
		if (queryFilter.query) {
			for (var j = 0; j < report.taskReports.length; j++) {
				var assemble = report.taskReports[j];
				var copies = [];
				for (var i = 0; i < assemble['copyReports'].length; i++) {
					var copy = assemble['copyReports'][i];
					var files = [];
					for (var x = 0; x < copy['files'].length; x++) {
						var file = copy['files'][x];
						if (file['path'].indexOf(queryFilter.query) > -1) {
							file['id']="file_"+j+"_"+i+"_"+x;
							files.push(file);
						}
					}
					if (files.length > 0) {
						var fileHits = {};
						fileHits['files'] = files;
						fileHits['id'] = "copy_"+j+"_"+i;
						copies.push(fileHits);
					}
				}
				if (copies.length > 0) {
					var hit = {};
					hit['assemble'] = assemble.assemble;
					hit['copyReports'] = copies;
					hit['id']="resultID"+j;
					results.push(hit);
				}
			}
			
		}
		return results;
	};
});
