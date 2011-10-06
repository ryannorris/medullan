_.templateSettings = {
	interpolate : /\<\?\=(.+?)\?\>/g
}

var Medullan = {
	EVM : {
		UI : {
			Workspace : Backbone.Router.extend({
				initialize: function(options) {
					_.bindAll(this, 'onCardCreation', 'start', 'edit');
					
					this.eventBus = options.eventBus;
					// this.eventBus.bind('cardCreated', this.onCardCreation)
				},
				
				routes : {
					""	: "start",
					"edit/:id":	"edit",
				},
				
				start: function() {
					var toolbar = new Medullan.EVM.UI.View.Toolbar({ eventBus: this.eventBus });
					var desktop = new Medullan.EVM.UI.View.Desktop({ eventBus: this.eventBus })
				},
				
				edit: function(storyId) {
					var wi = new Medullan.EVM.UI.Model.WorkItem({ id: storyId });
					wi.save();
				},
				
				// responses to events on the bus
				onCardCreation: function(e) {
					this.navigate("edit/" + e.card.id);
				},
			}),

			EventBus : function() { return _.extend({}, Backbone.Events) },

			View : {
				Toolbar : Backbone.View.extend({
					el: $("#toolbar"),
					
					events : {
						"click button[name='create']" : "createStory"
					},
					
					initialize : function(options) {
						_.bind(this, "createStory");
					},

					createStory : function(e) {
						var workItem = new Medullan.EVM.UI.Model.WorkItem();
						this.options.eventBus.trigger('cardCreated', {
							workItem : workItem
						});
					}
				}),

				Desktop : Backbone.View.extend({

					el: $("#desktop"),
					
					initialize : function(options) {
						var card = new Medullan.EVM.UI.Model.WorkItem();
						var view = new Medullan.EVM.UI.View.StoryCard({ model: card });
					}

				}),

				StoryCard : Backbone.View.extend({
					
					el: $("#story-card"),

					initialize : function(options) {
						_.bind(this, "createEntry");
					},
					
					events : {
						"click button[name='createCardButton']":	"createEntry"
					},
					
					createEntry: function(e) {
						var model = { 
								name: $("input[name='story-name']").val(), 
								businessValue: $("input[name='story-value']").val()
							};
						this.model.save(model);
					},

					render : function() {
					}
				})
			},

			Model : {
				WorkItem : Backbone.Model.extend({
					url: '/medullan-evm/api/work-items'
				}),
				
				WorkItems: Backbone.Collection.extend({
					model: this.WorkItem
				})
			}
		},

		AppView : function(context) {

			var bus = new Medullan.EVM.UI.EventBus();
			var router = new Medullan.EVM.UI.Workspace({ eventBus: bus });
			
			Backbone.history.start({
				pushState : false,
				root : context // this is used for when we're not at the application root context
			});
		}
	}
}