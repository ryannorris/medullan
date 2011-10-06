class UrlMappings {

	static mappings = {
		"/login/$action?"(controller: "login")
		"/logout/$action?"(controller: "logout")
		"/"(parseRequest: true) {
			controller = "project"
			action = [GET: "read", PUT: "update", DELETE: "delete", POST: "create"]
		}
		"/$controller"(parseRequest: true) {
			action = [GET: "read", PUT: "update", DELETE: "delete", POST: "create"]
		}
	}
}
