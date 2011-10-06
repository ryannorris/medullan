package medullan.evm

import grails.plugin.rally.RallyConnectionFactory


class ProjectController {

	def read = {
			
		RallyConnectionFactory factory = new RallyConnectionFactory()

		if(params.id) {
			
			log.debug("loading iterations for project: ${params.id}")
			
			ValueMeasuredProject theProject = new ValueMeasuredProject(params.id, null, factory)
			def projectIterations = theProject.iterations()
			
			projectIterations.sort { it.get('StartDate').substring(0, 9).replace('-', '') }

			withFormat {
				json {
					render(contentType: 'application/json') {
						results(success: true)
						project {
							title = theProject.getObjectTitle()
							iterations = array {
								projectIterations.each { projectIteration ->
									iteration(
										name: projectIteration.getObjectTitle(), 
										id: projectIteration.getObjectId(),
										plannedValue: projectIteration.plannedValue(),
										earnedValue: projectIteration.earnedValue())
								}
							}
						}
					}
				}
			}
			
		} else {

			ValueMeasuredWorkspace theWorkspace = new ValueMeasuredWorkspace('https://rally1.rallydev.com/slm/webservice/1.26/workspace/4041143754.js', factory)
			def workspaceProjects = theWorkspace.projects()

			log.debug("request format is ${request.format}")

			withFormat {
				html { }
				json {
					render(contentType: 'application/json') {
						results(success: true, count: workspaceProjects.size())
						projects = array {
							workspaceProjects.each { workspaceProject ->
								project(name: workspaceProject.getObjectTitle(), id: workspaceProject.getObjectId())
							}
						}
					}
				}
			}
		}
	}
}
