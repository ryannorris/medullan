package medullan.evm

import grails.test.ControllerUnitTestCase;

class UserControllerTests extends ControllerUnitTestCase {
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
    }

    protected void tearDown() {
		
		mockRequest.params.workspaceId = ""
		
    }
}
