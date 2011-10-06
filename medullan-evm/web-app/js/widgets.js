var BlackWhite = {
	Widgets : {}
};

/**
 * 
 */
BlackWhite.Widgets.Events = {
	events: {
		"click":		"domEvent",
		"mouseover":	"domEvent",
		"mouseout":		"domEvent",
		"mousedown":	"domEvent",
		"mouseup":		"domEvent",
		"mousemove":	"domEvent",
		"keypress":		"domEvent",
		"keydown":		"domEvent",
		"keyup":		"domEvent"
	},
	
	domEvent: function(event) {
		this.trigger(event.type, { source: this, domEvent: event });
	}
};

/**
 * 
 */
BlackWhite.Widgets.Controls = (function() {
	
	var control = Backbone.View.extend({
		parent: null,
		
		initialize: function(options) {
			_.extend(this, BlackWhite.Widgets.Events);
			_.extend(this, Backbone.Events);
			this.delegateEvents(this.events);  	// need to do this as it occurs
												// earlier in the leftcycle than when we load the mixin
			
			this.trigger("initialize", this);
			
			this.postInitialize();
		},
		
		postInitialize: function() {
			// override in subclasses
		},
		
		setWidth: function(width) {
			$(this.el).css("width", width);
			return this;
		},
		
		setHeight: function(height) {
			$(this.el).css("height", height);
			return this;
		},
		
		setEnabled: function(enabled) {
			$(this.el).attr("enabled", enabled);
			return this;
		}
	});
	
	var label = control.extend({
		tagName: "span",
		
		className: "UILabel",

		setText: function(text) {
			$(this.el).text(text);
			return this;
		}
		
	});
	
	var button = control.extend({
		tagName: "button",
		
		className: "UIButton",
		
		setLabel: function(label) {
			$(this.el).text(label);
			return this;
		}
	});
	
	var textBox = control.extend({
		tagName: "input",
		
		className: "UITextBox",
		
		password: false,
		
		setPassword: function(password) {
			this.password = password;
		},
		
		postInitialize: function() {
			if(this.password) {
				$(this.el).attr("type", "password");
			} else {
				$(this.el).attr("type", "text");
			}
		}
	});
	
	/**
	 * CSS styles:
	 * 
	 * For the List box: .selectlist For each list item in the box: .item
	 */
	var listView = control.extend({
		selectedItem : null,

		selectedIndex : null,

		listItems : [],
		
		className: "UIListView",

		postInitialize : function() {
			_.bindAll(this, "render", "item_onSelected");
			_.extend(this, Backbone.Events);

			this.itemTemplate = this.options.itemTemplate;

			this.collection.bind("add", this.render, this);
			this.collection.bind("all", this.render, this);
		},

		render : function() {
			$(this.el).children().empty();

			var self = this;

			this.collection.each(function(item) {
				var itemToRender = new self.ListItem({
					model : item,
					itemTemplate : self.itemTemplate
				});
				
				itemToRender.bind("selected", self.item_onSelected);
				self.listItems.push(itemToRender);

				$(self.el).append(itemToRender.render().el);
			});
		},

		item_onSelected : function(e) {
			_.each(this.listItems, function(item) {
				item.unhighlight();
			});

			e.source.highlight();
			this.selectedItem = e.source.model;

			this.trigger("change");
		},

		/**
		 * <h3>Events</h3>
		 * 
		 * <ul>
		 * <li> selected - when an item is selected</li>
		 * </ul>
		 * 
		 * <h3>Styles</h3>
		 * 
		 * <ul>
		 * <li> .highlighted - when an item in the list is highlighted</li>
		 * <li> .hover - when an item in the list is hovered over</li>
		 * </ul>
		 */
		ListItem : Backbone.View.extend({
			events : {
				"click" : "item_onClick",
				"mouseover" : "item_onMouseover",
				"mouseout" : "item_onMouseout"
			},
			
			className: "UIListView-item",

			initialize : function(options) {
				_.bindAll(this, "render", "item_onClick", "item_onMouseover",
						"item_onMouseout", "highlight", "unhighlight");
				_.extend(this, Backbone.Events);

				this.itemTemplate = options.itemTemplate;

				this.model.bind("change", this.render, this);
			},

			render : function() {
				$(this.el).append(
						_.template(this.itemTemplate, this.model.toJSON()));
				return this;
			},

			item_onClick : function(e) {
				this.trigger("selected", {
					source : this
				});
			},

			item_onMouseover : function(e) {
				$(this.el).addClass("hover");
			},

			item_onMouseout : function(e) {
				$(this.el).removeClass("hover");
			},

			highlight : function() {
				$(this.el).addClass("highlighted");
			},

			unhighlight : function() {
				$(this.el).removeClass("highlighted");
			}
		})
	});

	return {
		TextBox: textBox,
		Label : label,
		Button : button,
		ListView : listView
	};
	
})();

