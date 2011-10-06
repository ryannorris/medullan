<html xmlns:g="grails">
<head>
<title>Medullan EVM</title>
<meta name="layout" content="main" />
</head>
<body>

<style>

* {
	font-family: "Lucida Grande", Verdana, Helvetica, Arial, sans-serif;
	font-size: 100%;  /* reset */
}

.UIListView {
	min-height: 30px;
	border: 2px solid #888888;
	overflow-y: auto;
}

.UIListView-item {
	padding: 3px;
}

.hover {
	background-color: #EEEEEE;
	cursor: pointer;
}

.disabled {
	cursor: default;
	color: #EEEEEEE;
}

.highlighted {
	background-color: #FF9933;
	font-weight: bold;
}

.panel {
	clear: both;
}

.float {
	padding: 8px;
	float: left;
}

.hidden {
	display: none;
}

#hider {
	position: absolute;
	z-index: 99;
	top: 0px;
	left: 0px;
	width: 100%;
	height: 100%;
	background-color: #888888;
	opacity: 0.6;
}

#UIDialogueBox {
	position: absolute;
	z-index: 100;
	border: 2px solid #888888;
	background-color: #FFFFFF;
}

#UIDialogueBox .title {
	padding: 3px;
	font-size: 12pt;
	text-align: center;
	background-color: #666666;
	color: #FFFFFF;
}

.UIHBox {
	clear: both;
	padding: 6px;
}

.UIHBox > div {
	float: left;
	padding: 3px;
}

</style>

<g:javascript library="jquery-1.6.2.min" />
<g:javascript library="underscore-min" />
<g:javascript library="backbone-min" />

<script type="text/template" id="project-item-template">
{{name}}
</script>

<script type="text/template" id="iteration-detail-template">
<h2>{{name}}</h2>
<div>Planned Value: {{plannedValue}}</div>
<div>Earned Value: {{earnedValue}}</div>
</script>

<g:javascript library="core" />
<g:javascript library="widgets" />
<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script type="text/javascript">
<!--

google.load("visualization", "1.0", {packages:["corechart"]});

_.templateSettings = {
	interpolate : /\{\{(.+?)\}\}/g
};

$(function() {

	pull(BlackWhite.Widgets.Controls).as("controls");
	pull(BlackWhite.Widgets.Windows).as("windows");
	pull(BlackWhite.Widgets.Layouts).as("layouts");
	pull(BlackWhite.Widgets).as("widgets");

	/* all the models and collections */
	
	window.Iteration = Backbone.Model.extend({
		url: function() {
			return "iteration?id=" + this.id
		},

		parse: function(response) {
			return response.iteration;
		}
	});

	window.Iterations = Backbone.Collection.extend({
		model: Iteration,
		
		url: "iteration",

		parse: function(response) {
			return response.iterations;
		}
	});

	window.Project = Backbone.Model.extend({
		url: function() {
			return "project?id=" + this.id
		}

	});

	window.Projects = Backbone.Collection.extend({
		model: Project,
		
		url: "project",

		parse: function(response) {
			return response.projects;
		}
	});

	/* application specific components */

	window.ReleaseValueChart = layouts.Box.extend({

		postInitialize: function(options) {
			_.bindAll(this, "render", "initializeDataTable", "dataChange");
			$(this.el).css("clear", "both");
			this.initializeDataTable();
			
			this.chart = new google.visualization.LineChart(this.el);
			
			this.collection.bind("all", this.dataChange, this);
		}, 

		initializeDataTable: function() {
			this.data = new google.visualization.DataTable();
			this.data.addColumn('string', 'Sprint');
			this.data.addColumn('number', 'Planned Value');
			this.data.addColumn('number', 'Earned Value');
		},

		dataChange: function() {

			var sprint = 0;

			var self = this;

			var planned = 0;
			var earned = 0;

			this.data.removeRows(0, this.data.getNumberOfRows());
			this.data.addRows(this.collection.size());
			
			this.collection.each(function(model) {
				planned += model.get('plannedValue');
				earned += model.get('earnedValue');
				self.data.setValue(sprint, 0, model.get('name'));
				self.data.setValue(sprint, 1, planned);
				self.data.setValue(sprint, 2, earned);

				sprint++;
			});

			this.render();
		},

		render: function() {
	        this.chart.draw(this.data, {width: this.options.width, height: this.options.height, title: 'Current Release EVM'});
		}
	});

	window.IterationDetailPanel = layouts.Container.extend({

		template: _.template($("#iteration-detail-template").html()),

		initialize: function(options) {
			_.bindAll(this, "render");
			
			this.model.bind("change", this.render, this);
		},

		show: function() {
			$(this.el).removeClass("hidden");
		},

		hide: function() {
			$(this.el).addClass("hidden");
		},

		render: function() {
			$(this.el).empty();
			$(this.el).append(this.template(this.model.toJSON()));
		}
	});
	
	var projects = new Projects();

	var iterations = new Iterations();

	var projectList = new controls.ListView({ collection: projects, itemTemplate: $("#project-item-template").html() }).setWidth(300).setHeight(200);
	var iterationList = new controls.ListView({ collection: iterations, itemTemplate: $("#project-item-template").html() }).setWidth(500).setHeight(200);

	var projectListView = new layouts.VBox({ children: [ new controls.Label().setText("Projects"), projectList ] });
	var iterationListView = new layouts.VBox({ children: [ new controls.Label().setText("Iterations"), iterationList ] });
	
	projects.fetch();
	
	var iteration = new Iteration();
	var iterationDetailView = new IterationDetailPanel({ model: iteration });

	var evmChart = new ReleaseValueChart({ collection: iterations, width: 600, height: 300 });
	
	var layout = new layouts.VBox({ rootEl: "body" }).add(
			new layouts.HBox({ children: [ projectListView, iterationListView, iterationDetailView ]})
	).add(evmChart);

	var box = new windows.DialogueBox({ width: 450, height: 150, title: "EVM" });

	var pendingText = new controls.Label().setText();

	var button = new controls.Button().setLabel("Close");
	
	button.bind("click", function(e) {
		box.close();
	});
	
	projectList.bind("change", function() {
		pendingText.setText("Loading data for " + projectList.selectedItem.get('name'));
		box.open(
				"One Moment", 
				[ new layouts.Box({ children: [ pendingText ] }).setWidth("100%").setAlign("center") ]);
		
		var selectedProject = projectList.selectedItem.clone();

		selectedProject.fetch({ 
			success: function() {
				iterations.reset(selectedProject.get('project').iterations);
				box.close();
			},

			error: function() {
				pendingText.setText("An error occurred loading project data.  Please try again later.");
				box.add(button);
			}
		});
	});

	iterationList.bind("change", function() {
		pendingText.setText("Loading data for " + iterationList.selectedItem.get('name'));

		box.open(
				"One Moment", 
				[ new layouts.Box({ children: [ pendingText ] }).setWidth("100%").setAlign("center") ]);
	
		var selectedIteration = iterationList.selectedItem.clone();
		
		selectedIteration.fetch({
			success: function() {
				iteration.set(selectedIteration.toJSON());
				iterationDetailView.show();
				box.close();
			},

			error: function() {
				pendingText.setText("An error occurred loading iteration data.  Please try again later.");
				box.add(button);
			}
		});
	});
});

-->
</script>


</body>
</html>

