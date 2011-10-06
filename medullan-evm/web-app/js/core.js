function pull(namespace) {
	return {
		as: function(shortname) {
			window[shortname] = namespace;
		}
	}
}