BlackWhite.Widgets.Layouts = (function() {
	var container = Backbone.View.extend({
		childElements: null,
		
		parent: null,
		
		initialize : function(options) {
			_.extend(this, Backbone.Events);
			
			var self = this;
			
			this.childElements = new Array();
			
			_.each(this.options.children, function(child) {
				self.add(child);
			});
			
			if(this.options.rootEl) {
				$(this.options.rootEl).append($(this.el));
			}
			
			this.postInitialize();
		},
		
		postInitialize: function() {
			// override in subclassses - allows for some constructor logic
		},
		
		setVisible: function(visible) {
			if(visible) {
				$(this.el).removeClass("hidden");
			} else {
				$(this.el).addClass("hidden");
			}
			
			return this;
		},

		add: function(child) {
			child.parent = this;
			this.childElements.push(child);
			
			$(this.el).append($(child.el));
			
			this.trigger("childAdded");
			
			return this;
		},
		
		remove: function(child) {
			this.childElements = _.without(this.childElements, child);
			$(child.el).detach();

			this.trigger("childRemoved", { child: child });
			
			return this;
		},
		
		removeAllChildren: function() {
			var self = this;
			
			_.each(this.childElements, function(child) {
				self.remove(child);
			});
		},
		
		removeAt: function(index) {
			this.trigger("childRemoved");
			return this;
		},
		
		setWidth: function(width) {
			$(this.el).css("width", width);
			return this;
		},
		
		setHeight: function(height) {
			$(this.el).css("height", height);
			return this;
		},
		
		setAlign: function(align) {
			$(this.el).css("text-align", align);
			return this;
		}
	});

	var box = container.extend({});

	var hbox = box.extend({
		className: "UIHBox"
	});

	var vbox = box.extend({
		className: "UIVBox"
	});

	return {
		Container : container,
		Box : box,
		HBox : hbox,
		VBox : vbox
	};
})();

BlackWhite.Widgets.Windows = (function() {
	var dialogueBox = BlackWhite.Widgets.Layouts.Container
			.extend({
				id: "UIDialogueBox",
				className: "hidden",
				modal : true,
				
				postInitialize: function() {
					$(this.el).html("<div class=\"title\"></div>");
				},

				setX : function(x) {
					$(this.el).css("left", x);
					return this;
				},

				setY : function(y) {
					$(this.el).css("top", y);
					return this;
				},
				
				setTitle: function(title) {
					$(this.el).children(".title").text(this.options.title = title);
				},

				open : function(title, children) {
					var self = this;
					
					if (this.modal) {
						$("body").append("<div id=\"hider\"></div>");
					}
					
					this.removeAllChildren();
					
					_.each(children, function(child){
						self.add(child);
					});
					
					this.setHeight(this.options.height)
						.setWidth(this.options.width)
						.setX((window.innerWidth - this.options.width) / 2)
						.setY((window.innerHeight - this.options.height) / 2);

					this.setVisible(true);
					
					this.setTitle(title);
					
					$("body").append(this.el);
				},

				close : function() {
					$("#hider").detach();
					$(this.el).addClass("hidden");
				}
			});

	return {
		DialogueBox : dialogueBox
	};
})();