package medullan.evm

import grails.test.ControllerUnitTestCase
import grails.web.JSONBuilder

class ProjectControllerTests extends ControllerUnitTestCase {
    protected void setUp() {
        super.setUp()
		
		mockConfig('''
			rally {
				username = 'rnorris@medullan.com'
				password = 'Wfly, wnf?'
				api {
					version = 1.26
				}
			}
		''')
		
		controller.class.metaClass.render = { Map map, Closure closure ->
			delegate.response.outputStream << new JSONBuilder().build(closure).toString()
        }
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testReadingProjectsInWorkspace() {
		
		mockSession.workspaceId = 'https://rally1.rallydev.com/slm/webservice/1.26/workspace/4041143754.js'
		mockRequest.contentType = "application/json"
		
		controller.read()
		
		assert controller.response.contentAsString
		
    }
}
