package medullan.evm

import grails.plugin.rally.HierarchicalRequirement
import grails.plugin.rally.RallyApiObject
import grails.test.ControllerUnitTestCase

class QueryControllerTests extends ControllerUnitTestCase {
	protected void setUp() {
		super.setUp()
		
		mockLogging(RallyApiObject)
		mockLogging(HierarchicalRequirement)

		mockConfig('''
		rally {
			username = 'rnorris@medullan.com'
			password = 'Wfly, wnf?'
			api {
				version = 1.26
			}
		}
	''')
	}

	protected void tearDown() {
		super.tearDown()
	}
	
	void testSomethingElse() {
		EarnedValueRequirement requirement = new EarnedValueRequirement(null, null, false)
		def queryResult = requirement.query("(Release = https://rally1.rallydev.com/slm/webservice/1.26/release/4041172829.js)", 100, 1, true)
		
		queryResult.results.each { result ->
			println result.additiveValueInWorkBreakdown() 
		}
		
	}

	void donttestSomething() {
		HierarchicalRequirement project = new HierarchicalRequirement(null, null, false)
		def queryResult = project.query("(Release = https://rally1.rallydev.com/slm/webservice/1.26/release/4041172829.js)", 100, 1, true)
		// TODO need to iterate on this when there are more than 100 stories
		
		// every damn story has a parent because Rally only applies releases to leaf nodes in the tree.  We have to walk.
		
		
		
		def resultMap = [:]
		
		def rootNodes = queryResult.results.findAll { ! it.parent() }		// there may be several root nodes
		
		rootNodes.each { root ->
			def kids = root.children()
			println kids*.getObjectTitle()
		}
		
		queryResult.results.each { result ->
			
			// each result is either a node or a leaf in the tree
			// it's a leaf if there are no children
			// it's a node if it has a parent AND children
			// without a parent, it's the root node
			
			// the trick is to connect the various nodes to form a tree
			
			if(! result.parent()) {				// root node
				// attach all branch nodes to this one	
			}
			
			if(! result.children()) {			// leaf node
					
			}
			
			def objectId = result.parent() ? result.parent().getObjectId() : result.getObjectId()
			def object = result.parent() ? result : null
			
			if(! resultMap[objectId]) {
				resultMap[objectId] = []
			}
			
			resultMap[objectId] += object
		}
		
		println resultMap
	}
}
