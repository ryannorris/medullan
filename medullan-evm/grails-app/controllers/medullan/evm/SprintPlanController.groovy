package medullan.evm

import grails.plugin.rally.Iteration


class SprintPlanController {

    def read = { 
		EarnedValueRequirement requirement = new EarnedValueRequirement(null, null, false)
		def queryResult = requirement.query("(Iteration = {$params.id})", 100, 1, true)
		
		Iteration iteration = new Iteration(params.id, null, null)
		
		def sprintPlannedValue = queryResult.result.additiveValueInWorkBreakdown()*.sum()
		
		withFormat {
			json {
				render(contentType: "application/json") {
					sprint(theme: iteration.getObjectTitle(), plannedValue: sprintPlannedValue)
				}
			}
		}
		
		queryResult.results.each { result ->
			println result.additiveValueInWorkBreakdown() 
		}
	}
}
